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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import org.openecard.addon.Context
import org.openecard.addon.bind.BindingResult
import org.openecard.addon.bind.BindingResultCode
import org.openecard.addons.cardlink.sal.*
import org.openecard.addons.cardlink.ws.*
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.WSHelper
import org.openecard.common.util.HandlerUtils
import org.openecard.mobile.activation.Websocket
import java.util.*


private val logger = KotlinLogging.logger {}

class CardLinkProcess constructor(private val ctx: Context, private val ws: Websocket) {

	private val dispatcher = ctx.dispatcher

    fun start(): BindingResult {
		val dynCtx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY)
		val conHandle = openSession()
		// TODO: For now we generate the cardSessionID here, should be moved to the CardLink-Service
		val cardSessionId = UUID.randomUUID().toString()
		dynCtx.put(TR03112Keys.SESSION_CON_HANDLE, HandlerUtils.copyHandle(conHandle))
		dynCtx.put(CardLinkKeys.WS_SESSION_ID, cardSessionId)
		setProcessWebsocket(dynCtx, ws)
		ws.connect(cardSessionId)
		prepareWebsocketListener(dynCtx)
		val cardHandle = performDidAuth(conHandle)
		handleRemoteApdus(cardHandle, dynCtx)
		destroySession(cardHandle)

		// no error means success
		ws.close(200, null)
        return BindingResult(BindingResultCode.OK)
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
		val closeSessionResp = dispatcher.safeDeliver(closeSession) as DestroySessionResponse

		// Check CloseSessionResponse
		WSHelper.checkResult(closeSessionResp)
	}

	@Throws(WSHelper.WSException::class)
	private fun performDidAuth(conHandle: ConnectionHandleType): ConnectionHandleType {
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

		//return didAuthResp.authenticationProtocolData
		TODO("Not yet implemented")
	}

	private fun handleRemoteApdus(cardHandle: ConnectionHandleType, dynCtx: DynamicContext) {
		val wsListener = getWebsocketListener(dynCtx) as WebsocketListenerImpl

		// TODO: currently, wait for APDUs until websocket channel is closed
		while (wsListener.isOpen() && isAPDUExchangeOngoing(wsListener)) {
			val sendApduMessage: EgkEnvelope? = waitForSendApduMessage(wsListener)

			if (sendApduMessage == null) {
				val errorMsg = "Didn't receive any SendAPDU messages from CardLink-Service."
				logger.warn { errorMsg }
				continue
			}

			if (sendApduMessage.payload !is SendApdu) {
				val errorMsg = "Received malformed eGK payload. Payload is not from type: SendApdu."
				logger.error { errorMsg }
			} else {
				val apdu = (sendApduMessage.payload as SendApdu).apdu
				val correlationId = sendApduMessage.correlationId
				val cardSessionId = sendApduMessage.cardSessionId

				val apduResponse = sendApduToCard(cardHandle, apdu)

				val egkEnvelope = EgkEnvelope(
					cardSessionId,
					correlationId,
					SendApduResponse(
						cardSessionId,
						apduResponse
					),
					SEND_APDU_RESPONSE
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

	private fun isAPDUExchangeOngoing(wsListener: WebsocketListenerImpl) : Boolean {
		var isAPDUExchangeOngoing: Boolean
		runBlocking {
			isAPDUExchangeOngoing = wsListener.isAPDUExchangeOngoing()
		}
		return isAPDUExchangeOngoing
	}

	private fun waitForSendApduMessage(wsListener: WebsocketListenerImpl) : EgkEnvelope? {
		var sendApduMessage: EgkEnvelope?
		runBlocking {
			sendApduMessage = wsListener.pollMessage(SEND_APDU)

			var pollTryCounter = 5
			while (sendApduMessage == null && pollTryCounter != 0) {
				delay(500)
				sendApduMessage = wsListener.pollMessage(SEND_APDU)
				pollTryCounter--
			}
		}
		return sendApduMessage
	}

}
