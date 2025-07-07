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

@OptIn(ExperimentalUnsignedTypes::class)
fun List<UByteArray>.mergeToArray(): UByteArray {
	val len = this.fold(0, { acc, next -> acc + next.size })
	val result = UByteArray(len)
	var offset = 0
	forEach {
		it.copyInto(result, destinationOffset = offset)
		offset += it.size
	}
	return result
}

/**
 * Checks if the reference data can be found in the array.
 * It applies the given mask before comparing the values.
 * If offset is null, the reference is searched for in any possible position.
 * If offset is defined, the reference data must be found at the exact position.
 */
@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.maskedContains(
	referenceData: UByteArray,
	mask: UByteArray? = null,
	offset: Int? = null,
): Boolean {
	if (referenceData.size > size) {
		throw IndexOutOfBoundsException("Reference data is bigger than data to search in.")
	}

	// loop over window between offset and end of data
	val start = (offset ?: 0)
	val end =
		if (offset == null) {
			size - referenceData.size
		} else {
			start
		}
	for (i in start until end + 1) {
		val nextSlice = this.sliceArray(i until i + referenceData.size)
		val matches =
			if (mask == null) {
				nextSlice.contentEquals(referenceData)
			} else {
				val m1 = nextSlice.mask(mask)
				val m2 = referenceData.mask(mask)
				m1.contentEquals(m2)
			}

		if (matches) {
			return true
		}
	}

	return false
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.mask(mask: UByteArray): UByteArray {
	require(size == mask.size) { "Mask does not have the same size as the array to mask." }
	val result = copyOf()
	for (i in 0 until size) {
		result[i] = result[i] and mask[i]
	}
	return result
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.removeLeadingZeros(): UByteArray {
	val data = this.dropWhile { it == 0u.toUByte() }
	return if (data.isEmpty()) {
		ubyteArrayOf(0u)
	} else {
		data.toUByteArray()
	}
}
