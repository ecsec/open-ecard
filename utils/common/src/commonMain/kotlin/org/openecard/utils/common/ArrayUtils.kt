package org.openecard.utils.common

/**
 * Reverses the array when the given condition is true.
 * This is a convenience function, so an extra if around the reverse is not needed.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.reversedIf(cond: () -> Boolean): UByteArray =
	if (cond()) {
		this.reversedArray()
	} else {
		this
	}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toNibbles(bigEndian: Boolean = true): UByteArray =
	flatMap {
		val lb = (it.toUInt() and 0xFu).toUByte()
		val hb = (it.toUInt() shr 4).toUByte()
		if (bigEndian) {
			listOf(hb, lb)
		} else {
			listOf(lb, hb)
		}
	}.toUByteArray()

/**
 * Converts the digits to a string.
 * Each byte may only be in the range from 0 to 9.
 * @throws IllegalArgumentException Thrown when one of the bytes is out of the allowed range.
 */
@Throws(IllegalArgumentException::class)
@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toDigitsString(): String = map { it.toInt().digitToChar() }.toCharArray().concatToString()
