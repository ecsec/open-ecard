package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.utils.serialization.toPrintable

class ReadBinary(
	offset: ULong = 0u,
	efIdentifier: UShort = 0u,
	val le: UShort = 0u,
	val forceExtendedLength: Boolean = false,
	proprietaryDataObject: Boolean = false,
) : BaseBinaryCommand(0xB0u, offset, efIdentifier, proprietaryDataObject) {
	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu
		get() {
			val params = this.buildParameters()

			val data =
				params.offsetTlv?.let {
					makeDataObject(it, proprietaryDataObject)
				} ?: ubyteArrayOf()

			return CommandApdu(
				0x00u,
				params.ins,
				params.p1,
				params.p2,
				data = data.toPrintable(),
				le = le,
				forceExtendedLength = forceExtendedLength,
			)
		}

	companion object {
		fun readCurrentEf(
			offset: ULong = 0u,
			le: UShort = 0u,
			forceExtendedLength: Boolean = false,
			proprietaryDataObject: Boolean = false,
		): ReadBinary = ReadBinary(offset, 0u, le, forceExtendedLength, proprietaryDataObject)

		fun readShortEf(
			shortEf: UByte,
			offset: ULong = 0u,
			le: UShort = 0u,
			forceExtendedLength: Boolean = false,
			proprietaryDataObject: Boolean = false,
		): ReadBinary = ReadBinary(offset, shortEf.toUShort(), le, forceExtendedLength, proprietaryDataObject)

		fun readEfIdentifier(
			efIdentifier: UShort,
			offset: ULong = 0u,
			le: UShort = 0u,
			forceExtendedLength: Boolean = false,
			proprietaryDataObject: Boolean = false,
		): ReadBinary = ReadBinary(offset, efIdentifier, le, forceExtendedLength, proprietaryDataObject)
	}
}
