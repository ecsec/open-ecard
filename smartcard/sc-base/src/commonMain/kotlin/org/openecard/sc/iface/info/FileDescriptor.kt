package org.openecard.sc.iface.info

import org.openecard.sc.iface.CardCapabilities
import org.openecard.utils.common.bitSetOf
import org.openecard.utils.common.doIf
import org.openecard.utils.common.toUShort

class FileDescriptor(
	val fdByte: FileDescriptorByte,
	val codingByte: CardCapabilities.DataCoding?,
	val maxRecordSize1: UShort?,
	val numRecords1: UShort?,
) {
	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun parse(data: UByteArray): FileDescriptor? {
			val fdByteValue = data[0]
			if ((fdByteValue.toUInt() and 0x80u) == 0u) {
				val fdByte = FileDescriptorByte(fdByteValue)
				val codingByte = doIf(data.size == 2) { CardCapabilities.DataCoding(data[1]) }
				val maxRecordSize =
					if (data.size == 3) {
						data[2].toUShort()
					} else if (data.size >= 4) {
						data.sliceArray(2 until 4).toUShort(0)
					} else {
						null
					}
				val numRecords =
					if (data.size == 5) {
						data[4].toUShort()
					} else if (data.size >= 6) {
						data.sliceArray(4 until 6).toUShort(0)
					} else {
						null
					}

				return FileDescriptor(fdByte, codingByte, maxRecordSize, numRecords)
			} else {
				// seems to be some proprietary value
				return null
			}
		}
	}
}

class FileDescriptorByte(
	val fdByte: UByte,
) {
	val fileAcessibility by lazy { FileAccessibility.fromFileDescriptorByte(fdByte) }
	val isDf = (fdByte.toUInt() and 0b111000u) == 0b111000u
	val isEf = !isDf
	val efCategory: EfCategory? by lazy {
		if (!isDf) {
			EfCategory.fromFileDescriptorByte(fdByte)
		} else {
			null
		}
	}
	val efStructure: EfStructure? by lazy {
		if (!isDf) {
			EfStructure.fromFileDescriptorByte(fdByte)
		} else {
			null
		}
	}
}

enum class FileAccessibility {
	/**
	 * Not shareable file.
	 */
	NOT_SHAREABLE,

	/**
	 * Shareable file.
	 * This means the file supports concurrent access in logical channels.
	 */
	SHAREABLE,
	;

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromFileDescriptorByte(fdByte: UByte): FileAccessibility {
			val shareable = bitSetOf(fdByte)[6]
			return if (shareable) {
				SHAREABLE
			} else {
				NOT_SHAREABLE
			}
		}
	}
}

enum class EfCategory {
	WORKING_EF,
	INTERNAL_EF,
	PROPRIETARY,
	;

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		internal fun fromFileDescriptorByte(fdByte: UByte): EfCategory? {
			val bits = fdByte.toUInt().shr(3) and 0x7u
			// it is weird, because table 14 contains entries to describe EFs with 0x7. Don't they have a category?
			return when (bits) {
				0x7u -> null
				0x0u -> WORKING_EF
				0x1u -> INTERNAL_EF
				else -> PROPRIETARY
			}
		}
	}
}

enum class EfStructure {
	NO_INFORMATION,
	TRANSPARENT,
	LINEAR_FIXED_ANY,
	LINEAR_FIXED_TLV,
	LINEAR_VARIABLE_ANY,
	LINEAR_VARIABLE_TLV,
	CYCLIC_FIXED_ANY,
	CYCLIC_FIXED_TLV,
	TLV_BER,
	TLV_SIMPLE,
	;

	val isTransparent by lazy { this in setOf(TRANSPARENT) }
	val isRecord by lazy {
		this in
			setOf(LINEAR_FIXED_ANY, LINEAR_FIXED_TLV, LINEAR_VARIABLE_ANY, CYCLIC_FIXED_ANY, CYCLIC_FIXED_TLV)
	}
	val isDataObject by lazy { this in setOf(TLV_BER, TLV_SIMPLE) }

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		internal fun fromFileDescriptorByte(fdByte: UByte): EfStructure? {
			val bits = fdByte.toUInt() and 0b111111u
			val upper = bits.shr(3)
			val lower = bits and 0x7u
			return if (upper == 0x7u) {
				when (lower) {
					0x1u -> TLV_BER
					0x2u -> TLV_SIMPLE
					else -> null
				}
			} else {
				when (lower) {
					0x0u -> NO_INFORMATION
					0x1u -> TRANSPARENT
					0x2u -> LINEAR_FIXED_ANY
					0x3u -> LINEAR_FIXED_TLV
					0x4u -> LINEAR_VARIABLE_ANY
					0x5u -> LINEAR_VARIABLE_TLV
					0x6u -> CYCLIC_FIXED_ANY
					0x7u -> CYCLIC_FIXED_TLV
					else -> throw RuntimeException("Logic error in EfStructure parsing")
				}
			}
		}
	}
}
