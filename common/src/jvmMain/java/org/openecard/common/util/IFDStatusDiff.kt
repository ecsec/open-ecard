/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
package org.openecard.common.util

import io.github.oshai.kotlinlogging.KotlinLogging
import iso.std.iso_iec._24727.tech.schema.IFDStatusType
import iso.std.iso_iec._24727.tech.schema.SlotStatusType
import java.util.LinkedList

private val logger = KotlinLogging.logger { }

/**
 *
 * @author Tobias Wich
 */
class IFDStatusDiff(
	private val expected: List<IFDStatusType>,
) {
	private var result: MutableList<IFDStatusType>? = null
	private var deleted: MutableList<IFDStatusType>? = null

	fun diff(
		others: List<IFDStatusType>,
		withNew: Boolean,
	) {
		result = LinkedList()
		deleted = LinkedList(expected)

		for (next in others) {
			val nextName = next.ifdName
			if (expectedContains(nextName)) {
				// check for terminal connection
				val other = expectedGet(nextName)
				val otherCard = other!!.isConnected
				val otherCardB = otherCard ?: false
				val thisCard = next.isConnected
				val thisCardB = thisCard ?: false
				if (thisCardB != otherCardB) {
					result!!.add(next)
				} else {
					// check for card insertions or removals
					if (other.slotStatus.size == next.slotStatus.size) {
						for (nextSlot in next.slotStatus) {
							val thisSlotIdx = nextSlot.index
							var otherSlot: SlotStatusType? = null
							for (nextOtherSlot in other.slotStatus) {
								if (thisSlotIdx == nextOtherSlot.index) {
									otherSlot = nextOtherSlot
									break
								}
							}
							// no equivalent slot, only occurs on evolving terminals ;-)
							if (otherSlot == null) {
								result!!.add(next)
								break
							}
							// compare card status of slot
							if (nextSlot.isCardAvailable != otherSlot.isCardAvailable) {
								result!!.add(next)
								break
							}
							// compare if there are different cards in slot
							val nextATR = nextSlot.atRorATS
							val otherATR = otherSlot.atRorATS
							if (!(nextATR == null && otherATR == null) && !(arrayEquals(nextATR, otherATR))) {
								result!!.add(next)
								break
							}
						}
					}
				}
				// delete card from deleted list
				deleted!!.remove(expectedGet(nextName))
			} else if (withNew) {
				result!!.add(next)
			}
		}

		merge(deleted!!)
	}

	private fun expectedGet(ifdName: String): IFDStatusType? {
		for (s in expected) {
			if (s.ifdName == ifdName) {
				return s
			}
		}
		return null
	}

	private fun expectedContains(ifdName: String): Boolean {
		val b = expectedGet(ifdName) != null
		return b
	}

	private fun merge(deleted: List<IFDStatusType>): List<IFDStatusType> {
		val states = ArrayList<IFDStatusType>(result!!.size + deleted.size)
		states.addAll(result!!)
		// transform all deleted terminals so it is clear they disappeared
		for (next in deleted) {
			val s = IFDStatusType()
			s.ifdName = next.ifdName
			s.isConnected = java.lang.Boolean.FALSE
			// slots must be present too
			for (nextST in next.slotStatus) {
				val st = SlotStatusType()
				st.index = nextST.index
				st.isCardAvailable = java.lang.Boolean.FALSE
				s.slotStatus.add(st)
			}
			// add new deleted terminal type to states list
			states.add(s)
		}
		this.result = states
		return states
	}

	fun hasChanges(): Boolean =
		if (this.result != null && !result!!.isEmpty()) {
			true
		} else {
			false
		}

	fun result(): List<IFDStatusType>? = result

	companion object {
		fun diff(
			expected: List<IFDStatusType>,
			others: List<IFDStatusType>,
			withNew: Boolean,
		): IFDStatusDiff {
			val diff = IFDStatusDiff(expected)
			diff.diff(others, withNew)
			return diff
		}

		private fun arrayEquals(
			a: ByteArray?,
			b: ByteArray?,
		): Boolean {
			if (a == null && b == null) {
				return false
			} else {
				val result = a.contentEquals(b)
				return result
			}
		}
	}
}
