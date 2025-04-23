/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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
package org.openecard.scio

import io.github.oshai.kotlinlogging.KotlinLogging

private val LOG = KotlinLogging.logger { }

/**
 * Task which checks if the received nfc tag is still available. If the nfc tag is no longer available, then the nfc tag
 * is removed and the card object is removed from the NFCCardTerminal, see
 * [NFCCardTerminal.removeTag].
 *
 * @author Mike Prechtl
 */
class NFCCardMonitoring(
	private val terminal: NFCCardTerminal<*>,
	private val card: AbstractNFCCard,
) : Runnable {
	private val lock = Object()

	@Volatile
	private var wasSignalled = false

	@Volatile
	private var isTranceiving = false

	override fun run() {
		LOG.debug { "Starting monitor thread." }

		synchronized(lock) {
			while (!wasSignalled) {
				try {
					if (!isTranceiving && !card.isTagPresent) {
						LOG.debug { "Detected card absence." }
						// remove tag if card is no longer available/connected to terminal
						terminal.removeTag()
						LOG.debug { "Stopping monitor thread due to card absence." }
						return
					}
					lock.wait(250)
				} catch (ex: InterruptedException) {
					LOG.warn(ex) { "Task which checks the availability of the nfc card is interrupted." }
					LOG.debug { "Stopping monitor thread due to interrupt." }
					return
				}
			}
		}
		LOG.debug { "Stopping monitor thread due to signalling." }
	}

	fun notifyStopMonitoring() {
		LOG.debug { "Notifying stop monitor thread." }
		synchronized(lock) {
			wasSignalled = true
			lock.notifyAll()
		}
	}

	fun notifyStartTranceiving() {
		synchronized(lock) {
			if (!isTranceiving) {
				isTranceiving = true
			} else {
				LOG.warn { "Received consecutive start tranceive notifications without stopping!" }
			}
		}
	}

	fun notifyStopTranceiving() {
		synchronized(lock) {
			if (isTranceiving) {
				isTranceiving = false
			} else {
				LOG.warn { "Received consecutive stop tranceive notifications without starting!" }
			}
		}
	}
}
