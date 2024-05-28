package org.openecard.common.apdu.common

class ClassByte (
	var channelNumber: Int,
	var sm: SecureMessagingIndication,
	var commandChaining: Boolean,
) {
	val byte: Byte
		get() {
			return if (channelNumber > 3) {
				val ch = (channelNumber and 0b0000_1111)
				val smVal = when (sm) {
					SecureMessagingIndication.NO_SM -> 0
					SecureMessagingIndication.SM_WO_HEADER -> 0b0010_0000
					else -> throw IllegalArgumentException("SM indicator not supported with this amount of channels.")
				}
				val chain = if (commandChaining) 0b0001_0000 else 0
				(0b0100_0000 or ch or smVal or chain).toByte()
			} else {
				val ch = (channelNumber and 0b0000_0011)
				val smVal = sm.value shl 2
				val chain = if (commandChaining) 0b0001_0000 else 0
				(ch or smVal or chain).toByte()
			}
		}

	companion object {
		fun parse(data: Byte): ClassByte {
			return if ((data.toInt() and 0b1110_0000) == 0) {
				val smVal = ((data.toInt() shr 2) and 0b11).toByte()
				val sm =  SecureMessagingIndication.values().find { it.value.toByte() == smVal }!!
				ClassByte(
					data.toInt() and 0b0000_0011,
					sm,
					(data.toInt() and 0b0001_0000) != 0,
				)
			} else if ((data.toInt() and 0b1100_0000) == 0b0100_0000) {
				ClassByte(
					data.toInt() and 0b0000_1111,
					if ((data.toInt() and 0b0010_0000) > 0) SecureMessagingIndication.SM_WO_HEADER else SecureMessagingIndication.NO_SM,
					(data.toInt() and 0b0001_0000) != 0,
				)
			} else {
				throw IllegalArgumentException("Proprietary class bytes are not supported.")
			}
		}
	}
}

enum class SecureMessagingIndication(val value: Int) {
	NO_SM(0x00),
	PROPRIETARY(0x01),
	SM_WO_HEADER(0x02),
	SM_W_HEADER(0x03),
	;
}
