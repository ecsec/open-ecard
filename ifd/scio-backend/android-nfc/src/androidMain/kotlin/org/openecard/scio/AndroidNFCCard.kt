/** **************************************************************************
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

import android.nfc.TagLostException
import android.nfc.tech.IsoDep
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.ifd.scio.SCIOATR
import org.openecard.common.ifd.scio.SCIOErrorCode
import org.openecard.common.ifd.scio.SCIOException
import java.io.ByteArrayOutputStream
import java.io.IOException

private val LOG = KotlinLogging.logger { }

/**
 * NFC implementation of SCIO API card interface.
 *
 * @author Dirk Petrautzki
 */
class AndroidNFCCard(
	terminal: NFCCardTerminal<*>,
) : AbstractNFCCard(terminal) {
	private val connectLock: Object = Object()

	@kotlin.concurrent.Volatile
	private var transceiveTimeout = 0

	@kotlin.concurrent.Volatile
	private var histBytes: ByteArray? = null

	@kotlin.concurrent.Volatile
	private var isodep: IsoDep? = null

	@kotlin.concurrent.Volatile
	private var cardMonitor: NFCCardMonitoring? = null

	@kotlin.concurrent.Volatile
	private var tagPending = true

	@kotlin.concurrent.Volatile
	private var monitor: Thread? = null

	@kotlin.Throws(IOException::class)
	fun setTag(
		tag: IsoDep,
		timeout: Int,
	) {
		LOG.debug { "Assigning tag $tag with timeout $timeout" }
		kotlin.synchronized(connectLock) {
			isodep = tag
			transceiveTimeout = timeout

			var histBytesTmp = tag.historicalBytes
			if (histBytesTmp == null) {
				histBytesTmp = tag.hiLayerResponse
			}
			this.histBytes = histBytesTmp

			connectTag()

			tagPending = false
			connectLock.notifyAll()
			startCardMonitor()
		}
	}

	private fun startCardMonitor() {
		val createdMonitor = NFCCardMonitoring(terminal, this)
		val executionThread = Thread(createdMonitor)
		executionThread.start()
		this.monitor = executionThread
		this.cardMonitor = createdMonitor
	}

	@Throws(IOException::class)
	private fun connectTag() {
		this.isodep?.let { isodep ->
			isodep.connect()
			isodep.timeout = this.transceiveTimeout
		}
	}

	@Throws(SCIOException::class)
	override fun disconnect(reset: Boolean) {
		if (reset) {
			synchronized(connectLock) {
				val wasConnected = this.isTagPresent
				innerTerminateTag()
				if (wasConnected) {
					try {
						connectTag()

						startCardMonitor()
					} catch (ex: IOException) {
						LOG.error(ex) { "Failed to connect NFC tag." }
						throw SCIOException("Failed to reset channel.", SCIOErrorCode.SCARD_E_UNEXPECTED, ex)
					}
				}
			}
		}
	}

	override val isTagPresent: Boolean
		get() {
			try {
				val isodep = this.isodep
				val isTagPresent = !tagPending && isodep != null && isodep.isConnected
				return isTagPresent
			} catch (e: SecurityException) {
				return false
			}
		}

	override fun tagWasPresent(): Boolean {
		try {
			val isodep = this.isodep
			val tagWasPresent = !tagPending && (isodep == null || !isodep.isConnected)
			return tagWasPresent
		} catch (e: SecurityException) {
			return true
		}
	}

	@kotlin.Throws(SCIOException::class)
	override fun terminateTag(): Boolean {
		kotlin.synchronized(connectLock) {
			val changed = this.innerTerminateTag()
			this.isodep = null
			this.histBytes = null
			this.tagPending = false
			connectLock.notifyAll()
			return changed
		}
	}

	@Throws(SCIOException::class)
	fun innerTerminateTag(): Boolean {
		try {
			return this.terminateTag(this.monitor, this.cardMonitor)
		} catch (ex: IOException) {
			LOG.error { "Failed to close NFC tag." }
			throw SCIOException("Failed to close NFC channel.", SCIOErrorCode.SCARD_E_UNEXPECTED, ex)
		} finally {
			this.monitor = null
			this.cardMonitor = null
		}
	}

	@Throws(IOException::class)
	private fun terminateTag(
		monitor: Thread?,
		cardMonitor: NFCCardMonitoring?,
	): Boolean {
		synchronized(connectLock) {
			val isodep = this.isodep
			if (cardMonitor != null) {
				LOG.debug { "Killing the monitor" }
				cardMonitor.notifyStopMonitoring()
			}
			if (isodep != null) {
				LOG.debug { "Closing the tag" }
				try {
					val wasClosed: Boolean = isodep.isConnected
					isodep.close()
					return wasClosed
				} catch (e: SecurityException) {
					return false
				}
			} else {
				return false
			}
		}
	}

	override val aTR: SCIOATR
		get() {
			val currentHistBytes: ByteArray?
			kotlin.synchronized(connectLock) {
				var interrupted = false
				while (tagPending && !interrupted) {
					try {
						connectLock.wait()
					} catch (ex: InterruptedException) {
						interrupted = true
					}
				}
				if (interrupted) {
					currentHistBytes = null
				} else {
					currentHistBytes = this.histBytes
				}
			}

			// build ATR according to PCSCv2-3, Sec. 3.1.3.2.3.1
			if (currentHistBytes == null) {
				return SCIOATR(ByteArray(0))
			} else {
				val out: ByteArrayOutputStream = ByteArrayOutputStream()
				// Initial Header
				out.write(0x3B)
				// T0
				out.write(0x80 or (currentHistBytes.size and 0xF))
				// TD1
				out.write(0x80)
				// TD2
				out.write(0x01)
				// ISO14443A: The historical bytes from ATS response.
				// ISO14443B: 1-4=Application Data from ATQB, 5-7=Protocol Info Byte from ATQB, 8=Higher nibble = MBLI from ATTRIB command Lower nibble (RFU) = 0
				// TODO: check that the HiLayerResponse matches the requirements for ISO14443B
				out.write(currentHistBytes, 0, currentHistBytes.size)

				// TCK: Exclusive-OR of bytes T0 to Tk
				val preATR: ByteArray = out.toByteArray()
				var chkSum = 0
				for (i in 1..<preATR.size) {
					chkSum = chkSum.toInt() xor preATR[i].toInt()
				}
				out.write(chkSum)

				val atr = out.toByteArray()
				return SCIOATR(atr)
			}
		}

	@kotlin.Throws(IOException::class)
	override fun transceive(apdu: ByteArray): ByteArray {
		val cm = checkNotNull(cardMonitor)
		cm.notifyStartTranceiving()
		try {
			val currentTag =
				synchronized(connectLock) {
					while (tagPending) {
						try {
							connectLock.wait()
						} catch (ex: InterruptedException) {
							throw IOException(ex)
						}
					}
					isodep
				}
			checkNotNull(currentTag) { "Transmit of apdu command failed, because the tag is not present." }
			try {
				return currentTag.transceive(apdu)
			} catch (ex: TagLostException) {
				LOG.debug(ex) { "NFC Tag is not present." }
				this.terminal.removeTag()
				throw IllegalStateException("Transmit of apdu command failed, because the tag was lost.")
			}
		} finally {
			cm.notifyStopTranceiving()
		}
	}
}
