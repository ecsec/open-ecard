package org.openecard.utils.common

import kotlin.UByteArray
import kotlin.random.Random
import kotlin.random.nextUBytes

@OptIn(ExperimentalUnsignedTypes::class)
fun Random.nextBitField(size: Int): UByteArray {
	if (size <= 0) throw IllegalArgumentException("At least one bit")

	val byteCount = (size + 7) / 8
	val randBytes = nextUBytes(byteCount)
	val bitsToUnset = 8 - size % 8

	return if (bitsToUnset < 8) {
		val mask = (0xff.toUInt() shr bitsToUnset).toUByte()
		(ubyteArrayOf(randBytes[0].and(mask)) + randBytes.drop(1))
	} else {
		randBytes
	}
}
