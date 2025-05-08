package org.openecard.sc.iface

class CommandApdu
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val cla: UByte,
		val ins: UByte,
		val p1: UByte,
		val p2: UByte,
		val data: UByteArray = ubyteArrayOf(),
		val le: UShort? = 0u,
		forceExtendedLength: Boolean = false,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val lc: UShort = data.size.toUShort()

		@OptIn(ExperimentalUnsignedTypes::class)
		val toBytes: UByteArray by lazy {
			val isLcExtended = lc > 0xFFu
			val isLeExtended = le?.let { it > 0xFFu } == true
			val isExtended =
				(le != null && forceExtendedLength || isLcExtended || isLeExtended) || (isLcExtended)

			val header = ubyteArrayOf(cla, ins, p1, p2)

			val lcField =
				if (lc > 0u) {
					if (isExtended) {
						ubyteArrayOf(0x00u, (lc.rotateRight(8) and 0xFFu).toUByte(), (lc and 0xFFu).toUByte())
					} else {
						ubyteArrayOf((lc and 0xFFu).toUByte())
					}
				} else {
					ubyteArrayOf()
				}
			val leField =
				le?.let {
					if (isExtended) {
						if (lc > 0u) {
							ubyteArrayOf((le.rotateRight(8) and 0xFFu).toUByte(), (le and 0xFFu).toUByte())
						} else {
							ubyteArrayOf(0x00u, (le.rotateRight(8) and 0xFFu).toUByte(), (le and 0xFFu).toUByte())
						}
					} else {
						ubyteArrayOf((le and 0xFFu).toUByte())
					}
				} ?: ubyteArrayOf()

			// assemble apdu
			header + lcField + data + leField
		}
	}

class ResponseApdu
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val data: UByteArray,
		val sw1: UByte,
		val sw2: UByte,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val apdu: UByteArray by lazy {
			data + ubyteArrayOf(sw1, sw2)
		}
		val sw: UShort
			get() = (sw1.toUInt().shl(8) or sw2.toUInt()).toUShort()
	}

@OptIn(ExperimentalUnsignedTypes::class)
fun ByteArray.toResponseApdu(): ResponseApdu = toUByteArray().toResponseApdu()

@OptIn(ExperimentalUnsignedTypes::class)
fun UByteArray.toResponseApdu(): ResponseApdu {
	require(size >= 2)
	val sw = takeLast(2)
	val data = sliceArray(0 until size - 2)
	return ResponseApdu(data.toUByteArray(), sw1 = sw[0].toUByte(), sw2 = sw[1].toUByte())
}

val ResponseApdu.isNormalProcessed: Boolean
	get() = sw.toUInt() == 0x9000u
