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

/**
 * A set of utility functions for long integers.
 *
 * @author Tobias Wich
 */
object LongUtils {
	/**
	 * Convert a long integer to a byte array with a given bit size per byte.
	 *
	 * @param value Long integer to be converted.
	 * @param numBits Number of bits to use per byte.
	 * @param bigEndian `true` when output should be in Big Endian, `false` for Little Endian.
	 * @return byte[]
	 * @throws IllegalArgumentException Thrown in case the numBits value is not within the permitted range.

	 * Convert a long integer to a byte array with a given bit size per byte.
	 *
	 * @param value - long integer to be converted
	 * @param numBits Number of bits to use per byte.
	 * @return byte[]

	 * Convert a long integer to a byte array.
	 *
	 * @param value long integer to be converted
	 * @return byte[]
	 */
	fun toByteArray(
		value: Long,
		numBits: Int = 8,
		bigEndian: Boolean = true,
	): ByteArray {
		require(!(numBits <= 0 || numBits > 8)) { "Numbits must be between 0 and 8." }

		if (value == 0L) {
			return ByteArray(1)
		}

		var buffer: ByteArray? = null

		val numBytesInBuffer = 64 / numBits
		val restBits = 64 - (numBytesInBuffer * numBits)

		var j = 0
		for (i in numBytesInBuffer - (if (restBits > 0) 0 else 1) downTo 0) {
			val b: Byte
			// first chunk which has uneven number of bits?
			if (i == numBytesInBuffer) {
				val mask = numBitsToMask(restBits.toByte())
				b = ((value shr (((i - 1) * numBits) + restBits)).toByte().toInt() and mask.toInt()).toByte()
			} else {
				val mask = numBitsToMask(numBits.toByte())
				b = ((value shr (i * numBits)) and mask.toLong()).toByte()
			}

			if (buffer == null && b.toInt() != 0) {
				buffer = ByteArray(i + 1)
			} else if (buffer == null) {
				continue
			}

			buffer[j] = b
			j++
		}

		// when emitting little endian, reverse the array
		if (!bigEndian) {
			buffer = ByteUtils.reverse(buffer)
		}

		return buffer!!
	}

	private fun numBitsToMask(numBits: Byte): Byte {
		var result: Byte = 0
		for (i in 0..<numBits) {
			result = ((result.toInt() shl 1) or 1).toByte()
		}
		return result
	}

	/**
	 * Convert a long integer to a byte array.
	 * If the resulting array contains less bytes than 8 bytes, 0 bytes are prepended if the flag is set.
	 *
	 * @param value long integer to be converted
	 * @param padArrayToTypeLength
	 * @param bigEndian `true` when output should be in Big Endian, `false` for Little Endian.
	 * @return byte[]

	 * Convert a long integer to a byte array.
	 * If the resulting array contains less bytes than 8 bytes, 0 bytes are prepended if the flag is set.
	 *
	 * @param value long integer to be converted
	 * @param padArrayToTypeLength
	 * @return byte[]
	 */
	fun toByteArray(
		value: Long,
		padArrayToTypeLength: Boolean,
		bigEndian: Boolean = true,
	): ByteArray {
		var result = toByteArray(value, 8, bigEndian)
		if (padArrayToTypeLength && result.size < 8) {
			result = ByteUtils.concatenate(ByteArray(8 - result.size), result)!!
		}
		return result
	}
}
