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
import org.mockito.Mockito
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


private val logger = KotlinLogging.logger {}

/**
 * @author Mike Prechtl
 */
class CardLinkProtocolTest {

	private lateinit var activationUtils: CommonActivationUtils

	private val promise: Promise<ActivationResult?> = Promise<ActivationResult?>()

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
			override fun onSuccess(source: ActivationSource?) { logger.info { "[ServiceHandler] onSuccess" } }
			override fun onFailure(response: ServiceErrorResponse?) { logger.info { "[ServiceHandler] onFailure: ${response?.errorMessage}" } }
		}

		val config = OpeneCardContextConfig(pcscFactory, JAXBMarshaller::class.java.getCanonicalName())
		activationUtils = CommonActivationUtils(config, msgSetter)

		val contextManager = activationUtils.context(nfcCapabilities)
		contextManager.initializeContext(startServiceHandler)
	}

	@Test
	fun testCardLinkProtocol() {
		val webSocketMock = Mockito.mock(Websocket::class.java)

		val callbackController = object : ControllerCallback {
			override fun onStarted() {
				logger.info { "Authentication started." }
			}

			override fun onAuthenticationCompletion(result: ActivationResult?) {
				logger.info { "Authentication completed." }
				promise.deliver(result)
			}
		}

		val cardlinkInteraction = object : CardLinkInteraction {
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

		val cardLinkFactory = activationUtils.cardLinkFactory()
		cardLinkFactory.create(webSocketMock, callbackController, cardlinkInteraction)

		val activationResult = promise.deref()
		Assert.assertNotNull(activationResult)
		Assert.assertEquals(activationResult?.resultCode, ActivationResultCode.OK)

		Mockito.verify(callbackController, Mockito.times(1)).onStarted()
	}
}
