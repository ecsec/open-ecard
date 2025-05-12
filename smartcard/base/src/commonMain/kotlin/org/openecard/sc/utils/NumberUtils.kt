package org.openecard.sc.utils

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toUByte(offset: Int): UByte {
	val b = this[offset]
	return b
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toUShort(offset: Int): UShort {
	val b1 = this[offset]
	val b2: UByte = this[offset + 1]
	val u = b1.toUInt().rotateLeft(8) or b2.toUInt()
	return u.toUShort()
}
