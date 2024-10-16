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
import iso.std.iso_iec._24727.tech.schema.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.openecard.addon.Context
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.addons.cardlink.sal.*
import org.openecard.addons.cardlink.ws.*
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ECardConstants
import org.openecard.common.WSHelper
import org.openecard.common.toException
import org.openecard.common.util.HandlerUtils
import org.openecard.mobile.activation.Websocket
import org.openecard.mobile.activation.WebsocketListener
import java.util.*


private val logger = KotlinLogging.logger {}

class CardLinkProcess(
	private val ctx: Context,
	private val ws: Websocket,
	private val successorListener: WebsocketListener,
) {

	private val dispatcher = ctx.dispatcher

	fun start(): BindingResult {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
		val conHandle = openSession()
		dynCtx.put(TR03112Keys.SESSION_CON_HANDLE, HandlerUtils.copyHandle(conHandle))

		val wsPair = WsPair.withNewListener(ws, successorListener)
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

		// no error means success
		val bindingResult = BindingResult(BindingResultCode.OK)
		bindingResult.addParameter(CardLinkKeys.CARD_SESSION_ID, cardSessionId)
		bindingResult.addParameter(CardLinkKeys.ICCSN, iccsn)
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
		val closeSession = DestroySession().apply {
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
	private fun performDidAuth(conHandle: ConnectionHandleType, dynCtx: DynamicContext): ConnectionHandleType {
		// Perform a DIDAuthenticate to authenticate the user
		val didAuth = DIDAuthenticate().apply {
			connectionHandle = conHandle
			authenticationProtocolData = DIDAuthenticationDataType().apply {
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

	private fun handleRemoteApdus(cardHandle: ConnectionHandleType, wsPair: WsPair) {
		val wsListener = wsPair.listener

		while (wsListener.isOpen()) {
			val gematikMessage: GematikEnvelope? = wsListener.nextMessageBlocking()

			if (gematikMessage == null) {
				val errorMsg = "Timeout happened during APDU exchange with CardLink-Service."
				logger.warn { errorMsg }
				throw WSHelper.makeResultError(ECardConstants.Minor.Disp.TIMEOUT, errorMsg).toException()
			}

			if (gematikMessage.payload is RegisterEgkFinish) {
				logger.debug { "Received '${REGISTER_EGK_FINISH}' message from CardLink service." }
				// replace listener with the provided successor, so the application can continue
				wsPair.switchToSuccessorListener()
				return
			}

			if (gematikMessage.payload is ICCSNReassignment) {
				logger.debug { "Received '${ICCSN_REASSIGNMENT}' message from CardLink service." }
				// TODO: do interaction call here to inform User about received ICCSN reassignment message
				continue
			}

			if (gematikMessage.payload is TasklistErrorPayload) {
				val errorMsg = gematikMessage.payload.errormessage ?: "Received an unknown error from CardLink service."
				val displayError = "$errorMsg (Status ${gematikMessage.payload.status})"
				logger.warn { "Received '${TASK_LIST_ERROR}': $displayError" }

				val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
				dynCtx.put(CardLinkKeys.ERROR_CODE, gematikMessage.payload.status.toString())
				dynCtx.put(CardLinkKeys.ERROR_MESSAGE, errorMsg)

				throw WSHelper.makeResultError(ECardConstants.Minor.Disp.COMM_ERROR, displayError).toException()
			}

			if (gematikMessage.cardSessionId == null || gematikMessage.correlationId == null) {
				val errorMsg = "Received malformed SendAPDU message which does not contain a cardSessionId or correlationId."
				logger.warn { errorMsg }
				continue
			}

			if (gematikMessage.payload !is SendApdu) {
				val errorMsg = "Received malformed eGK payload. Payload is not from type: SendApdu."
				logger.error { errorMsg }
			} else {
				val apdu = gematikMessage.payload.apdu
				val correlationId = gematikMessage.correlationId
				val cardSessionId = gematikMessage.cardSessionId

				val apduResponse = sendApduToCard(cardHandle, apdu)

				val egkEnvelope = GematikEnvelope(
					SendApduResponse(
						cardSessionId,
						apduResponse
					),
					correlationId,
					cardSessionId,
				)
				val egkEnvelopeJson = cardLinkJsonFormatter.encodeToString(egkEnvelope)
				ws.send(egkEnvelopeJson)
			}
		}
	}

	private fun sendApduToCard(cardHandle: ConnectionHandleType, apdu: ByteArray) : ByteArray {
		val inputAPDU = InputAPDUInfoType()
		inputAPDU.inputAPDU = apdu

		val t = Transmit()
		t.slotHandle = cardHandle.slotHandle
		t.inputAPDUInfo.add(inputAPDU)

		val response = dispatcher.safeDeliver(t) as TransmitResponse
		WSHelper.checkResult(response)
		return response.outputAPDU[0]
	}

	private fun waitForSessionInformation(dynCtx: DynamicContext, wsPair: WsPair) {
		val wsListener = wsPair.listener
		runBlocking {
			val sessionInformation = wsListener.nextMessage()
			val payload = sessionInformation?.payload

			if (sessionInformation == null) {
				val errorMsg = "Timeout happened during waiting for '${SESSION_INFO}' message from CardLink-Service."
				logger.warn { errorMsg }
			}

			if (payload != null && payload is SessionInformation) {
				dynCtx.put(CardLinkKeys.CARD_SESSION_ID, sessionInformation.cardSessionId)
				dynCtx.put(CardLinkKeys.WS_SESSION_ID, payload.webSocketId)
				dynCtx.put(CardLinkKeys.PHONE_NUMBER_REGISTERED, payload.phoneRegistered)
				logger.debug { "Using ${sessionInformation.cardSessionId} as cardSessionId and ${payload.webSocketId} as webSocketId." }
			} else {
				// we generate our own cardSessionId
				val cardSessionId = UUID.randomUUID().toString()
				dynCtx.put(CardLinkKeys.CARD_SESSION_ID, cardSessionId)
				dynCtx.put(CardLinkKeys.PHONE_NUMBER_REGISTERED, false)
				logger.debug { "Received no or a malformed SessionInformation message. Using $cardSessionId as cardSessionId." }
			}
		}
	}
}
