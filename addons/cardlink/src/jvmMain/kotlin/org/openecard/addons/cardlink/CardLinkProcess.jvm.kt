/****************************************************************************
 * Copyright (C) 2024 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.addons.cardlink

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.CreateSession
import iso.std.iso_iec._24727.tech.schema.CreateSessionResponse
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticate
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticateResponse
import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import iso.std.iso_iec._24727.tech.schema.DestroySession
import iso.std.iso_iec._24727.tech.schema.DestroySessionResponse
import iso.std.iso_iec._24727.tech.schema.InputAPDUInfoType
import iso.std.iso_iec._24727.tech.schema.PowerDownDevices
import iso.std.iso_iec._24727.tech.schema.Transmit
import iso.std.iso_iec._24727.tech.schema.TransmitResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.openecard.addon.Context
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.addons.cardlink.sal.CARDLINK_PROTOCOL_ID
import org.openecard.addons.cardlink.sal.CardLinkKeys
import org.openecard.addons.cardlink.sal.setWsPair
import org.openecard.addons.cardlink.ws.GematikEnvelope
import org.openecard.addons.cardlink.ws.ICCSNReassignment
import org.openecard.addons.cardlink.ws.ICCSN_REASSIGNMENT
import org.openecard.addons.cardlink.ws.REGISTER_EGK_FINISH
import org.openecard.addons.cardlink.ws.RegisterEgkFinish
import org.openecard.addons.cardlink.ws.SESSION_INFO
import org.openecard.addons.cardlink.ws.SendApdu
import org.openecard.addons.cardlink.ws.SendApduResponse
import org.openecard.addons.cardlink.ws.SessionInformation
import org.openecard.addons.cardlink.ws.TASK_LIST_ERROR
import org.openecard.addons.cardlink.ws.TasklistErrorPayload
import org.openecard.addons.cardlink.ws.WsPair
import org.openecard.addons.cardlink.ws.cardLinkJsonFormatter
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.WSHelper
import org.openecard.common.toException
import org.openecard.common.util.HandlerUtils
import org.openecard.mobile.activation.CardLinkErrorCodes
import org.openecard.mobile.activation.Websocket
import org.openecard.mobile.activation.WebsocketListener
import kotlin.time.Duration
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private val logger = KotlinLogging.logger {}

class CardLinkProcess(
	private val ctx: Context,
	private val ws: Websocket,
	private val successorListener: WebsocketListener,
) {
	private val dispatcher = ctx.dispatcher

	fun start(): BindingResult {
		// ensure that switch to successor is called in any case
		val wsPair = WsPair.withNewListener(ws, successorListener)
		return wsPair.use { internalProcessing(it) }
	}

	private fun internalProcessing(wsPair: WsPair): BindingResult {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
		val conHandle = openSession()
		dynCtx.put(TR03112Keys.SESSION_CON_HANDLE, HandlerUtils.copyHandle(conHandle))

		val wsListener = wsPair.listener
		setWsPair(dynCtx, wsPair)
		ws.connect()
		wsListener.waitForOpenChannel()

		waitForSessionInformation(dynCtx, wsPair)

		val cardHandle = performDidAuth(conHandle, dynCtx)
		handleRemoteApdus(cardHandle, wsPair)

		destroySession(cardHandle)

		val cardSessionId = dynCtx.get(CardLinkKeys.CARD_SESSION_ID) as String
		val webSocketId = dynCtx.get(CardLinkKeys.WS_SESSION_ID) as String?
		val iccsn = dynCtx.get(CardLinkKeys.ICCSN) as String?
		val iccsnReassignment = dynCtx.get(CardLinkKeys.ICCSN_REASSIGNMENT_TIMESTAMP) as String?

		// no error means success
		val bindingResult = BindingResult(BindingResultCode.OK)
		bindingResult.addParameter(CardLinkKeys.PERSONAL_DATA, dynCtx.get(CardLinkKeys.PERSONAL_DATA) as String)
		bindingResult.addParameter(CardLinkKeys.CARD_SESSION_ID, cardSessionId)
		bindingResult.addParameter(CardLinkKeys.ICCSN, iccsn)
		iccsnReassignment?.let { bindingResult.addParameter(CardLinkKeys.ICCSN_REASSIGNMENT_TIMESTAMP, it) }
		webSocketId?.let { bindingResult.addParameter(CardLinkKeys.WS_SESSION_ID, it) }
		return bindingResult
	}

	@Throws(WSHelper.WSException::class)
	private fun openSession(): ConnectionHandleType {
		// Perform a CreateSession to initialize the SAL
		val createSession = CreateSession()
		val createSessionResp = dispatcher.safeDeliver(createSession) as CreateSessionResponse

		// Check CreateSessionResponse
		WSHelper.checkResult(createSessionResp)

		// Update ConnectionHandle.
		val connectionHandle = createSessionResp.connectionHandle

		return connectionHandle
	}

	@Throws(WSHelper.WSException::class)
	private fun destroySession(conHandle: ConnectionHandleType) {
		// Perform a CloseSession to close the SAL
		val closeSession =
			DestroySession().apply {
				connectionHandle = conHandle
			}
		val pdd = PowerDownDevices()
		pdd.contextHandle = closeSession.connectionHandle.contextHandle
		dispatcher.safeDeliver(pdd)
		val closeSessionResp = dispatcher.safeDeliver(closeSession) as DestroySessionResponse

		// Check CloseSessionResponse
		WSHelper.checkResult(closeSessionResp)
	}

	@Throws(WSHelper.WSException::class)
	private fun performDidAuth(
		conHandle: ConnectionHandleType,
		dynCtx: DynamicContext,
	): ConnectionHandleType {
		// Perform a DIDAuthenticate to authenticate the user
		val didAuth =
			DIDAuthenticate().apply {
				connectionHandle = conHandle
				authenticationProtocolData =
					DIDAuthenticationDataType().apply {
						protocol = CARDLINK_PROTOCOL_ID
					}
			}
		val didAuthResp = dispatcher.safeDeliver(didAuth) as DIDAuthenticateResponse

		// Check DIDAuthenticateResponse
		WSHelper.checkResult(didAuthResp)

		val cardHandle = dynCtx.get(TR03112Keys.CONNECTION_HANDLE) as ConnectionHandleType?
		require(cardHandle != null) { "CardLink Protocol ended without a handle to the connected card." }
		return cardHandle
	}

	private fun handleRemoteApdus(
		cardHandle: ConnectionHandleType,
		wsPair: WsPair,
	) {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)!!
		val wsListener = wsPair.listener

		while (wsListener.isOpen()) {
			val gematikMessage: GematikEnvelope? = wsListener.nextMessageBlocking()

			if (gematikMessage == null) {
				val errorMsg = "Timeout happened during APDU exchange with CardLink-Service."
				logger.warn { errorMsg }
				throw WSHelper.makeResultError(CardLinkErrorCodes.CardLinkCodes.SERVER_TIMEOUT.name, errorMsg).toException()
			}

			if (gematikMessage.payload is SessionInformation) {
				logger.debug {
					"Received '${SESSION_INFO} during ongoing process. Ignoring since most probably caused by reconnection."
				}
				continue
			}

			if (gematikMessage.payload is RegisterEgkFinish) {
				logger.debug { "Received '${REGISTER_EGK_FINISH}' message from CardLink service." }
				return
			}

			if (gematikMessage.payload is ICCSNReassignment) {
				logger.debug { "Received '${ICCSN_REASSIGNMENT}' message from CardLink service." }
				dynCtx.put(CardLinkKeys.ICCSN_REASSIGNMENT_TIMESTAMP, gematikMessage.payload.lastAssignment)
				continue
			}

			if (gematikMessage.payload is TasklistErrorPayload) {
				val errorMsg = gematikMessage.payload.errormessage ?: "Received an unknown error from CardLink service."
				val errorResultCode =
					CardLinkErrorCodes.CardLinkCodes.byStatus(gematikMessage.payload.status)
						?: CardLinkErrorCodes.CardLinkCodes.UNKNOWN_ERROR

				logger.warn { "Received '${TASK_LIST_ERROR}': $errorMsg (Result Code: $errorResultCode)" }

				dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, errorResultCode)
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)

				throw WSHelper.makeResultError(errorResultCode.name, errorMsg).toException()
			}

			if (gematikMessage.cardSessionId == null || gematikMessage.correlationId == null) {
				val errorMsg = "Received malformed SendAPDU message which does not contain a cardSessionId or correlationId."
				logger.warn { errorMsg }

				dynCtx.put(CardLinkKeys.SERVICE_ERROR_CODE, CardLinkErrorCodes.CardLinkCodes.INVALID_WEBSOCKET_MESSAGE)
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)

				throw WSHelper
					.makeResultError(
						CardLinkErrorCodes.CardLinkCodes.INVALID_WEBSOCKET_MESSAGE.name,
						errorMsg,
					).toException()
			}

			if (gematikMessage.payload !is SendApdu) {
				val errorMsg = "Received malformed eGK payload. Payload is not from type: SendApdu."
				logger.error { errorMsg }
			} else {
				val apdu = gematikMessage.payload.apdu
				val correlationId = gematikMessage.correlationId
				val cardSessionId = gematikMessage.cardSessionId

				val apduResponse = sendApduToCard(cardHandle, apdu)

				val egkEnvelope =
					GematikEnvelope(
						SendApduResponse(
							cardSessionId,
							apduResponse,
						),
						correlationId,
						cardSessionId,
					)
				val egkEnvelopeJson = cardLinkJsonFormatter.encodeToString(egkEnvelope)
				ws.send(egkEnvelopeJson)
			}
		}
	}

	private fun sendApduToCard(
		cardHandle: ConnectionHandleType,
		apdu: ByteArray,
	): ByteArray {
		val inputAPDU = InputAPDUInfoType()
		inputAPDU.inputAPDU = apdu

		val t = Transmit()
		t.slotHandle = cardHandle.slotHandle
		t.inputAPDUInfo.add(inputAPDU)

		val response = dispatcher.safeDeliver(t) as TransmitResponse
		WSHelper.checkResult(response)
		return response.outputAPDU[0]
	}

	@OptIn(ExperimentalUuidApi::class)
	private fun waitForSessionInformation(
		dynCtx: DynamicContext,
		wsPair: WsPair,
	) {
		val wsListener = wsPair.listener
		runBlocking {
			val sessionInformation = wsListener.nextMessage(Duration.parse("5s"))
			val payload = sessionInformation?.payload

			if (sessionInformation == null) {
				val errorMsg = "Timeout happened during waiting for '${SESSION_INFO}' message from CardLink-Service."
				logger.warn { errorMsg }
			}

			if (payload != null && payload is SessionInformation) {
				dynCtx.put(CardLinkKeys.CARD_SESSION_ID, sessionInformation.cardSessionId)
				dynCtx.put(CardLinkKeys.WS_SESSION_ID, payload.webSocketId)
				dynCtx.put(CardLinkKeys.PHONE_NUMBER_REGISTERED, payload.phoneRegistered)
				logger.debug {
					"Using ${sessionInformation.cardSessionId} as cardSessionId and ${payload.webSocketId} as webSocketId."
				}
			} else {
				// we generate our own cardSessionId
				val cardSessionId = Uuid.random().toString()
				dynCtx.put(CardLinkKeys.CARD_SESSION_ID, cardSessionId)
				dynCtx.put(CardLinkKeys.PHONE_NUMBER_REGISTERED, false)
				logger.debug { "Received no or a malformed SessionInformation message. Using $cardSessionId as cardSessionId." }
			}
		}
	}
}
