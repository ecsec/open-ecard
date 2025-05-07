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
package org.openecard.common.apdu.common

import org.openecard.common.apdu.common.CardAPDU
import org.openecard.common.apdu.common.CardCommandAPDU
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.ShortUtils.toByteArray

/**
 * Resolves RAPDU status words to human readable messages.
 * Cp. ISO-7816-4 sec. 5.1.3 Status Bytes
 *
 * @author Tobias Wich
 */
@Suppress("ktlint:standard:property-naming")
object CardCommandStatus {
	private const val defaultMsg = "Unknown status word (possibly proprietary)."
	private const val sw62 = "State of non-volatile memory unchanged."
	private const val sw63 = "State of non-volatile memory changed."
	private const val sw64 = "State of non-volatile memory unchanged."
	private const val sw65 = "State of non-volatile memory changed."
	private const val sw66 = "Security related issues."
	private const val sw67 = "Wrong length without further indication."
	private const val sw68 = "Functions in CLA not supported."
	private const val sw69 = "Command not allowed."
	private const val sw6A = "Wrong parameters P1-P2."
	private const val sw6B = "Wrong parameters P1-P2."
	private const val sw6C = "Wrong length field."
	private const val sw6D = "Instruction code not supported or invalid."
	private const val sw6E = "Class not supported."
	private const val sw6F = "No precise diagnosis."

	fun getMessage(status: ByteArray): String {
		var msg = defaultMsg
		when (status[0]) {
			0x90.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = "No error."
				}

			0x61.toByte() -> msg = "No error, but " + (0xFF and status[1].toInt()) + " bytes left to read."
			0x62.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw62 + " No further information given."
					0x81.toByte() -> msg = sw62 + " Part of returned data may be corrupted."
					0x82.toByte() -> msg = sw62 + " End of file reached before reading requested number of bytes."
					0x83.toByte() -> msg = sw62 + " Selected file deactivated."
					0x84.toByte() -> msg = sw62 + " File control information not formatted according to ISO/IEC 7816-4."
					0x85.toByte() -> msg = sw62 + " Selected file in termination state."
					0x86.toByte() -> msg = sw62 + " No input data available from a sensor on the card."
					else ->
						if (status[1] >= 0x02 && status[1] <= 0x80) {
							msg = sw62 + " Triggering by the card (see ISO/IEC 7816-4 8.6.1)."
						}
				}

			0x63.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw63 + " No further information given."
					0x81.toByte() -> msg = sw63 + " File filled up by last write."
					else ->
						if (status[1] >= 0xC0 && status[1] <= 0xCF) {
							msg = sw63 + " Counter is " + (0x0F and status[1].toInt()) + "."
						}
				}

			0x64.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw64 + " Execution error."
					0x01.toByte() -> msg = sw64 + " Immediate response required by the card."
					else ->
						if (status[1] >= 0x02 && status[1] <= 0x80) {
							msg = sw64 + " Triggered by the card (see ISO/IEC 7816-4 8.6.1)."
						}
				}

			0x65.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw65 + " No information given."
					0x81.toByte() -> msg = sw65 + " Memory failure."
				}

			0x66.toByte() -> msg = sw66
			0x67.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw67
				}

			0x68.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw68 + " No information given."
					0x81.toByte() -> msg = sw68 + " Logical channel not supported."
					0x82.toByte() -> msg = sw68 + " Secure messaging not supported."
					0x83.toByte() -> msg = sw68 + " Last command of the chain expected."
					0x84.toByte() -> msg = sw68 + " Command chaining not supported."
				}

			0x69.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw69 + " No information given."
					0x81.toByte() -> msg = sw69 + " Command incompatible with file structure."
					0x82.toByte() -> msg = sw69 + " Security status not satisfied."
					0x83.toByte() -> msg = sw69 + " Authentication method blocked."
					0x84.toByte() -> msg = sw69 + " Reference data not usable."
					0x85.toByte() -> msg = sw69 + " Conditions of use not satisfied."
					0x86.toByte() -> msg = sw69 + " Command not allowed (no current EF)."
					0x87.toByte() -> msg = sw69 + " Expected secure messaging data objects missing."
					0x88.toByte() -> msg = sw69 + " Incorrect secure messaging data objects."
				}

			0x6A.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw6A + " No information given."
					0x80.toByte() -> msg = sw6A + " Incorrect parameters in the command data field."
					0x81.toByte() -> msg = sw6A + " Function not supported."
					0x82.toByte() -> msg = sw6A + " File or application not found."
					0x83.toByte() -> msg = sw6A + " Record not found."
					0x84.toByte() -> msg = sw6A + " Not enough memory space in the file."
					0x85.toByte() -> msg = sw6A + " Command length inconsistent with TLV structure."
					0x86.toByte() -> msg = sw6A
					0x87.toByte() -> msg = sw6A + " Command length inconsistent with parameters P1-P2."
					0x88.toByte() -> msg = sw6A + " Referenced data or reference data not found."
					0x89.toByte() -> msg = sw6A + " File already exists."
					0x8A.toByte() -> msg = sw6A + " DF name already exists."
				}

			0x6B.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw6B
				}

			0x6C.toByte() -> msg = sw6C + " " + (0xFF and status[1].toInt()) + " bytes available."
			0x6D.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw6D
				}

			0x6E.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw6E
				}

			0x6F.toByte() ->
				when (status[1]) {
					0x00.toByte() -> msg = sw6F
				}
		}

		// append status code
		msg += " (Code: " + ByteUtils.toHexString(status) + ")"

		return msg
	}

	fun ok(): ByteArray = byteArrayOf(0x90.toByte(), 0x00.toByte())

	fun responseOk(): List<ByteArray> = response(ok())

	fun response(vararg expected: ByteArray): List<ByteArray> {
		val result = ArrayList<ByteArray>(expected.size)
		result.addAll(listOf(*expected))

		return expected.asList()
	}

	fun response(vararg expected: Int): List<ByteArray> {
		val conv = mutableListOf<ByteArray>()
		for (item in expected) {
			conv.add(toByteArray(item.toShort(), true))
		}

		return conv
	}
}
