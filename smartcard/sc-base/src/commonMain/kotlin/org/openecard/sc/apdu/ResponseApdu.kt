package org.openecard.sc.apdu

class ResponseApdu
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val data: UByteArray,
		val sw1: UByte,
		val sw2: UByte,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		val toBytes: UByteArray by lazy {
			data + ubyteArrayOf(sw1, sw2)
		}
		val sw: UShort by lazy { (sw1.toUInt().shl(8) or sw2.toUInt()).toUShort() }
		val status: StatusWordResult by lazy { sw.toStatusWord() }
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

fun ResponseApdu.matchStatus(vararg codes: StatusWord): Boolean = status.type in codes

@OptIn(ExperimentalUnsignedTypes::class)
fun ResponseApdu.matchStatus(vararg codes: UShort): Boolean = status.sw in codes

@Throws(ApduProcessingError::class)
fun ResponseApdu.checkStatus(vararg codes: StatusWord): ResponseApdu {
	if (!matchStatus(*codes)) {
		throw ApduProcessingError(status, status.type.description)
	} else {
		return this
	}
}

@Throws(ApduProcessingError::class)
fun ResponseApdu.checkOk() {
	checkStatus(StatusWord.OK)
}

@Throws(ApduProcessingError::class)
fun ResponseApdu.checkNoError() {
	if (!(status.isNormal || status.isWarning)) {
		throw ApduProcessingError(status, status.type.description)
	}
}

@Throws(ApduProcessingError::class)
@OptIn(ExperimentalUnsignedTypes::class)
fun ResponseApdu.checkStatus(vararg codes: UShort): ResponseApdu {
	if (!matchStatus(*codes)) {
		throw ApduProcessingError(status, status.type.description)
	} else {
		return this
	}
}
