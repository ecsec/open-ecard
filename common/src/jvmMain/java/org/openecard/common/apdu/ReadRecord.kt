/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

import iso.std.iso_iec._24727.tech.schema.Transmit
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.ShortUtils

/**
 * READ RECORD Command.
 * See ISO/IEC 7816-4 Section 7.3.3
 *
 * @author Dirk Petrautzki
 * @author Simon Potzernheim
 */
class ReadRecord : CardCommandAPDU {
	/**
	 * Creates an new READ RECORD APDU that reads the first record of the current selected file.
	 */
	constructor() : super(x00, READ_RECORD_INS_1, 0x01.toByte(), 0x04.toByte(), 0xFF)

	/**
	 * Creates an new READ RECORD APDU.
	 *
	 * @param recordNumber Number of the record
	 */
	constructor(recordNumber: Byte) : super(
		x00,
		READ_RECORD_INS_1,
		recordNumber,
		0x04.toByte(),
		0xFF,
	)

	/**
	 * Creates an new READ RECORD APDU.
	 * Bits 8 to 4 of P2 encode a short EF identifier.
	 *
	 * @param shortFileID Short file identifier
	 * @param recordNumber Number of the record
	 */
	constructor(shortFileID: Byte, recordNumber: Byte) : super(
		x00,
		READ_RECORD_INS_1,
		recordNumber,
		((shortFileID.toInt() shl 3) or 0x04).toByte(),
		0xFF,
	)

	/**
	 * Creates an new READ RECORD APDU.
	 * The offset data object with tag '54' is encoded in the command data field.
	 *
	 * @param recordNumber Number of the record
	 * @param offset Offset
	 * @param length Expected length
	 */
	constructor(recordNumber: Byte, offset: Short, length: Short) : super(
		x00,
		READ_RECORD_INS_2,
		recordNumber,
		0x04.toByte(),
	) {
		var content = ShortUtils.toByteArray(offset)
		content = ByteUtils.concatenate(0x54.toByte(), content)

		data = content
		setLE(length)
	}

	/**
	 * Creates an new READ RECORD APDU.
	 * Bits 8 to 4 of P2 encode a short EF identifier.
	 * The offset data object with tag '54' is encoded in the command data field.
	 *
	 * @param shortFileID Short file identifier
	 * @param recordNumber Number of the record
	 * @param offset Offset
	 * @param length Expected length
	 */
	constructor(shortFileID: Byte, recordNumber: Byte, offset: Short, length: Short) : super(
		x00,
		READ_RECORD_INS_2,
		recordNumber,
		((shortFileID.toInt() shl 3) or 0x04).toByte(),
	) {
		var content = ShortUtils.toByteArray(offset)
		content = ByteUtils.concatenate(0x54.toByte(), content)

		data = content
		setLE(length)
	}

	/**
	 * Creates a new Transmit message.
	 *
	 * @param slotHandle Slot handle
	 * @return Transmit
	 */
	override fun makeTransmit(slotHandle: ByteArray?): Transmit =
		makeTransmit(
			slotHandle,
			listOf(
				byteArrayOf(0x90.toByte(), 0x00.toByte()),
				byteArrayOf(0x62.toByte(), 0x82.toByte()),
			),
		)

	companion object {
		/**
		 * READ RECORD command instruction byte. INS = 0xB2
		 */
		private const val READ_RECORD_INS_1 = 0xB2.toByte()

		/**
		 * READ RECORD command instruction byte. INS = 0xB3
		 */
		private const val READ_RECORD_INS_2 = 0xB3.toByte()
	}
}
