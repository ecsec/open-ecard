package org.openecard.sc.apdu.command

import org.openecard.sc.apdu.CommandApdu
import org.openecard.utils.serialization.toPrintable

/**
 * Select command APDU according to ISO/IEC 7816-4, Sec. 7.1.1.
 *
 * The file parameter can bei either a _file identifier_, a _DF name_, or a _path_.
 * Sec. 5.3.1.1, goes into details what these types are:
 *
 * * **Selection by DF name** —
 *   A DF name may reference any DF. It is a string of up to sixteen bytes. Any application identifier
 *   (AID, see 8.2.1.2) may be used as DF name. In order to select unambiguously by DF name, e.g., when selecting by
 *   means of application identifiers, each DF name shall be unique within a given card.
 * * **Selection by file identifier** —
 *   A file identifier may reference any file. It consists of two bytes. The value `3F00` is reserved for referencing
 *   the MF. The value `FFFF` is reserved for future use. The value `3FFF` is reserved (see below and 7.4.1). The value
 *   `0000` is reserved (see 7.2.2 and 7.4.1). In order to unambiguously select any file by its identifier, all EFs and
 *   DFs immediately under a given DF shall have different file identifiers.
 * * **Selection by path** —
 *   A path may reference any file. It is a concatenation of file identifiers. The path begins with the identifier of a
 *   DF (the MF for an absolute path or the current DF for a relative path) and ends with the identifier of the file
 *   itself. Between those two identifiers, the path consists of the identifiers of the successive parent DFs, if any.
 *   The order of the file identifiers is always in the direction parent to child. If the identifier of the current DF
 *   is not known, then the value `3FFF` (reserved value) can be used at the beginning of the path. The values
 *   `3F002F00` and `3F002F01` are reserved (see 8.2.1.1). The path allows an unambiguous selection of any file from
 *   the MF or from the current DF (see 8.3).
 *
 * Note that short EF names can not be used with in a select APDU.
 */
class Select
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		val p1: UByte,
		val data: UByteArray?,
		val fileOccurrence: SelectFileOccurrence = SelectFileOccurrence.FIRST,
		val fileControlInfo: FileControlInformation = FileControlInformation.NONE,
		val extendedLength: Boolean = false,
		val forceNoLe: Boolean = false,
	) : IsoCommandApdu {
		@OptIn(ExperimentalUnsignedTypes::class)
		override val apdu: CommandApdu by lazy {
			val le: UShort? =
				if (forceNoLe) {
					null
				} else {
					if (fileControlInfo == FileControlInformation.NONE) {
						null
					} else {
						0u
					}
				}
			CommandApdu(0x00u, 0xA4u, p1, p2, (data ?: ubyteArrayOf()).toPrintable(), le, forceExtendedLength = extendedLength)
		}
	
		val p2: UByte by lazy { ((fileControlInfo.code.toUInt() shl 2) or fileOccurrence.code.toUInt()).toUByte() }

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			fun selectMf(
				withName: Boolean = true,
				fileControlInfo: FileControlInformation = FileControlInformation.NONE,
				forceNoLe: Boolean = false,
			): Select =
				Select(
					0x00u,
					if (withName) ubyteArrayOf(0x3Fu, 0x00u) else null,
					SelectFileOccurrence.FIRST,
					fileControlInfo,
					forceNoLe = forceNoLe,
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

			/**
			 * Convenience function which is identical to [selectDfName].
			 */
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
