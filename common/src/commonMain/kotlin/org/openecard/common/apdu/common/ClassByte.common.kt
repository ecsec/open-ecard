package org.openecard.common.apdu.common

sealed interface ClassByte {
	val byte: Byte

	companion object {
		@OptIn(ExperimentalStdlibApi::class)
		fun parse(data: Byte): ClassByte =
			if ((data.toInt() and 0b1110_0000) == 0) {
				val smVal = ((data.toInt() shr 2) and 0b11).toByte()
				val sm = SecureMessagingIndication.entries.find { it.value.toByte() == smVal }!!
				InterIndustryClassByte(
					data.toInt() and 0b0000_0011,
					sm,
					(data.toInt() and 0b0001_0000) != 0,
				)
			} else if ((data.toInt() and 0b1100_0000) == 0b0100_0000) {
				InterIndustryClassByte(
					(data.toInt() and 0b0000_1111) + 4,
					if ((data.toInt() and 0b0010_0000) >
						0
					) {
						SecureMessagingIndication.SM_WO_HEADER
					} else {
						SecureMessagingIndication.NO_SM
					},
					(data.toInt() and 0b0001_0000) != 0,
				)
			} else if ((data.toInt() and 0b1110_0000) == 0b0010_0000) {
				throw IllegalArgumentException(
					"Class byte starting with bits 001 are reserved for future use (CLA=${data.toHexString()}).",
				)
			} else if (data.toInt() and 0b1000_0000 == 0b1000_0000) {
				ProprietaryClassByte((data.toInt() and 0b0111_1111).toByte())
			} else {
				throw IllegalArgumentException("Class byte parsing iss erroneous (CLA=${data.toHexString()}).")
			}

		fun parseInterIndustry(data: Byte): InterIndustryClassByte =
			when (val claByte = parse(data)) {
				is InterIndustryClassByte -> claByte
				else -> throw IllegalArgumentException("Class byte is not an Inter-Industry class byte.")
			}
	}
}

class InterIndustryClassByte(
	var channelNumber: Int,
	var sm: SecureMessagingIndication,
	var commandChaining: Boolean,
	var proprietary: Boolean = false,
) : ClassByte {
	override val byte: Byte
		get() {
			val propBit = if (proprietary) 0x80 else 0x00
			return if (channelNumber > 3) {
				val ch = (channelNumber - 4) and 0b0000_1111
				val smVal =
					when (sm) {
						SecureMessagingIndication.NO_SM -> 0
						SecureMessagingIndication.SM_WO_HEADER -> 0b0010_0000
						else -> throw IllegalArgumentException("SM indicator not supported with this amount of channels.")
					}
				val chain = if (commandChaining) 0b0001_0000 else 0
				(propBit or 0b0100_0000 or ch or smVal or chain).toByte()
			} else {
				val ch = (channelNumber and 0b0000_0011)
				val smVal = sm.value shl 2
				val chain = if (commandChaining) 0b0001_0000 else 0
				(propBit or ch or smVal or chain).toByte()
			}
		}
}

class ProprietaryClassByte(
	var proprietaryData: Byte,
) : ClassByte {
	override val byte: Byte
		get() = (0x80 or proprietaryData.toInt()).toByte()

	fun toInterIndustry(): InterIndustryClassByte {
		val isoCla = ClassByte.parseInterIndustry(proprietaryData)
		isoCla.proprietary = true
		return isoCla
	}
}

enum class SecureMessagingIndication(
	val value: Int,
) {
	NO_SM(0x00),
	PROPRIETARY(0x01),
	SM_WO_HEADER(0x02),
	SM_W_HEADER(0x03),
}
