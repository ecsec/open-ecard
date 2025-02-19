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
import kotlinx.serialization.encodeToString
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.openecard.addons.cardlink.ws.*
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
import java.util.concurrent.TimeUnit
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

const val wrongCan = "1231234"
const val correctCan = "123123"

@OptIn(ExperimentalEncodingApi::class)
fun String.toB64() : String {
	return Base64.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).encode(this.toByteArray())
}

/**
 * @author Mike Prechtl
 */
@Test(groups = ["interactive"])
class CardLinkProtocolTest {

	private lateinit var activationUtils: CommonActivationUtils
	private lateinit var callbackController: ControllerCallback
	private lateinit var cardLinkInteraction: CardLinkInteraction

	private fun setupContext(isContextInitialized: Promise<Boolean>) {
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

	@OptIn(ExperimentalStdlibApi::class)
	private fun setupWebsocketMock(answerWithError: Boolean = false, websocketListenerHash: Promise<Int>): Websocket {
		val webSocketMock = Mockito.mock(Websocket::class.java)
		val correlationIdTan = UUID.randomUUID().toString()
		val correlationIdMseApdu = UUID.randomUUID().toString()
		val cardSessionId = UUID.randomUUID().toString()
		val argumentCaptor = ArgumentCaptor.forClass(WebsocketListener::class.java)

		Mockito.`when`(webSocketMock.getUrl()).then {
			"ws://localhost:8080/"
		}

		Mockito.`when`(webSocketMock.connect()).then {
			logger.info { "[WS-MOCK] Websocket connect was called with cardSessionId: $cardSessionId." }
			argumentCaptor.value.onOpen(webSocketMock)
			/* Use payload eyAid2ViU29ja2V0SWQiOiAiMTIzNDU2IiwgInBob25lUmVnaXN0ZXJlZCI6IHRydWUgfQ for registered phone */
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

		Mockito.`when`(webSocketMock.setListener(argumentCaptor.capture())).then {
			when (val l = argumentCaptor.value) {
				is WebsocketListenerImpl -> logger.info { "[WS-MOCK] OpeneCard-Internal Websocket-Listener was provided." }
				else -> {
					logger.info { "[WS-MOCK] An alternative Websocket-Listener was provided." }
					websocketListenerHash.deliver(l.hashCode())
				}
			}
		}

		Mockito.`when`(webSocketMock.send(Mockito.contains(REQUEST_SMS_TAN))).then {
			logger.info { "[WS-MOCK] Received $REQUEST_SMS_TAN_RESPONSE message from App:\n${it.arguments[0]}" }
			argumentCaptor.value.onText(webSocketMock, """
				[
					{
						"type":"$REQUEST_SMS_TAN_RESPONSE",
						"payload":"eyJyZXN1bHRDb2RlIjoiU1VDQ0VTUyIsImVycm9yTWVzc2FnZSI6bnVsbH0"
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
						"payload":"${
							if(answerWithError) {
								"""{"resultCode":"UNKNOWN_ERROR","errorMessage":""}""".toB64()
							} else {
								"""{"resultCode":"SUCCESS","errorMessage":null}""".toB64()
							}
						}"
					},
					"$cardSessionId",
					"$correlationIdTan"
				]
			""")
		}

		Mockito.`when`(webSocketMock.send(Mockito.contains(REGISTER_EGK))).then {
			logger.info { "[WS-MOCK] Received $REGISTER_EGK message from App:\n${it.arguments[0]}" }

			val mseApdu = "002241A406840109800100".hexToByteArray()
			val mseMessage = GematikEnvelope(
				SendApdu(cardSessionId, mseApdu),
				correlationIdMseApdu,
				cardSessionId,
			)

			argumentCaptor.value.onText(webSocketMock, cardLinkJsonFormatter.encodeToString(mseMessage))
		}

		Mockito.`when`(webSocketMock.send(Mockito.contains(SEND_APDU_RESPONSE))).then {
			val sendApduResponse = it.arguments[0] as String

			logger.info { "[WS-MOCK] Received $SEND_APDU_RESPONSE message from App:\n${sendApduResponse}" }

			if (sendApduResponse.contains(correlationIdMseApdu)) {
				logger.info { "[WS-MOCK] Received sendAPDUResponse for MSE message in CardLink-Mock." }
				val randomBytes = Random.nextBytes(32).toHexString()
				val internalAuthApdu = "0088000020${randomBytes}00".hexToByteArray()
				val internalAuthMessage = GematikEnvelope(
					SendApdu(cardSessionId, internalAuthApdu),
					UUID.randomUUID().toString(),
					cardSessionId,
				)
				argumentCaptor.value.onText(webSocketMock, cardLinkJsonFormatter.encodeToString(internalAuthMessage))
			} else {
				logger.info { "[WS-MOCK] Received sendAPDUResponse for Internal Authenticate in CardLink-Mock." }
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
		return webSocketMock
	}

	private fun setupCallbackController(activationResult: Promise<ActivationResult>) {
		this.callbackController = Mockito.mock(ControllerCallback::class.java)

		Mockito.`when`(callbackController.onStarted()).then {
			logger.info { "Authentication started." }
		}

		Mockito.`when`(callbackController.onAuthenticationCompletion(Mockito.any())).then {
			logger.info { "Authentication completed." }
			activationResult.deliver(it.arguments[0] as ActivationResult?)
		}
	}

	@BeforeClass
	fun setupCardLinkInteraction() {
		this.cardLinkInteraction = object : CardLinkInteraction {
			override fun requestCardInsertion() { logger.info { "requestCardInsertion" } }
			override fun requestCardInsertion(msgHandler: NFCOverlayMessageHandler) { logger.info { "requestCardInsertion" } }
			override fun onCardInteractionComplete() { logger.info { "onCardInteractionComplete" } }
			override fun onCardInserted() { logger.info { "onCardInserted" } }
			override fun onCardInsufficient() { logger.info { "onCardInsufficient" } }
			override fun onCardRecognized() { logger.info { "onCardRecognized" } }
			override fun onCardRemoved() { logger.info { "onCardRemoved" } }
			override fun onCanRequest(enterCan: ConfirmPasswordOperation) {
				logger.info { "onCanRequest" }
				enterCan.confirmPassword(wrongCan)
			}
			override fun onCanRetry(enterCan: ConfirmPasswordOperation, resultCode: String?, errorMessage: String?) {
				logger.info { "onCanRetry: $errorMessage (Status Code: $resultCode)" }
				enterCan.confirmPassword(correctCan)
			}

			override fun onPhoneNumberRequest(enterPhoneNumber: ConfirmTextOperation) {
				logger.info { "onPhoneNumberRequest" }
				enterPhoneNumber.confirmText("+491517264234")
			}
			override fun onSmsCodeRequest(smsCode: ConfirmPasswordOperation) {
				logger.info { "onSmsCodeRequest" }
				smsCode.confirmPassword("123456")
			}
			override fun onPhoneNumberRetry(
				enterPhoneNumber: ConfirmTextOperation,
				resultCode: String?,
				errorMessage: String?,
			) {
				logger.info { "onPhoneNumberRetry: $errorMessage (Status Code: $resultCode)" }
				enterPhoneNumber.confirmText("+491517264234")
			}

			override fun onSmsCodeRetry(
				smsCode: ConfirmPasswordOperation,
				resultCode: String?,
				errorMessage: String?,
			) {
				logger.info { "onSmsCodeRetry: $errorMessage (Status Code: $resultCode)" }
				smsCode.confirmPassword("123456")
			}

		}
	}

	@Test
	fun testCardLinkProtocol() {
		val activationResult: Promise<ActivationResult> = Promise()
		val isContextInitialized: Promise<Boolean> = Promise()
		val websocketListenerHash: Promise<Int> = Promise()

		setupContext(isContextInitialized)
		setupCallbackController(activationResult)
		val wsListener = setupWebsocketMock(false, websocketListenerHash)

		val webSocketListenerSuccessor = Mockito.mock(WebsocketListener::class.java)
		val cardLinkFactory = activationUtils.cardLinkFactory()
		cardLinkFactory.create(wsListener, callbackController, cardLinkInteraction, webSocketListenerSuccessor)

		val result = activationResult.deref()
		Assert.assertNotNull(result)
		Assert.assertEquals(result?.resultCode, ActivationResultCode.OK)

		Mockito.verify(callbackController, Mockito.times(1)).onAuthenticationCompletion(Mockito.any())
		Assert.assertEquals(websocketListenerHash.deref(), webSocketListenerSuccessor.hashCode())
	}

	@Test
	fun ensureWebsocketListenerSuccessorIsAlwaysSet() {
		val activationResult: Promise<ActivationResult> = Promise()
		val isContextInitialized: Promise<Boolean> = Promise()
		val websocketListenerHash: Promise<Int> = Promise()

		setupContext(isContextInitialized)
		setupCallbackController(activationResult)
		val wsListener = setupWebsocketMock(true, websocketListenerHash)

		val webSocketListenerSuccessor = Mockito.mock(WebsocketListener::class.java)
		val cardLinkFactory = activationUtils.cardLinkFactory()
		cardLinkFactory.create(wsListener , callbackController, cardLinkInteraction, webSocketListenerSuccessor)

		val result = activationResult.deref()
		Assert.assertNotNull(result)
		Assert.assertNotEquals(result?.resultCode, ActivationResultCode.OK)

		Mockito.verify(callbackController, Mockito.times(1)).onAuthenticationCompletion(Mockito.any())
		Assert.assertEquals(websocketListenerHash.deref(2, TimeUnit.SECONDS), webSocketListenerSuccessor.hashCode())
	}

}
