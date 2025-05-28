package org.openecard.sc.apdu

sealed interface ClassByte {
	val byte: UByte

	companion object {
		@Throws(IllegalArgumentException::class)
		@OptIn(ExperimentalStdlibApi::class)
		fun parse(data: UByte): ClassByte =
			if ((data.toInt() and 0b1110_0000) == 0) {
				val smVal = ((data.toInt() shr 2) and 0b11).toUByte()
				val sm =
					SecureMessagingIndication.entries.find { it.value.toUByte() == smVal }!!
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
				ProprietaryClassByte((data.toInt() and 0b0111_1111).toUByte())
			} else {
				throw IllegalArgumentException("Class byte parsing iss erroneous (CLA=${data.toHexString()}).")
			}

		fun parseInterIndustry(data: UByte): InterIndustryClassByte =
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
	override val byte: UByte
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
				(propBit or 0b0100_0000 or ch or smVal or chain).toUByte()
			} else {
				val ch = (channelNumber and 0b0000_0011)
				val smVal = sm.value.toInt() shl 2
				val chain = if (commandChaining) 0b0001_0000 else 0
				(propBit or ch or smVal or chain).toUByte()
			}
		}

	fun setChannelNumber(channelNumber: Int): InterIndustryClassByte =
		InterIndustryClassByte(channelNumber, sm, commandChaining)

	fun setCommandChaining(commandChaining: Boolean): InterIndustryClassByte =
		InterIndustryClassByte(channelNumber, sm, commandChaining)

	fun setSecureMessaging(sm: SecureMessagingIndication): InterIndustryClassByte =
		InterIndustryClassByte(channelNumber, sm, commandChaining)
}

class ProprietaryClassByte(
	var proprietaryData: UByte,
) : ClassByte {
	override val byte: UByte
		get() = (0x80 or proprietaryData.toInt()).toUByte()

	fun toInterIndustry(): InterIndustryClassByte {
		val isoCla = ClassByte.parseInterIndustry(proprietaryData)
		isoCla.proprietary = true
		return isoCla
	}
}

enum class SecureMessagingIndication(
	val value: UByte,
) {
	NO_SM(0x00u),
	PROPRIETARY(0x01u),
	SM_WO_HEADER(0x02u),
	SM_W_HEADER(0x03u),
}
