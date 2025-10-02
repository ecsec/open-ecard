package org.openecard.utils.common

object Iso88591 {
	fun ByteArray.decodeIso88591(): String {
		val inputData = this
		return buildString {
			inputData.forEach { byte ->
				append(Char(byte.toUByte().toInt()))
			}
		}
	}

	fun String.encodeIso88591(): ByteArray {
		val bytes = ByteArray(length)
		forEachIndexed { i, char ->
			val code = char.code
			if (code > 0xff) {
				throw IllegalStateException("Char with code ${code.toString(16)} can not be encoded as ISO-8859-1 string")
			} else {
				bytes[i] = code.toByte()
			}
		}
		return bytes
	}
}
