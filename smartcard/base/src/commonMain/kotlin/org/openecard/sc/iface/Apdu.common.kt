package org.openecard.sc.iface

class CommandApdu
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val cla: UByte,
		val ins: UByte,
		val p1: UByte,
		val p2: UByte,
		val data: UByteArray = ubyteArrayOf(),
		val le: UShort = 0.toUShort(),
		forceExtendedLength: Boolean = false,
	) {
		val toBytes: ByteArray
			get() {
				TODO()
			}
	}

class ResponseApdu(
	val data: ByteArray,
	val sw1: UByte,
	val sw2: UByte,
) {
	val sw: UShort
		get() = (sw1.toUInt().shl(8) or sw2.toUInt()).toUShort()
}

fun ByteArray.toResponseApdu(): ResponseApdu {
	require(size >= 2)
	val sw = takeLast(2)
	val data = sliceArray(0 until size - 2)
	return ResponseApdu(data, sw1 = sw[0].toUByte(), sw2 = sw[1].toUByte())
}

val ResponseApdu.isNormalProcessed: Boolean
	get() = sw.toUInt() == 0x9000u
