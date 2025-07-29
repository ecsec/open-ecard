package org.openecard.sc.iface.info

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.tlv.Tlv
import org.openecard.utils.common.toUShort

private val log = KotlinLogging.logger { }

interface FileReference {
	object MF : FileReference

	class ShortEf(
		val shortEf: UByte,
	) : FileReference

	class Identifier(
		val fileIdentifier: UShort,
	) : FileReference

	class Path
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			private val rawPath: UByteArray,
		) : FileReference {
			@OptIn(ExperimentalUnsignedTypes::class)
			val qualified by lazy {
				rawPath.size % 2 != 0
			}

			@OptIn(ExperimentalUnsignedTypes::class)
			val absolute by lazy {
				rawPath[0].toUInt() == 0x3fu && rawPath[1].toUInt() == 0x00u
			}

			val relative by lazy {
				!absolute
			}

			@OptIn(ExperimentalUnsignedTypes::class)
			val path =
				if (qualified) {
					rawPath.dropLast(1).toUByteArray()
				} else {
					rawPath
				}

			/**
			 * In a qualified Path, the last byte is p1.
			 */
			@OptIn(ExperimentalUnsignedTypes::class)
			val p1: UByte? =
				if (qualified) {
					rawPath.last()
				} else {
					null
				}
		}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromDataObject(tlv: Tlv): FileReference? =
			if (tlv.tag == StandardTags.fileReference) {
				val data = tlv.contentAsBytesBer
				if (data.isEmpty()) {
					MF
				} else if (data.size == 1) {
					val efByte = data[0]
					if ((efByte.toInt() and 0x7) == 0) {
						ShortEf(efByte.toInt().shr(3).toUByte())
					} else {
						log.info { "Short EF reference uses forbidden bytes" }
						null
					}
				} else if (data.size == 2) {
					Identifier(data.toUShort(0))
				} else {
					Path(data)
				}
			} else {
				null
			}
	}
}
