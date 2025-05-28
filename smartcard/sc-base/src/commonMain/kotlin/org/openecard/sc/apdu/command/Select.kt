package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.utils.serialization.toPrintable

class Select
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val p1: UByte,
		val data: UByteArray?,
		val fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
		val fileControlInfo: FileControlInformation = FileControlInformation.NONE,
		val extendedLength: Boolean = false,
	) : IsoCommandApdu {
		@OptIn(ExperimentalUnsignedTypes::class)
		override val apdu: CommandApdu by lazy {
			val le: UShort? =
				if (fileControlInfo == FileControlInformation.NONE) {
					0u
				} else {
					null
				}
			CommandApdu(0x00u, 0xA4u, p1, p2, (data ?: ubyteArrayOf()).toPrintable(), le, forceExtendedLength = extendedLength)
		}
	
		val p2: UByte by lazy { ((fileControlInfo.code.toUInt() shl 2) or fileOccurrence.code.toUInt()).toUByte() }

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectMf(
				withName: Boolean = true,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
			): Select =
				Select(
					0x00u,
					if (withName) ubyteArrayOf(0x3Fu, 0x00u) else null,
					SelectFileOccurrence.FIRST,
					fileControlInfo,
				)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectAnyIdentifier(
				data: UByteArray?,
				fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				extendedLength: Boolean = false,
			): Select = Select(0x00u, data, fileOccurrence, fileControlInfo, extendedLength)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectChildDfIdentifier(
				data: UByteArray,
				fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				extendedLength: Boolean = false,
			): Select = Select(0x01u, data, fileOccurrence, fileControlInfo, extendedLength)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectEfIdentifier(
				data: UByteArray,
				fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				extendedLength: Boolean = false,
			): Select = Select(0x02u, data, fileOccurrence, fileControlInfo, extendedLength)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectParentDf(
				fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				extendedLength: Boolean = false,
			): Select = Select(0x03u, null, fileOccurrence, fileControlInfo, extendedLength)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectDfName(
				data: UByteArray,
				fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				extendedLength: Boolean = false,
			): Select = Select(0x04u, data, fileOccurrence, fileControlInfo, extendedLength)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectApplicationId(
				data: UByteArray,
				fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				extendedLength: Boolean = false,
			): Select = selectDfName(data, fileOccurrence, fileControlInfo, extendedLength)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectPathAbsolute(
				data: UByteArray,
				fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				extendedLength: Boolean = false,
			): Select = Select(0x08u, data, fileOccurrence, fileControlInfo, extendedLength)

			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectPathRelative(
				data: UByteArray,
				fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				extendedLength: Boolean = false,
			): Select = Select(0x09u, data, fileOccurrence, fileControlInfo, extendedLength)
		}
	}

enum class SelectFileOccurrence(
	val code: UByte,
) {
	FIRST(0x0u),
	LAST(0x1u),
	NEXT(0x2u),
	PREVIOUS(0x3u),
}

enum class FileControlInformation(
	val code: UByte,
) {
	FCI(0x0u),
	FCP(0x1u),
	FMD(0x2u),
	NONE(0x3u),
}
