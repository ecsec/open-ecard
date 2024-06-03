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
 */

package org.openecard.addons.cardlink

import io.github.oshai.kotlinlogging.KotlinLogging
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.openecard.addons.cardlink.sal.CardLinkKeys
import org.openecard.addons.cardlink.ws.*
import org.openecard.binding.tctoken.TR03112Keys
import org.openecard.common.DynamicContext
import org.openecard.common.ifd.scio.TerminalFactory
import org.openecard.common.util.Promise
import org.openecard.mobile.activation.*
import org.openecard.mobile.activation.common.CommonActivationUtils
import org.openecard.mobile.activation.common.NFCDialogMsgSetter
import org.openecard.mobile.system.OpeneCardContextConfig
import org.openecard.scio.PCSCFactory
import org.openecard.ws.common.GenericInstanceProvider
import org.openecard.ws.jaxb.JAXBMarshaller
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.util.*


private val logger = KotlinLogging.logger {}

/**
 * @author Mike Prechtl
 */
class CardLinkProtocolTest {

	private lateinit var activationUtils: CommonActivationUtils
	private lateinit var webSocketMock: Websocket
	private lateinit var callbackController: ControllerCallback
	private lateinit var cardLinkInteraction: CardLinkInteraction

	private val activationResult: Promise<ActivationResult?> = Promise<ActivationResult?>()
	private val isContextInitialized: Promise<Boolean> = Promise<Boolean>()

	@BeforeClass
	fun setup() {
		val pcscFactory : GenericInstanceProvider<TerminalFactory?> = object : GenericInstanceProvider<TerminalFactory?> {
			override val instance = PCSCFactory()
		}

		val msgSetter = object : NFCDialogMsgSetter {
			override fun setText(msg: String) { }
			override fun isSupported(): Boolean { return false }
		}

		val nfcCapabilities = object : NFCCapabilities {
			override fun isAvailable(): Boolean { return true }
			override fun isEnabled(): Boolean { return true }
			override fun checkExtendedLength(): NfcCapabilityResult { return NfcCapabilityResult.SUPPORTED }
		}

		val startServiceHandler = object : StartServiceHandler {
			override fun onSuccess(source: ActivationSource?) {
				logger.info { "[ServiceHandler] onSuccess" }
				isContextInitialized.deliver(true)
			}
			override fun onFailure(response: ServiceErrorResponse?) {
				logger.info { "[ServiceHandler] onFailure: ${response?.errorMessage}" }
				isContextInitialized.deliver(false)
			}
		}

		val config = OpeneCardContextConfig(pcscFactory, JAXBMarshaller::class.java.getCanonicalName())
		activationUtils = CommonActivationUtils(config, msgSetter)

		val contextManager = activationUtils.context(nfcCapabilities)
		contextManager.initializeContext(startServiceHandler)

		isContextInitialized.deref()
	}

	@BeforeClass
	fun setupWebsocketMock() {
		this.webSocketMock = Mockito.mock(Websocket::class.java)
		val correlationIdTan = UUID.randomUUID().toString()
		val cardSessionId = UUID.randomUUID().toString()
		val argumentCaptor = ArgumentCaptor.forClass(WebsocketListener::class.java)

		Mockito.`when`(webSocketMock.connect()).then {
			logger.info { "[WS-MOCK] Websocket connect was called with cardSessionId: $cardSessionId." }
		}

		Mockito.`when`(webSocketMock.setListener(argumentCaptor.capture())).then {
			logger.info { "[WS-MOCK] Websocket-Listener was provided." }
			argumentCaptor.value.onOpen(webSocketMock)
			argumentCaptor.value.onText(webSocketMock, """
				[
					{
						"type":"$SESSION_INFO",
						"payload":"eyAid2ViU29ja2V0SWQiOiAiMTIzNDU2IiwgInBob25lUmVnaXN0ZXJlZCI6IGZhbHNlIH0"
					},
					"$cardSessionId"
				]
			""")
		}

		Mockito.`when`(webSocketMock.send(Mockito.contains(REQUEST_SMS_TAN))).then {
			logger.info { "[WS-MOCK] Received $REQUEST_SMS_TAN_RESPONSE message from App:\n${it.arguments[0]}" }
			argumentCaptor.value.onText(webSocketMock, """
				[
					{
						"type":"$REQUEST_SMS_TAN_RESPONSE",
						"payload":"eyJtaW5vciI6bnVsbCwiZXJyb3JNZXNzYWdlIjpudWxsfQ"
					},
					"$cardSessionId",
					"$correlationIdTan"
				]
			""")
		}

		Mockito.`when`(webSocketMock.send(Mockito.contains(CONFIRM_TAN))).then {
			logger.info { "[WS-MOCK] Received $CONFIRM_TAN message from App:\n${it.arguments[0]}" }
			argumentCaptor.value.onText(webSocketMock, """
				[
					{
						"type":"$CONFIRM_TAN_RESPONSE",
						"payload":"eyJtaW5vciI6bnVsbCwiZXJyb3JNZXNzYWdlIjpudWxsfQ"
					},
					"$cardSessionId",
					"$correlationIdTan"
				]
			""")
		}

		Mockito.`when`(webSocketMock.send(Mockito.contains(REGISTER_EGK))).then {
			logger.info { "[WS-MOCK] Received $REGISTER_EGK message from App:\n${it.arguments[0]}" }
			argumentCaptor.value.onText(webSocketMock, """
				[
					{
						"type":"$SEND_APDU",
						"payload":"eyAiY2FyZFNlc3Npb25JZCI6ICI2MjU5MDQ4ZS1lMjFmLTRlODYtOGZjNS00NTNmMGEwYTVjNjQiLCAiYXBkdSI6ICJBS1FBREFJL0FBIiB9IA"
					},
					"$cardSessionId",
					"$correlationIdTan"
				]
			""")
		}

		Mockito.`when`(webSocketMock.send(Mockito.contains(SEND_APDU_RESPONSE))).then {
			logger.info { "[WS-MOCK] Received $SEND_APDU_RESPONSE message from App:\n${it.arguments[0]}" }
			argumentCaptor.value.onText(webSocketMock, """
				[
					{
						"type":"$REGISTER_EGK_FINISH",
						"payload":"eyAicmVtb3ZlQ2FyZCI6IHRydWUgfQ"
					},
					"$cardSessionId",
					"$correlationIdTan"
				]
			""")
		}
	}

	@BeforeClass
	fun setupCallbackController() {
		this.callbackController = object : ControllerCallback {
			override fun onStarted() {
				logger.info { "Authentication started." }
			}

			override fun onAuthenticationCompletion(result: ActivationResult?) {
				logger.info { "Authentication completed." }
				activationResult.deliver(result)
			}
		}
	}

	@BeforeClass
	fun setupCardLinkInteraction() {
		this.cardLinkInteraction = object : CardLinkInteraction {
			override fun requestCardInsertion() { logger.info { "requestCardInsertion" } }
			override fun requestCardInsertion(msgHandler: NFCOverlayMessageHandler) { logger.info { "requestCardInsertion" } }
			override fun onCardInteractionComplete() { logger.info { "onCardInteractionComplete" } }
			override fun onCardRecognized() { logger.info { "onCardRecognized" } }
			override fun onCardRemoved() { logger.info { "onCardRemoved" } }
			override fun onCanRequest(enterCan: ConfirmPasswordOperation) {
				logger.info { "onCanRequest" }
				enterCan.confirmPassword("123456")
			}
			override fun onPhoneNumberRequest(enterPhoneNumber: ConfirmTextOperation) {
				logger.info { "onPhoneNumberRequest" }
				enterPhoneNumber.confirmText("+491517264234")
			}
			override fun onSmsCodeRequest(smsCode: ConfirmPasswordOperation) {
				logger.info { "onSmsCodeRequest" }
				smsCode.confirmPassword("123456")
			}
		}
	}

	@Test
	fun testCardLinkProtocol() {
		val cardLinkFactory = activationUtils.cardLinkFactory()
		cardLinkFactory.create(webSocketMock, callbackController, cardLinkInteraction)

		val result = activationResult.deref()
		Assert.assertNotNull(result)
		Assert.assertEquals(result?.resultCode, ActivationResultCode.OK)

		Mockito.verify(callbackController, Mockito.times(1)).onStarted()
	}
}
