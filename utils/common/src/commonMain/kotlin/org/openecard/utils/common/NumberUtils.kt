package org.openecard.utils.common

fun Boolean.toInt(): Int = if (this) 1 else 0

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toUByte(offset: Int): UByte {
	val b = this[offset]
	return b
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toUShort(
	offset: Int,
	bigEndian: Boolean = true,
): UShort {
	val lowerOffset = if (bigEndian) offset else offset + 1
	val higherOffset = if (bigEndian) offset + 1 else offset
	val b1 = this[lowerOffset]
	val b2 = this[higherOffset]
	val u = b1.toUInt().shl(8) or b2.toUInt()
	return u.toUShort()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toUInt(
	offset: Int,
	bigEndian: Boolean = true,
): UInt {
	val b1 = this.toUShort(offset, bigEndian)
	val b2 = this.toUShort(offset + 2, bigEndian)
	val u =
		if (bigEndian) {
			b1.toUInt().shl(16) or b2.toUInt()
		} else {
			b2.toUInt().shl(16) or b1.toUInt()
		}
	return u
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toULong(
	offset: Int,
	bigEndian: Boolean = true,
): ULong {
	val b1 = this.toUInt(offset, bigEndian)
	val b2 = this.toUInt(offset + 4, bigEndian)
	val u =
		if (bigEndian) {
			b1.toULong().shl(32) or b2.toULong()
		} else {
			b2.toULong().shl(32) or b1.toULong()
		}
	return u
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.enlargeToShort(): UByteArray = enlargeToNBytes(Short.SIZE_BYTES)

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.enlargeToInt(): UByteArray = enlargeToNBytes(Int.SIZE_BYTES)

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.enlargeToLong(): UByteArray = enlargeToNBytes(Long.SIZE_BYTES)

@OptIn(ExperimentalUnsignedTypes::class)
private fun UByteArray.enlargeToNBytes(n: Int): UByteArray =
	if (size < n) {
		val result = UByteArray(n)
		this.copyInto(result, destinationOffset = result.size - this.size)
		result
	} else {
		this
	}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByte.toUByteArray(): UByteArray = ubyteArrayOf(this)

@OptIn(ExperimentalUnsignedTypes::class)
fun UShort.toUByteArray(bigEndian: Boolean = true): UByteArray {
	val v1 = this and 0xFFu
	val v2 = this.toUInt().shr(8) and 0xFFu
	return (ubyteArrayOf(v2.toUByte(), v1.toUByte())).reversedIf { !bigEndian }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UInt.toUByteArray(bigEndian: Boolean = true): UByteArray {
	val v1 = this.toUShort().toUByteArray()
	val v2 = this.shr(16).toUShort().toUByteArray()
	return (v2 + v1).reversedIf { !bigEndian }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ULong.toUByteArray(bigEndian: Boolean = true): UByteArray {
	val v1 = this.toUInt().toUByteArray()
	val v2 = this.shr(32).toUInt().toUByteArray()
	return (v2 + v1).reversedIf { !bigEndian }
}

@OptIn(ExperimentalUnsignedTypes::class)
fun ULong.toSparseUByteArray(
	numBits: Int = 8,
	bigEndian: Boolean = true,
): UByteArray {
	require(!(numBits < 1 || numBits > 8)) { "numBits must be between 1 and 8, but was $numBits." }
	val value = this

	val numBytesInBuffer = 64 / numBits
	val restBits = 64 - (numBytesInBuffer * numBits)
	val buffer = mutableListOf<UByte>()

	var firstAdded = false
	for (i in numBytesInBuffer - (if (restBits > 0) 0 else 1) downTo 0) {
		val b: UByte
		// first chunk might have an uneven number of bits
		if (i == numBytesInBuffer) {
			val mask = numBitsToMask(restBits)
			b = ((value shr (((i - 1) * numBits) + restBits)).toByte().toInt() and mask.toInt()).toUByte()
		} else {
			val mask = numBitsToMask(numBits)
			b = ((value shr (i * numBits)) and mask.toULong()).toUByte()
		}

		if (!firstAdded && b.toUInt() == 0u) {
			continue
		}
		firstAdded = true
		buffer.add(b)
	}

	if (buffer.isEmpty()) {
		// return at least one byte
		return ubyteArrayOf(0u)
	}

	return buffer.toUByteArray().reversedIf { !bigEndian }
}

private fun numBitsToMask(numBits: Int): UByte {
	var result: UInt = 0u
	for (i in 0..<numBits) {
		result = ((result shl 1) or 1u)
	}
	return result.toUByte()
}

val UByte.isOdd: Boolean get() {
	return this.mod(2u) == 1u
}
val UShort.isOdd: Boolean get() {
	return this.mod(2u) == 1u
}
val UInt.isOdd: Boolean get() {
	return this.mod(2u) == 1u
}
val ULong.isOdd: Boolean get() {
	return this.mod(2u) == 1u
}
