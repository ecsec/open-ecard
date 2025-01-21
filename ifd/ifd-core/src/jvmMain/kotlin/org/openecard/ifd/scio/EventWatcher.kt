/****************************************************************************
 * Copyright (C) 2015-2018 ecsec GmbH.
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
package org.openecard.ifd.scio

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.ChannelHandleType
import iso.std.iso_iec._24727.tech.schema.IFDStatusType
import iso.std.iso_iec._24727.tech.schema.SlotStatusType
import org.openecard.common.ifd.scio.NoSuchTerminal
import org.openecard.common.ifd.scio.SCIOException
import org.openecard.common.ifd.scio.TerminalState
import org.openecard.common.ifd.scio.TerminalWatcher
import org.openecard.ifd.scio.wrapper.ChannelManager
import java.math.BigInteger
import java.util.concurrent.Callable
import javax.annotation.Nonnull
import kotlin.collections.MutableList
import kotlin.collections.indices

private val LOG = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class EventWatcher(
	private val cm: ChannelManager,
	private val timeout: Long,
	private val callback: ChannelHandleType?
) : Callable<List<IFDStatusType>> {
	private val watcher: TerminalWatcher = cm.terminals.watcher

	private var currentState: MutableList<IFDStatusType>? = null
	private var expectedState: MutableList<IFDStatusType>? = null

	@Throws(SCIOException::class)
	fun start(): List<IFDStatusType> {
		val initialState = watcher.start()
		currentState = convert(initialState)
		// convert again to be safe from manipulation from the outside
		return convert(initialState)
	}

	fun setExpectedState(expectedState: MutableList<IFDStatusType>) {
		this.expectedState = expectedState
	}

	val isAsync: Boolean
		get() = callback != null

	@Throws(SCIOException::class)
	override fun call(): MutableList<IFDStatusType> {
		// compare expected status and see if we are already finished
		var diff = compare(expectedState!!)
		if (!diff.isEmpty()) {
			return diff
		}

		// nothing happened so far, update our state
		val events = mutableListOf<TerminalWatcher.StateChangeEvent>()
		var event = watcher.waitForChange(timeout)
		if (!event.isCancelled) {
			do {
				events.add(event)
				// wait minimum amount of time to collect all remaining events
				event = watcher.waitForChange(1)
			} while (!event.isCancelled)
		}
		// update internal state according to all events which have occurred
		for (next in events) {
			updateState(next)
		}

		// compare again
		diff = compare(expectedState!!)

		// TODO: implement callbacks
		return diff
	}


	private fun updateState(event: TerminalWatcher.StateChangeEvent) {
		val name = event.getTerminal()
		if (event.state == TerminalWatcher.EventType.TERMINAL_ADDED) {
			currentState!!.add(createEmptyState(name))
		} else {
			val it = currentState!!.iterator()
			while (it.hasNext()) {
				val next = it.next()
				val slot = next.getSlotStatus()[0]
				if (next.getIFDName() == name) {
					when (event.state) {
						TerminalWatcher.EventType.CARD_INSERTED -> try {
							val ch = cm.openMasterChannel(name)
							slot.isCardAvailable = true
							slot.setATRorATS(ch.channel.card.aTR.bytes)
						} catch (ex: NoSuchTerminal) {
							LOG.error(ex) { "Failed to open master channel for terminal '${name}'." }
							slot.isCardAvailable = false
							cm.closeMasterChannel(name)
						} catch (ex: SCIOException) {
							LOG.error(ex) { "Failed to open master channel for terminal '${name}'." }
							slot.isCardAvailable = false
							cm.closeMasterChannel(name)
						}

						TerminalWatcher.EventType.CARD_REMOVED -> {
							cm.closeMasterChannel(name)
							slot.isCardAvailable = false
						}

						TerminalWatcher.EventType.TERMINAL_REMOVED -> {
							slot.isCardAvailable = false // just in case
							next.isConnected = false
						}

						else -> {
							LOG.error { "Unexpected event type: ${event.state}" }
							throw IllegalStateException("Unexpected event type: " + event.state)
						}
					}
					// no need to look any further
					break
				}
			}
		}
	}

	/**
	 * Compares the current status against the expected status and returns the difference if any.
	 * This function does not request new information from the hardware, but only uses the last state retrieved. The
	 * result of this function can be used as events in
	 * [org.openecard.ws.IFD.wait].
	 *
	 * @param expectedStatus Status known to the caller of the function.
	 * @return The difference between the internal state of this object and the given reference status.
	 */
	@Nonnull
	fun compare(expectedStatus: MutableList<IFDStatusType>): MutableList<IFDStatusType> {
		val remaining = currentState!!.toMutableList()

		for (nextExpect in expectedStatus) {
			val it = remaining.iterator()
			var matchFound = false
			// see if the current state contains the terminal that is expected to be present
			while (it.hasNext()) {
				val nextRemain = it.next()
				// found matching terminal
				if (nextRemain.getIFDName() == nextExpect.getIFDName()) {
					matchFound = true
					// see if there is any difference between the two
					if (EventWatcher.Companion.isStateEqual(nextRemain, nextExpect)) {
						// no difference, so delete this entry
						it.remove()
					}
					break
				}
			}
			// if remaining does not contain the expected status, the terminal was removed
			if (!matchFound) {
				val removed: IFDStatusType = EventWatcher.Companion.clone(nextExpect)
				removed.setIFDName(nextExpect.getIFDName())
				removed.isConnected = false
				remaining.add(removed)
			}
		}

		// clone entries, to prevent altering the state of this object from the outside
		return clone(remaining)
	}


	@Nonnull
	private fun convert(terminals: List<TerminalState>): MutableList<IFDStatusType> {
		val result = mutableListOf<IFDStatusType>()
		for (next in terminals) {
			result.add(convert(next))
		}
		return result
	}

	@Nonnull
	private fun convert(next: TerminalState): IFDStatusType {
		val result = IFDStatusType()
		result.setIFDName(next.name)
		result.isConnected = true
		val slot = SlotStatusType()
		result.getSlotStatus().add(slot)
		slot.setIndex(BigInteger.ZERO)
		slot.isCardAvailable = next.isCardPresent
		return result
	}

	companion object {
		private fun clone(orig: MutableList<IFDStatusType>): MutableList<IFDStatusType> {
			val result = mutableListOf<IFDStatusType>()
			for (next in orig) {
				result.add(clone(next))
			}
			return result
		}

		private fun clone(orig: IFDStatusType): IFDStatusType {
			val newStat = IFDStatusType()
			newStat.setIFDName(orig.getIFDName())
			newStat.isConnected = orig.isConnected

			for (next in orig.getSlotStatus()) {
				newStat.getSlotStatus().add(clone(next))
			}

			return newStat
		}

		private fun clone(orig: SlotStatusType): SlotStatusType {
			val slot = SlotStatusType()
			slot.isCardAvailable = orig.isCardAvailable
			slot.setIndex(orig.getIndex())
			val atr = orig.getATRorATS()
			if (atr != null) {
				slot.setATRorATS(atr.clone())
			}
			return slot
		}

		private fun createEmptyState(name: String): IFDStatusType {
			val status = IFDStatusType()
			status.setIFDName(name)
			status.isConnected = true
			status.getSlotStatus().add(createEmptySlot())
			return status
		}

		private fun createEmptySlot(): SlotStatusType {
			val slot = SlotStatusType()
			slot.isCardAvailable = false
			slot.setIndex(BigInteger.ZERO)
			return slot
		}

		private fun isStateEqual(a: IFDStatusType, b: IFDStatusType): Boolean {
			if (a.getIFDName() != b.getIFDName()) {
				return false
			}
			if (a.isConnected != b.isConnected) {
				return false
			}
			val sa = a.getSlotStatus()
			val sb = b.getSlotStatus()
			if (sa.size != sb.size) {
				return false
			}
			for (i in sa.indices) {
				if (!isSlotEqual(sa[i], sb[i])) {
					return false
				}
			}

			return true
		}

		private fun isSlotEqual(a: SlotStatusType, b: SlotStatusType): Boolean {
			if (a.isCardAvailable != b.isCardAvailable) {
				return false
			}
			if (a.getIndex() != b.getIndex()) {
				return false
			}
			// ATR is ignored, because it is not read by the conversion function
			return true
			//	if (a.getATRorATS() == null && b.getATRorATS() == null) {
//	    return true;
//	} else {
//	    // this method returns false when both are null, thatswhy the if before
//	    return ByteUtils.compare(a.getATRorATS(), b.getATRorATS());
//	}
		}
	}
}
