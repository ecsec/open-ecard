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
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.Initialize
import iso.std.iso_iec._24727.tech.schema.ListIFDs
import org.mockito.Mockito
import org.openecard.addon.AddonManager
import org.openecard.common.ClientEnv
import org.openecard.common.WSHelper
import org.openecard.common.event.EventDispatcherImpl
import org.openecard.common.ifd.scio.TerminalFactory
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.common.sal.CombinedCIFProvider
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.Promise
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.ViewController
import org.openecard.ifd.scio.IFD
import org.openecard.management.TinyManagement
import org.openecard.mobile.activation.*
import org.openecard.mobile.activation.common.CommonActivationUtils
import org.openecard.mobile.activation.common.NFCDialogMsgSetter
import org.openecard.mobile.system.OpeneCardContextConfig
import org.openecard.mobile.ui.*
import org.openecard.recognition.CardRecognitionImpl
import org.openecard.recognition.RepoCifProvider
import org.openecard.sal.TinySAL
import org.openecard.scio.PCSCFactory
import org.openecard.transport.dispatcher.MessageDispatcher
import org.openecard.ws.common.GenericInstanceProvider
import org.openecard.ws.jaxb.JAXBMarshaller
import org.testng.Assert
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test


private val logger = KotlinLogging.logger {}

/**
 * @author Mike Prechtl
 */
class CardlinkProtocolTest {

	private lateinit var env: ClientEnv

	private lateinit var activationUtils: CommonActivationUtils

	private val promise: Promise<ActivationResult?> = Promise<ActivationResult?>()

	@BeforeClass
	fun setup() {
		env = ClientEnv()

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

		val eventDispatcher = EventDispatcherImpl()
		eventDispatcher.start()

		val vc: ViewController = Mockito.mock(ViewController::class.java)
		val d: Dispatcher = MessageDispatcher(env)
		val tinyManagement = TinyManagement(env)
		val uc : UserConsent = createUserConsent(d, msgSetter, eventDispatcher)
		val mainSAL = TinySAL(env)

		val ifd = IFD()
		ifd.setEnvironment(env)
		//ifd.addProtocol(ECardConstants.Protocol.PACE, PACEProtocolFactory())

		env.gui = uc
		env.dispatcher = d
		env.management = tinyManagement
		env.eventDispatcher = eventDispatcher
		env.ifd = ifd
		env.sal = mainSAL

		val establishContext = EstablishContext()
		val ecr = ifd.establishContext(establishContext)
		logger.info { "Established context." }

		val cr = CardRecognitionImpl(env)
		env.recognition = cr

		WSHelper.checkResult(ecr)
		val contextHandle = ecr.contextHandle
		logger.info { "ContextHandle: ${ByteUtils.toHexString(contextHandle)}" }
		mainSAL.setIfdCtx(contextHandle)

		val listIFDs = ListIFDs()
		listIFDs.contextHandle = ecr.contextHandle

		val cp = CombinedCIFProvider()
		cp.addCifProvider(RepoCifProvider(cr))
		env.cifProvider = cp

		val addonManager = AddonManager(env, vc, mainSAL.salStateView)
		mainSAL.setAddonManager(addonManager)

		addonManager.registry.listAddons();

		WSHelper.checkResult(mainSAL.initialize(Initialize()))
	}

	private fun createUserConsent(dispatcher: Dispatcher, msgSetter: NFCDialogMsgSetter, eventDispatcher: EventDispatcher): CompositeUserConsent {
		val eacNavFac = EacNavigatorFactory.create(msgSetter, dispatcher)
		val insertFac = InsertCardNavigatorFactory(msgSetter)
		val pinMngFac = PINManagementNavigatorFactory(dispatcher, eventDispatcher)
		val cardLinkNavFac = CardLinkNavigatorFactory.create(msgSetter, dispatcher)

		val allFactories = listOf(eacNavFac, insertFac, pinMngFac, cardLinkNavFac)

		return CompositeUserConsent(
			allFactories,
			MessageDialogStub()
		)
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
			override fun requestCardInsertion(msgHandler: NFCOverlayMessageHandler?) { logger.info { "requestCardInsertion" } }
			override fun onCardInteractionComplete() { logger.info { "onCardInteractionComplete" } }
			override fun onCardRecognized() { logger.info { "onCardRecognized" } }
			override fun onCardRemoved() { logger.info { "onCardRemoved" } }
			override fun onCanRequest(enterCan: ConfirmPasswordOperation?) { logger.info { "onCanRequest" } }
			override fun onPhoneNumberRequest(enterPhoneNumber: ConfirmTextOperation?) { logger.info { "onPhoneNumberRequest" } }
			override fun onSmsCodeRequest(smsCode: ConfirmPasswordOperation?) { logger.info { "onSmsCodeRequest" } }
		}

		val cardLinkFactory = activationUtils.cardLinkFactory()
		cardLinkFactory.create(webSocketMock, callbackController, cardlinkInteraction)

		val activationResult = promise.deref()
		Assert.assertNotNull(activationResult)
		Assert.assertEquals(activationResult?.resultCode, ActivationResultCode.OK)

		Mockito.verify(callbackController, Mockito.times(1)).onStarted()
	}
}
