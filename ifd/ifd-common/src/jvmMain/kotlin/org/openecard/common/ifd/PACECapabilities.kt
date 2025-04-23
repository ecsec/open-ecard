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
 ***************************************************************************/
package org.openecard.common.ifd

import org.openecard.common.ECardConstants
import java.util.BitSet

/**
 *
 * @author Tobias Wich
 */
class PACECapabilities(
	capabilitiesStructure: ByteArray,
) {
	/**
	 * PACE Capabilities as defined in PCSC-10 AMD1.1 p. 6
	 */
	enum class PACECapability(
		val number: Long,
	) {
		DestroyPACEChannel(0x80),
		GenericPACE(0x40), //
		GermanEID(0x20),
		QES(0x10),
		;

		val protocol: String = ECardConstants.Protocol.PACE + "." + number

		companion object {
			fun getCapability(number: Long): PACECapability? {
				for (next in PACECapability.entries) {
					if (next.number == number) {
						return next
					}
				}
				return null
			}
		}
	}

	private val capabilities: BitSet

	init {
		if (capabilitiesStructure.size == 1) {
			// special case for reiner sct readers
			capabilities = makeBitSet(capabilitiesStructure)
		} else {
			// standard way
			val length = capabilitiesStructure[0]
			val data = capabilitiesStructure.copyOfRange(1, length + 1)
			capabilities = makeBitSet(data)
		}
	}

	val features: MutableList<Long>
		get() {
			val result = mutableListOf<Long>()
			var i = capabilities.nextSetBit(0)
			while (i >= 0) {
				// operate on index i here
				result.add(1L shl i)
				i = capabilities.nextSetBit(i + 1)
			}
			return result
		}
	val featuresEnum: List<PACECapability>
		get() {
			val features = this.features
			val result = mutableListOf<PACECapability>()
			for (next in features) {
				PACECapability.Companion.getCapability(next)?.let { result.add(it) }
			}
			return result
		}

	private fun makeBitSet(d: ByteArray): BitSet {
		val b = BitSet(d.size * 8)
		for (i in d.indices) {
			val next = d[i]
			for (j in 0..7) {
				val isSet = ((next.toInt() shr j) and 0x01) == 1
				if (isSet) {
					b.set((i * 8) + j)
				}
			}
		}
		return b
	}
}
