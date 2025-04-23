/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
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
import org.openecard.common.ifd.scio.SCIOCard
import org.openecard.common.ifd.scio.SCIOErrorCode
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.ifd.scio.SCIOProtocol
import org.openecard.common.ifd.scio.SCIOTerminal

private val LOG = KotlinLogging.logger { }

/**
 * NFC implementation of smartcardio's CardTerminal interface.
 * Implemented as singleton because we only have one nfc-interface.
 * Only activity's can react on a new intent, so they must set the tag via setTag()
 *
 * @author Dirk Petrautzki
 * @author Mike Prechtl
 * @param <T>
</T> */
abstract class NFCCardTerminal<T : AbstractNFCCard> : SCIOTerminal {
	@Volatile
	var nFCCard: T? = null
		private set

	override val name: String = STD_TERMINAL_NAME

	private val cardPresent: Object = Object()
	private val cardAbsent: Object = Object()
	protected val cardLock: Object = Object()

	@Throws(SCIOException::class)
	abstract fun prepareDevices(): Boolean

	fun powerDownDevices(): Boolean = this.setNFCCard(null)

	fun setDialogMsg(dialogMsg: String) {
		val currentCard = this.nFCCard
		currentCard?.setDialogMsg(dialogMsg)
	}

	override val isCardPresent: Boolean
		get() {
			synchronized(cardLock) {
				return this.nFCCard != null && !nFCCard!!.tagWasPresent()
			}
		}

	fun setNFCCard(card: T?): Boolean {
		synchronized(cardLock) {
			val oldCard = this.nFCCard
			if (oldCard != null) {
				try {
					oldCard.terminateTag()
				} catch (e: Exception) {
					LOG.warn(e) { "Exception occurred while cleaning up previous nfc card" }
				}
			}
			this.nFCCard = card
			if (card == null) {
				notifyCardAbsent()
			} else {
				notifyCardPresent()
			}
			return card !== oldCard
		}
	}

	fun removeTag(): Boolean {
		synchronized(cardLock) {
			val currentCard = this.nFCCard
			if (currentCard != null) {
				val changed = terminateTag(currentCard)
				notifyCardAbsent()
				return changed
			} else {
				return false
			}
		}
	}

	private fun terminateTag(currentCard: T?): Boolean {
		if (currentCard != null) { // maybe nfc tag is already removed
			LOG.info { "Removing NFC Tag and terminating card connection." }
			try {
				return currentCard.terminateTag()
			} catch (ex: SCIOException) {
				LOG.error(ex) { "Disconnect failed." }
				return true
			}
		} else {
			LOG.warn { "Double invocation of removeTag function." }
			return false
		}
	}

	protected fun notifyCardPresent() {
		synchronized(cardPresent) {
			cardPresent.notifyAll()
		}
	}

	protected fun notifyCardAbsent() {
		synchronized(cardAbsent) {
			cardAbsent.notifyAll()
		}
	}

	@Throws(SCIOException::class, IllegalStateException::class)
	override fun connect(protocol: SCIOProtocol): SCIOCard {
		synchronized(cardLock) {
			val currentCard: AbstractNFCCard? = this.nFCCard
			if (currentCard == null) {
				val msg = "No tag present."
				LOG.warn { msg }
				throw SCIOException(msg, SCIOErrorCode.SCARD_E_NO_SMARTCARD)
			}
			return currentCard
		}
	}

	@Throws(SCIOException::class)
	override fun waitForCardAbsent(timeout: Long): Boolean {
		val startTime = System.nanoTime() / 1000000
		val absent = !isCardPresent
		if (absent) {
			LOG.debug { "Card already absent..." }
			return true
		}
		LOG.debug { "Waiting for card absent..." }
		try {
			synchronized(cardAbsent) {
				while (isCardPresent) {
					// wait only if timeout is not finished
					val curTime = System.nanoTime() / 1000000
					val waitTime = timeout - (curTime - startTime)
					if (waitTime < 0) {
						break
					}
					cardAbsent.wait(timeout)
				}
			}
		} catch (ex: InterruptedException) {
			LOG.warn { "Waiting for card absent interrupted." }
		}
		val nowAbsent = !isCardPresent
		LOG.debug { "Notifying card is absent: $nowAbsent" }
		return nowAbsent
	}

	@Throws(SCIOException::class)
	override fun waitForCardPresent(timeout: Long): Boolean {
		val startTime = System.nanoTime() / 1000000
		val present = isCardPresent
		if (present) {
			LOG.debug { "Card already present..." }
			return true
		}
		LOG.debug { "Waiting for card present..." }
		try {
			synchronized(cardPresent) {
				while (!isCardPresent) {
					// wait only if timeout is not finished
					val curTime = System.nanoTime() / 1000000
					val waitTime = timeout - (curTime - startTime)
					if (waitTime < 0) {
						break
					}
					cardPresent.wait(timeout)
				}
			}
		} catch (ex: InterruptedException) {
			LOG.warn { "Waiting for card present interrupted." }
		}
		val nowPresent = isCardPresent
		LOG.debug { "${"Notifying card is present: {}"} $nowPresent" }
		return nowPresent
	}
}

const val STD_TERMINAL_NAME: String = "Integrated NFC"
