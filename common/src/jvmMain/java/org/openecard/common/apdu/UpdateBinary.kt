/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.common.apdu

import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException

/**
 * UPDATE BINARY command.
 * See ISO/IEC 7816-4 Section 7.2.5.
 *
 * @author Hans-Martin Haase
 */
class UpdateBinary : CardCommandAPDU {
	/**
	 * Creates a new UPDATE BINARY APDU.
	 * APDU: 0x00 0xD6 P1 P2 Lc Data
	 *
	 * @param p1 P1 according to ISO 7816 part 4 section 7.2.2.
	 * @param p2 P2 according to ISO 7816 part 4 section 7.2.2.
	 * @param dataUnitsToUpdate String of data units to be updated.
	 */
	constructor(p1: Byte, p2: Byte, dataUnitsToUpdate: ByteArray) : super(
		x00,
		UPDATE_BINARY_INS_DATA,
		p1,
		p2,
		dataUnitsToUpdate,
	)

	/**
	 * Creates a new UPDATE BINARY APDU.
	 * APDU: 0x00 0xD7 P1 P2 Lc Data
	 *
	 * @param p1 P1 according to ISO 7816 part 4 section 7.2.2.
	 * @param p2 P2 according to ISO 7816 part 4 section 7.2.2.
	 * @param offsetDiscretionaryData An TLV which insists of an offset data object (Tag 0x54) and a discretionary data
	 * object (Tag 53 or 73).
	 * @throws TLVException Thrown if the conversion of the TLV input data into a byte array failed.
	 */
	constructor(p1: Byte, p2: Byte, offsetDiscretionaryData: TLV) {
		val data = offsetDiscretionaryData.asList()

		require(data.size <= 2) { "Wrong number of objects." }

		require(data[0].tagNumWithClass == 0x54L) { "The tag for the offset data object is wrong." }

		require(!(data[1].tagNumWithClass != 0x53L && data[1].tagNumWithClass != 0x73L)) {
			"The discretionary data object does not start with the correct tag."
		}

		cla = x00
		ins = UPDATE_BINARY_INS_OFFSET_AND_DISCRETIONARY_DATA
		this.p1 = p1
		this.p2 = p2
		this.data = offsetDiscretionaryData.toBER()
		lc = offsetDiscretionaryData.toBER().size
	}

	companion object {
		/**
		 * Instruction byte for UPDATE BINARY which indicates that the data field contains a string of data units to be
		 * updated.
		 */
		private const val UPDATE_BINARY_INS_DATA = 0xD6.toByte()

		/**
		 * Instruction byte for UPDATE BINARY.
		 * This instruction byte indicates that the data field contains an offset data object followed by an discretionary
		 * data object.
		 */
		private const val UPDATE_BINARY_INS_OFFSET_AND_DISCRETIONARY_DATA = 0xD7.toByte()
	}
}
