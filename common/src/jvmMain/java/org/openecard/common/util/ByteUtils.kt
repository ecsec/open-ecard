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

import org.openecard.bouncycastle.util.Arrays
import java.io.PrintWriter
import java.io.StringWriter

/**
 * A set of utility functions for Byte and Byte Array.
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
object ByteUtils {
	/**
	 * Clone a byte array.
	 *
	 * @param input the byte array to clone
	 * @return new byte array, or null if input is null
	 */
	@JvmStatic
	fun clone(input: ByteArray?): ByteArray? {
		if (input == null) {
			return null
		}
		val ret = ByteArray(input.size)
		System.arraycopy(input, 0, ret, 0, input.size)
		return ret
	}

	/**
	 * Concatenate a byte array.
	 *
	 * @param x byte array
	 * @param y byte array
	 * @return x || y
	 */
	@JvmStatic
	fun concatenate(
		x: ByteArray?,
		y: ByteArray?,
	): ByteArray? {
		if (x == null) {
			return y
		}
		if (y == null) {
			return x
		}
		val ret = ByteArray(x.size + y.size)
		System.arraycopy(x, 0, ret, 0, x.size)
		System.arraycopy(y, 0, ret, x.size, y.size)
		return ret
	}

	/**
	 * Concatenate a byte array.
	 *
	 * @param x byte
	 * @param y byte array
	 * @return x || y
	 */
	@JvmStatic
	fun concatenate(
		x: Byte,
		y: ByteArray?,
	): ByteArray = concatenate(byteArrayOf(x), y)!!

	/**
	 * Concatenate a byte array.
	 *
	 * @param x byte array
	 * @param y byte
	 * @return x || y
	 */
	@JvmStatic
	fun concatenate(
		x: ByteArray?,
		y: Byte,
	): ByteArray? = concatenate(x, byteArrayOf(y))

	/**
	 * Concatenate a byte array.
	 *
	 * @param x byte
	 * @param y byte
	 * @return x || y
	 */
	@JvmStatic
	fun concatenate(
		x: Byte,
		y: Byte,
	): ByteArray? = concatenate(byteArrayOf(x), byteArrayOf(y))

	/**
	 * Cut leading null bytes of a byte array.
	 *
	 * @param input
	 * @return byte array without leading null bytes
	 */
	@JvmStatic
	fun cutLeadingNullBytes(input: ByteArray?): ByteArray? {
		if (input == null) {
			return null
		}
		var i = 0
		while (i < input.size - 1) {
			if (input[i] != 0x00.toByte()) {
				break
			}
			i++
		}
		return copy(input, i, input.size - i)
	}

	/**
	 * Removes leading null byte from the input byte array.
	 *
	 * @param input Byte array
	 * @return byte array without leading null bytes
	 */
	@JvmStatic
	fun cutLeadingNullByte(input: ByteArray?): ByteArray? {
		if (input == null) {
			return null
		}
		if (input[0] != 0x00.toByte()) {
			return clone(input)
		}
		return copy(input, 1, input.size - 1)
	}

	/**
	 * Copy of range.
	 *
	 * @param input the input
	 * @param offset
	 * @param length the length
	 * @return the byte[]
	 */
	@JvmStatic
	fun copy(
		input: ByteArray?,
		offset: Int,
		length: Int,
	): ByteArray? {
		if (input == null) {
			return null
		}
		val tmp = ByteArray(length)
		System.arraycopy(input, offset, tmp, 0, length)
		return tmp
	}

	/**
	 * Compare two byte arrays.
	 *
	 * @param x byte array
	 * @param y byte array
	 * @return true if x = y, otherwise false
	 */
	@JvmStatic
	fun compare(
		x: ByteArray?,
		y: ByteArray?,
	): Boolean {
		if (y == null || x == null) {
			return false
		}
		if (x.size != y.size) {
			return false
		}
		return Arrays.areEqual(x, y)
	}

	/**
	 * Compare two byte arrays.
	 *
	 * @param x byte array
	 * @param y byte array
	 * @return true if x = y, otherwise false
	 */
	@JvmStatic
	fun compareUnsigned(
		x: ByteArray?,
		y: ByteArray?,
	): Int = Arrays.compareUnsigned(x, y)

	/**
	 * Compare two byte arrays.
	 *
	 * @param x byte
	 * @param y byte array
	 * @return true if x = y, otherwise false
	 */
	@JvmStatic
	fun compare(
		x: Byte,
		y: ByteArray?,
	): Boolean = compare(byteArrayOf(x), y)

	/**
	 * Compare two byte arrays.
	 *
	 * @param x byte array
	 * @param y byte
	 * @return true if x = y, otherwise false
	 */
	@JvmStatic
	fun compare(
		x: ByteArray?,
		y: Byte,
	): Boolean = compare(x, byteArrayOf(y))

	/**
	 * Compare two bytes.
	 *
	 * @param x byte
	 * @param y byte
	 * @return true if x = y, otherwise false
	 */
	@JvmStatic
	fun compare(
		x: Byte,
		y: Byte,
	): Boolean = compare(byteArrayOf(x), byteArrayOf(y))

	/**
	 * Determines whether the first value is a prefix of the second value.
	 * If any of the values is null, `false` is returned.
	 *
	 * @param prefix The value which is a potential prefix.
	 * @param data The value containing the prefix.
	 * @return `true` if the first value is a prefix, `false` if it is not, any of the values is null, or
	 * the data value is shorter than the prefix.
	 */
	@JvmStatic
	fun isPrefix(
		prefix: ByteArray?,
		data: ByteArray?,
	): Boolean {
		if (prefix == null || data == null) {
			return false
		}
		if (prefix.size == 0 || prefix.size > data.size) {
			return false
		}
		for (i in prefix.indices) {
			if (prefix[i] != data[i]) {
				return false
			}
		}
		return true
	}

	/**
	 * Determines whether the first value is a prefix of the second value.
	 * If the data value is null, `false` is returned.
	 *
	 * @param prefix The value which is a potential prefix.
	 * @param data The value containing the prefix.
	 * @return `true` if the first value is a prefix, `false` if it is not, the data value is null or empty.
	 */
	fun isPrefix(
		prefix: Byte,
		data: ByteArray?,
	): Boolean = isPrefix(byteArrayOf(prefix), data)

	/**
	 * Create a reversed version of the given array.
	 * This function copies the value leaving the input untouched.
	 *
	 * @param in The array to reverse.
	 * @return The reversed array or null, if null was given.
	 */
	@JvmStatic
	fun reverse(`in`: ByteArray?): ByteArray? {
		if (`in` == null) {
			return null
		}
		val out = ByteArray(`in`.size)
		for (i in `in`.indices) {
			out[out.size - 1 - i] = `in`[i]
		}
		return out
	}

	/**
	 * Convert a byte array to a hex string suitable for use as XML's hexBinary type.
	 *
	 * @param bytes Input
	 * @return Hex string only compose of digits, no 0x and no spaces.
	 */
	@JvmStatic
	fun toHexString(bytes: ByteArray?): String? = toHexString(bytes, "%02X", false)

	/**
	 * Convert a byte array to a hex string.
	 *
	 * @param bytes Input
	 * @param formatted If true the string is formatted to 0xXX presentation
	 * @return Hex string
	 */
	@JvmStatic
	fun toHexString(
		bytes: ByteArray?,
		formatted: Boolean,
	): String? = toHexString(bytes, formatted, false)

	/**
	 * Convert a byte array to a hex string.
	 *
	 * @param bytes Input
	 * @param formatted If true the string is formatted to 0xXX presentation
	 * @param addLinebreak If true the string is formatted to 16 value per line
	 * @return Hex string
	 */
	@JvmStatic
	fun toHexString(
		bytes: ByteArray?,
		formatted: Boolean,
		addLinebreak: Boolean,
	): String? =
		if (formatted) {
			toHexString(bytes, "0x%02X ", addLinebreak)
		} else {
			toHexString(bytes, "%02X", addLinebreak)
		}

	private fun toHexString(
		bytes: ByteArray?,
		format: String,
		addLinebreak: Boolean,
	): String? {
		if (bytes == null) {
			return null
		}

		val writer = StringWriter(bytes.size * 2)
		val out = PrintWriter(writer)

		for (i in 1..bytes.size) {
			out.printf(format, bytes[i - 1])
			if (addLinebreak) {
				if (i % 16 == 0) {
					out.append("\n")
				}
			}
		}

		return writer.toString()
	}

	/**
	 * Encode a byte array as web safe base 64 string.
	 * The padding is removed, so that the resulting value can be used in URLs without extra escaping.
	 *
	 * @param bytes Date to be base 64 encoded.
	 * @return Base 64 coded string or `null` if no data was given.
	 * @see .toFileSafeBase64String
	 */
	@JvmStatic
	fun toWebSafeBase64String(bytes: ByteArray?): String? = toFileSafeBase64String(bytes, false)

	/**
	 * Encode a byte array as file safe base 64 string.
	 * The padding is removed according to the respective parameter.
	 *
	 * @param bytes Date to be base 64 encoded.
	 * @param withPadding If `true` the result contains pad characters when needed, if `false` the pad
	 * characters are cut off.
	 * @return Base 64 coded string or `null` if no data was given.
	 * @see FileSafeBase64

	 * Encode a byte array as file safe base 64 string.
	 *
	 * @param bytes Date to be base 64 encoded.
	 * @return Base 64 coded string or `null` if no data was given.
	 * @see .toFileSafeBase64String
	 */
	@JvmStatic
	@JvmOverloads
	fun toFileSafeBase64String(
		bytes: ByteArray?,
		withPadding: Boolean = true,
	): String? {
		if (bytes == null) {
			return null
		}

		var result: String = FileSafeBase64.toBase64String(bytes)
		if (!withPadding) {
			result = result.replace("=*$".toRegex(), "")
		}
		return result
	}

	/**
	 * Convert a byte array to a short integer.
	 * The size of byte array must be between 1 and 2. The endianess of the input is determined by the respective
	 * parameter.
	 *
	 * @param bytes Byte array to be converted.
	 * @param bigEndian `true` when input should be treated as Big Endian, `false` for Little Endian.
	 * @return short

	 * Convert a byte array to a short integer.
	 * The size of byte array must be between 1 and 2. The input is treated as Big Endian.
	 *
	 * @param bytes Byte array to be converted.
	 * @return short
	 */
	@JvmStatic
	@JvmOverloads
	fun toShort(
		bytes: ByteArray,
		bigEndian: Boolean = true,
	): Short {
		require(!(bytes.size > 2 || bytes.size < 1)) { "Size of byte array must be between 1 and 2." }

		return toLong(bytes, bigEndian).toShort()
	}

	/**
	 * Convert a byte array to an integer.
	 * The size of byte array must be between 1 and 4. The endianess of the input is determined by the respective
	 * parameter.
	 *
	 * @param bytes Byte array to be converted.
	 * @param bigEndian `true` when input should be treated as Big Endian, `false` for Little Endian.
	 * @return int

	 * Convert a byte array to an integer.
	 * The size of byte array must be between 1 and 4. The input is treated as Big Endian.
	 *
	 * @param bytes Byte array to be converted.
	 * @return int
	 */
	@JvmStatic
	@JvmOverloads
	fun toInteger(
		bytes: ByteArray,
		bigEndian: Boolean = true,
	): Int {
		require(!(bytes.size > 4 || bytes.isEmpty())) { "Size of byte array must be between 1 and 4." }

		return toLong(bytes, bigEndian).toInt()
	}

	/**
	 * Convert a byte array to a long integer.
	 * The size of byte array must be between 1 and 8. The endianess of the input is determined by the respective
	 * parameter.
	 *
	 * @param bytes Byte array to be converted.
	 * @param bigEndian `true` when input should be treated as Big Endian, `false` for Little Endian.
	 * @return long

	 * Convert a byte array to a long integer.
	 * The size of byte array must be between 1 and 8. The input is treated as Big Endian.
	 *
	 * @param bytes Byte array to be converted.
	 * @return long
	 */
	@JvmStatic
	fun toLong(
		bytes: ByteArray,
		bigEndian: Boolean = true,
	): Long {
		require(!(bytes.size > 8 || bytes.isEmpty())) { "Size of byte array must be between 1 and 8." }

		var result: Long = 0

		if (bigEndian) {
			for (i in bytes.indices) {
				result = result or ((0xFFL and bytes[bytes.size - 1 - i].toLong()) shl i * 8)
			}
		} else {
			for (i in bytes.indices) {
				result = result or ((0xFFL and bytes[i].toLong()) shl i * 8)
			}
		}

		return result
	}

	/**
	 * Checks if a bit in the array is set or not.
	 *
	 * @param position Position in array
	 * @param array Array
	 * @return True if the bit is set, false otherwise
	 * @throws IllegalArgumentException if position is negative or greater than the number of bits in this array
	 */
	@JvmStatic
	fun isBitSet(
		position: Int,
		array: ByteArray,
	): Boolean {
		require(!(position < 0 || position >= array.size * 8)) { "Position is invalid" }
		return ((array[position / 8].toInt() and (128 shr (position % 8))) > 0)
	}

	/**
	 * Sets the bit in the array.
	 *
	 * @param position Position
	 * @param array Array
	 * @throws IllegalArgumentException if position is negative or greater than the number of bits in this array
	 */
	@JvmStatic
	fun setBit(
		position: Int,
		array: ByteArray,
	) {
		require(!(position < 0 || position >= array.size * 8)) { "Position is invalid" }
		array[position / 8] = (array[position / 8].toInt() or (128 shr (position % 8))).toByte()
	}
}
