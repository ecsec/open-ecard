/****************************************************************************
 * Copyright (C) 2015-2019 ecsec GmbH.
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
package org.openecard.ifd.scio.wrapper

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.common.ECardConstants
import org.openecard.common.ifd.scio.NoSuchTerminal
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.ifd.scio.SCIOTerminals
import org.openecard.common.ifd.scio.TerminalFactory
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.Pair
import org.openecard.common.util.ValueGenerators.generateRandom
import java.util.TreeMap
import java.util.TreeSet

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 * @author Benedikt Biallowons
 */
class ChannelManager(
	private val termFact: TerminalFactory,
) {
	private val baseChannels: MutableMap<String, SingleThreadChannel> = mutableMapOf()
	private val handledChannels: MutableMap<ByteArray, SingleThreadChannel> = TreeMap(ByteArrayComparator())
	private val ifdNameToHandles: MutableMap<String, MutableSet<ByteArray>> = mutableMapOf()

	val terminals: SCIOTerminals
		get() = termFact.terminals()

	@Throws(SCIOException::class)
	fun prepareDevices(): Boolean = this.terminals.prepareDevices()

	fun powerDownDevices(): Boolean = this.terminals.powerDownDevices()

	@Synchronized
	@Throws(NoSuchTerminal::class, SCIOException::class)
	fun openMasterChannel(ifdName: String): SingleThreadChannel =
		baseChannels[ifdName]?.let {
			LOG.warn { "Terminal '$ifdName' is already connected." }
			it
		} ?: run {
			val t = this.terminals.getTerminal(ifdName)
			val ch = SingleThreadChannel(t)
			baseChannels.put(ifdName, ch)
			ifdNameToHandles.put(ifdName, TreeSet<ByteArray>(ByteArrayComparator()))
			ch
		}

	@Synchronized
	@Throws(NoSuchTerminal::class, SCIOException::class)
	fun openSlaveChannel(ifdName: String): Pair<ByteArray, SingleThreadChannel> {
		val baseCh = getMasterChannel(ifdName)
		val slaveCh = SingleThreadChannel(baseCh, true)
		val slotHandle = createSlotHandle()
		handledChannels.put(slotHandle, slaveCh)
		ifdNameToHandles[ifdName]!!.add(slotHandle)
		return Pair(slotHandle, slaveCh)
	}

	@Synchronized
	@Throws(NoSuchTerminal::class)
	fun getMasterChannel(ifdName: String): SingleThreadChannel {
		val ch = baseChannels[ifdName]
		if (ch == null) {
			throw NoSuchTerminal("No terminal with name '$ifdName' available.")
		} else {
			return ch
		}
	}

	@Synchronized
	@Throws(NoSuchChannel::class)
	fun getSlaveChannel(slotHandle: ByteArray): SingleThreadChannel {
		val ch = handledChannels[slotHandle]
		if (ch == null) {
			throw NoSuchChannel("No channel for slot '" + ByteUtils.toHexString(slotHandle) + "' available.")
		} else {
			return ch
		}
	}

	@Synchronized
	fun closeMasterChannel(ifdName: String?) {
		LOG.debug { "Closing MasterChannel" }
		val slotHandles = ifdNameToHandles[ifdName]
		if (slotHandles != null) {
			// iterate over copy of the list as the closeSlaveHandle call modifies the original slotHandles list
			for (slotHandle in slotHandles.toSet()) {
				try {
					closeSlaveChannel(slotHandle)
				} catch (ex: NoSuchChannel) {
					LOG.warn(ex) { "Failed to close channel for terminal '$ifdName'." }
				} catch (ex: SCIOException) {
					LOG.warn(ex) { "Failed to close channel for terminal '$ifdName'." }
				}
			}
			ifdNameToHandles.remove(ifdName)
		}

		val ch = baseChannels.remove(ifdName)
		if (ch == null) {
			LOG.warn { "No master channel for terminal '$ifdName' available." }
		} else {
			try {
				ch.shutdown()
			} catch (ex: SCIOException) {
				LOG.warn { "Failed to shut down master channel for terminal '$ifdName'." }
			}
		}
	}

	@Synchronized
	@Throws(NoSuchChannel::class, SCIOException::class)
	fun closeSlaveChannel(slotHandle: ByteArray) {
		LOG.debug { "Closing SlaveChannel" }
		val ch = handledChannels.remove(slotHandle)
		if (ch == null) {
			throw NoSuchChannel("No channel for slot '" + ByteUtils.toHexString(slotHandle) + "' available.")
		} else {
			val ifdName = ch.channel.card.terminal.name
			ifdNameToHandles[ifdName]!!.remove(slotHandle)
			ch.shutdown()
		}
	}

	companion object {
		fun createHandle(size: Int): ByteArray = generateRandom(size * 2)

		fun createSlotHandle(): ByteArray = createHandle(ECardConstants.SLOT_HANDLE_DEFAULT_SIZE)

		fun createCtxHandle(): ByteArray = createHandle(ECardConstants.CONTEXT_HANDLE_DEFAULT_SIZE)
	}
}
