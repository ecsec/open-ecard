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

import iso.std.iso_iec._24727.tech.schema.EstablishContext
import iso.std.iso_iec._24727.tech.schema.Initialize
import iso.std.iso_iec._24727.tech.schema.ListIFDs
import org.mockito.Mockito
import org.openecard.addon.AddonManager
import org.openecard.common.ClientEnv
import org.openecard.common.WSHelper
import org.openecard.common.event.EventDispatcherImpl
import org.openecard.common.interfaces.Dispatcher
import org.openecard.common.sal.CombinedCIFProvider
import org.openecard.gui.UserConsent
import org.openecard.gui.definition.ViewController
import org.openecard.ifd.scio.IFD
import org.openecard.management.TinyManagement
import org.openecard.mobile.activation.ActivationInteraction
import org.openecard.mobile.activation.common.NFCDialogMsgSetter
import org.openecard.mobile.ui.*
import org.openecard.recognition.CardRecognitionImpl
import org.openecard.recognition.RepoCifProvider
import org.openecard.sal.TinySAL
import org.openecard.transport.dispatcher.MessageDispatcher
import org.testng.annotations.Test
import java.util.*


/**
 * @author Mike Prechtl
 */
class CardlinkProtocolTest {

	private lateinit var env: ClientEnv

	@Test
	fun testCardlinkProtocol() {
		env = ClientEnv()

		//val uc: UserConsent = Mockito.mock(UserConsent::class.java)
		val vc: ViewController = Mockito.mock(ViewController::class.java)
		val msgSetter: NFCDialogMsgSetter = Mockito.mock(NFCDialogMsgSetter::class.java)
		val d: Dispatcher = MessageDispatcher(env)
		val tinyManagement = TinyManagement(env)
		val uc : UserConsent = createUserConsent(d, msgSetter)
		val mainSAL = TinySAL(env)

		val eventDispatcher = EventDispatcherImpl()
		eventDispatcher.start()

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
		val cr = CardRecognitionImpl(env)
		env.recognition = cr

		WSHelper.checkResult(ecr)
		val contextHandle = ecr.contextHandle
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

	private fun createUserConsent(dispatcher: Dispatcher, msgSetter: NFCDialogMsgSetter): CompositeUserConsent {
		val realFactories = HashMap<String, UserConsentNavigatorFactory<out ActivationInteraction?>>()

		val cardLinkNavFac = CardLinkNavigatorFactory.create(msgSetter, dispatcher)
		realFactories[cardLinkNavFac.protocolType] = cardLinkNavFac

		val allFactories = listOf(cardLinkNavFac)

		return CompositeUserConsent(
			allFactories,
			MessageDialogStub()
		)
	}
}
