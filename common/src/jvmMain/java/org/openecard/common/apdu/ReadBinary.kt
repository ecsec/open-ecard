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
package org.openecard.common.apdu

import iso.std.iso_iec._24727.tech.schema.Transmit
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.tlv.TLV
import org.openecard.common.tlv.TLVException
import org.openecard.common.util.ShortUtils

/**
 *
 * @author Moritz Horsch
 * @author Johannes Schmoelz
 * @author Tobias Wich
 */
class ReadBinary : CardCommandAPDU {
	/**
	 * Creates a new READ BINARY APDU.
	 */
	constructor() : super(x00, READ_BINARY_INS_1, x00, xFF)

	/**
	 * Creates a new READ BINARY APDU.
	 *
	 * @param offset Offset
	 */
	constructor(offset: Byte) : super(x00, READ_BINARY_INS_1, offset, xFF)

	/**
	 * Creates a new READ BINARY APDU.
	 *
	 * @param offset Offset
	 * @param length Expected length
	 */
	constructor(offset: Byte, length: Byte) : super(x00, READ_BINARY_INS_1, offset, length)

	/**
	 * Creates a new READ BINARY APDU.
	 * Bit 8 of P1 is set to 1, bits 7 and 6 of P1 are set to 00 (RFU),
	 * bits 5 to 1 of P1 encode a short EF identifier and P2 (eight bits)
	 * encodes an offset from zero to 255.
	 *
	 * @param shortFileID Short file identifier
	 * @param offset Offset
	 * @param length Expected length
	 */
	constructor(shortFileID: Byte, offset: Byte, length: Short) : super(
		x00,
		READ_BINARY_INS_1,
		((0x1F and shortFileID.toInt()) or 0x80).toByte(),
		offset,
	) {
		setLE(length)
	}

	/**
	 * Creates a new READ BINARY APDU.
	 * Bit 8 of P1 is set to 0, and P1-P2 (fifteen bits) encodes an
	 * offset from zero to 32 767.
	 *
	 * @param offset Offset
	 * @param length Expected length
	 */
	constructor(offset: Short, length: Short) {
		ins = READ_BINARY_INS_1
		p1 = (((offset.toInt() shr 8).toByte()).toInt() and 0x7F).toByte()
		p2 = (offset.toInt() and xFF.toInt()).toByte()
		setLE(length)
	}

	/**
	 * Creates a new READ BINARY APDU.
	 * P1-P2 set to '0000' identifies the current EF.
	 * The offset data object with tag '54' is encoded in the command data field.
	 *
	 * @param fileID File identifier
	 * @param offset Offset
	 * @param length Expected length
	 */
	constructor(fileID: Short, offset: Short, length: Short) : super(
		x00,
		READ_BINARY_INS_2,
		x00,
		x00,
	) {
		try {
			// offset DO according to ISO/IEC 7816-4, Sec. 7.2.2
			val discretionaryData = TLV()
			discretionaryData.setTagNumWithClass(0x53.toByte())
			val offsetDo = TLV()
			offsetDo.setTagNumWithClass(0x54.toByte())
			offsetDo.value = ShortUtils.toByteArray(offset)
			discretionaryData.value = offsetDo.toBER()
			val content = discretionaryData.toBER()

			p1P2 = ShortUtils.toByteArray(fileID, true)
			data = content
			setLE(length)
		} catch (ex: TLVException) {
			throw RuntimeException("Error encoding offset DO.", ex)
		}
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
		 * READ BINARY command instruction byte. INS = 0xB0
		 */
		private const val READ_BINARY_INS_1 = 0xB0.toByte()

		/**
		 * READ BINARY command instruction byte. INS = 0xB1
		 */
		private const val READ_BINARY_INS_2 = 0xB1.toByte()
	}
}
