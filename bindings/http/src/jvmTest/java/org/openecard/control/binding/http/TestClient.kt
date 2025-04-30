/****************************************************************************
 * Copyright (C) 2012-2019 ecsec GmbH.
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
package org.openecard.control.binding.http

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.CardInfoType
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType
import iso.std.iso_iec._24727.tech.schema.EstablishContext
import org.openecard.common.ClientEnv
import org.openecard.common.event.EventDispatcherImpl
import org.openecard.common.interfaces.CIFProvider
import org.openecard.common.interfaces.EventDispatcher
import org.openecard.ifd.scio.IFD
import org.openecard.management.TinyManagement
import org.openecard.recognition.CardRecognitionImpl
import org.openecard.sal.TinySAL
import org.openecard.transport.dispatcher.MessageDispatcher
import java.io.InputStream

val LOG = KotlinLogging.logger {}

/**
 * Implements a TestClient to test the HttpBinding.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class TestClient {
	// Service Access Layer (SAL)
	private val sal: TinySAL? = null

	init {
		try {
			setup()
		} catch (e: Exception) {
			LOG.error(e) { "${e.message}" }
		}
	}

	@Throws(Exception::class)
	private fun setup() {
		// Set up client environment
		val env = ClientEnv()

		// Set up the IFD
		val ifd = IFD()
		env.ifd = ifd

		// Set up Management
		val management = TinyManagement(env)
		env.management = management

		// Set up the Dispatcher
		val dispatcher = MessageDispatcher(env)
		env.dispatcher = dispatcher

		// Perform an EstablishContext to get a ContextHandle
		val establishContext = EstablishContext()
		val establishContextResponse = ifd.establishContext(establishContext)

		val contextHandle = ifd.establishContext(establishContext).contextHandle

		val recognition = CardRecognitionImpl(env)
		env.recognition = recognition

		env.cifProvider =
			object : CIFProvider {
				override fun getCardInfo(
					type: ConnectionHandleType?,
					cardType: String,
				): CardInfoType? = recognition.getCardInfo(cardType)

				override fun needsRecognition(atr: ByteArray): Boolean = true

				@Throws(RuntimeException::class)
				override fun getCardInfo(cardType: String): CardInfoType? = recognition.getCardInfo(cardType)

				override fun getCardImage(cardType: String): InputStream? = recognition.getCardImage(cardType)
			}

		// Set up EventManager
		val ed: EventDispatcher = EventDispatcherImpl()
		env.eventDispatcher = ed

		// Set up SALStateCallback
		// TODO: fix tests
// 	cardStates = new CardStateMap();
// 	SALStateCallback salCallback = new SALStateCallback(env, cardStates);
// 	ed.add(salCallback);
//
// 	// Set up SAL
// 	sal = new TinySAL(env, cardStates);
// 	env.setSAL(sal);
//
// 	// Set up GUI
// 	SwingUserConsent gui = new SwingUserConsent(new SwingDialogWrapper());
// 	sal.setGUI(gui);
// 	ifd.setGUI(gui);
//
// 	// Initialize the EventManager
// 	ed.start();
//
// 	AddonManager manager = new AddonManager(env, gui, null);
// 	sal.setAddonManager(manager);
//
// 	HttpBinding binding = new HttpBinding(24727);
// 	binding.setAddonManager(manager);
// 	binding.start();
	}
}
