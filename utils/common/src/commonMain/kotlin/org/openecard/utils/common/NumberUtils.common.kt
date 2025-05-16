package org.openecard.utils.common

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toUByte(offset: Int): UByte {
	val b = this[offset]
	return b
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toUShort(offset: Int): UShort {
	val b1 = this[offset]
	val b2 = this[offset + 1]
	val u = b1.toUInt().shl(8) or b2.toUInt()
	return u.toUShort()
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toUInt(offset: Int): UInt {
	val b1 = this.toUShort(offset)
	val b2 = this.toUShort(offset + 2)
	val u = b1.toUInt().shl(16) or b2.toUInt()
	return u
}

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toULong(offset: Int): ULong {
	val b1 = this.toUInt(offset)
	val b2 = this.toUInt(offset + 4)
	val u = b1.toULong().shl(8) or b2.toULong()
	return u
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
