package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.common.removeLeadingZeros
import org.openecard.utils.common.toUByteArray
import org.openecard.utils.serialization.toPrintable

class ReadRecord(
	val recordIdOrNum: UByte,
	val mode: ReadRecordMode,
	val offset: ULong = 0u,
	val shortEf: UByte = 0u,
	val le: UShort = 0u,
	val forceExtendedLength: Boolean = false,
	val proprietaryDataObject: Boolean = false,
) : IsoCommandApdu {
	init {
		check(shortEf < 0x1Fu) { "Short EF contains more than 5 bit" }
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override val apdu: CommandApdu by lazy {
		val p2 = (shortEf.toUInt().shl(3)) or mode.code
		val ins =
			if (offset > 0u) {
				0xB3u
			} else {
				0xB2u
			}
		val data =
			if (offset > 0u) {
				val offsetVal = offset.toUByteArray().removeLeadingZeros().toPrintable()
				val offsetTlv = TlvPrimitive(Tag.forTagNumWithClass(0x54u), offsetVal)
				makeDataObject(offsetTlv, proprietaryDataObject)
			} else {
				ubyteArrayOf()
			}

		CommandApdu(0x0u, ins.toUByte(), recordIdOrNum, p2.toUByte(), data.toPrintable(), le, forceExtendedLength)
	}

	companion object {
// 		fun readNextRecordIdentifier(
// 			offset: ULong = 0u,
// 			le: UShort = 0u,
// 			forceExtendedLength: Boolean = false,
// 			proprietaryDataObject: Boolean = false,
// 		): ReadRecord =
// 			ReadRecord(
// 				0u,
// 				mode = ReadRecordModeForIdentifier.NEXT_OCCURRENCE,
// 				offset = offset,
// 				le = le,
// 				forceExtendedLength = forceExtendedLength,
// 				proprietaryDataObject = proprietaryDataObject,
// 			)

		fun readRecordForNumber(
			recordNum: UByte,
			shortEf: UByte = 0u,
			offset: ULong = 0u,
			le: UShort = 0u,
			forceExtendedLength: Boolean = false,
			proprietaryDataObject: Boolean = false,
		) = ReadRecord(
			recordIdOrNum = recordNum,
			shortEf = shortEf,
			mode = ReadRecordModeForNumber.READ_P1,
			offset = offset,
			le = le,
			forceExtendedLength = forceExtendedLength,
			proprietaryDataObject = proprietaryDataObject,
		)

		fun readRecordForNumberToEnd(
			recordNum: UByte,
			shortEf: UByte = 0u,
			le: UShort = 0u,
			forceExtendedLength: Boolean = false,
			proprietaryDataObject: Boolean = false,
		) = ReadRecord(
			recordIdOrNum = recordNum,
			shortEf = shortEf,
			mode = ReadRecordModeForNumber.READ_FROM_P1_TO_END,
			le = le,
			forceExtendedLength = forceExtendedLength,
			proprietaryDataObject = proprietaryDataObject,
		)

		fun readRecordForNumberFromEnd(
			recordNum: UByte,
			shortEf: UByte = 0u,
			le: UShort = 0u,
			forceExtendedLength: Boolean = false,
			proprietaryDataObject: Boolean = false,
		) = ReadRecord(
			recordIdOrNum = recordNum,
			shortEf = shortEf,
			mode = ReadRecordModeForNumber.READ_FROM_END_TO_P1,
			le = le,
			forceExtendedLength = forceExtendedLength,
			proprietaryDataObject = proprietaryDataObject,
		)

		fun readAllRecords(
			shortEf: UByte = 0u,
			le: UShort = 0u,
			forceExtendedLength: Boolean = false,
			proprietaryDataObject: Boolean = false,
		) = ReadRecord(
			recordIdOrNum = 1u,
			shortEf = shortEf,
			mode = ReadRecordModeForNumber.READ_FROM_P1_TO_END,
			le = le,
			forceExtendedLength = forceExtendedLength,
			proprietaryDataObject = proprietaryDataObject,
		)
	}
}

interface ReadRecordMode {
	val code: UInt
}

enum class ReadRecordModeForIdentifier(
	override val code: UInt,
) : ReadRecordMode {
	FIRST_OCCURRENCE(0u),
	LAST_OCCURRENCE(1u),
	NEXT_OCCURRENCE(2u),
	PREVIOUS_OCCURRENCE(3u),
}

enum class ReadRecordModeForNumber(
	override val code: UInt,
) : ReadRecordMode {
	READ_P1(4u),
	READ_FROM_P1_TO_END(5u),
	READ_FROM_END_TO_P1(6u),
}
