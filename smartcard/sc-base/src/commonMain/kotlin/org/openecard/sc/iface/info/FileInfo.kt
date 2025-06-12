package org.openecard.sc.iface.info

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.LifeCycleStatus
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvException
import org.openecard.sc.tlv.findPrimitive
import org.openecard.sc.tlv.findTlv
import org.openecard.sc.tlv.toTlvBer
import org.openecard.utils.common.doIf
import org.openecard.utils.common.enlargeToLong
import org.openecard.utils.common.toULong
import org.openecard.utils.common.toUShort

private val log = KotlinLogging.logger { }

sealed interface FileInfo {
	/**
	 * Represents an unsupported file info object.
	 */
	class Unknown
		@OptIn(ExperimentalUnsignedTypes::class)
		constructor(
			val data: UByteArray,
		) : FileInfo

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromSelectResponseData(data: UByteArray): FileInfo {
			try {
				val tlv = data.toTlvBer().tlv
				
				return when (tlv.tag) {
					StandardTags.fcp -> {
						Fcp.fromTlv(tlv)
					}
					StandardTags.fmd -> {
						Fmd.fromTlv(tlv)
					}
					else -> {
						Fci.fromTlv(tlv)
					}
				}
			} catch (ex: TlvException) {
				log.info(ex) { "Failed to parse file information" }
				return Unknown(data)
			}
		}
	}
}

class Fcp(
	private val dos: List<Tlv>,
) : FileInfo {
	/**
	 * Number of data bytes in the file, excluding structural information
	 */
	@OptIn(ExperimentalUnsignedTypes::class)
	val numBytes: ULong? by lazy {
		dos
			.findPrimitive(0x80u)
			?.contentAsBytesBer
			?.enlargeToLong()
			?.toULong(0)
	}

	/**
	 * Number of data bytes in the file, including structural information if any
	 */
	@OptIn(ExperimentalUnsignedTypes::class)
	val numBytesStructure: UShort? by lazy {
		dos.findTlv(0x81u)?.contentAsBytesBer?.let {
			doIf(it.size == 2) { it.toUShort(0) }
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val fileDescriptor: FileDescriptor? by lazy {
		dos.findTlv(0x82u)?.contentAsBytesBer?.let {
			FileDescriptor.parse(it)
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val fileIdentifier: UShort? by lazy {
		dos.findTlv(0x83u)?.contentAsBytesBer?.let {
			doIf(it.size == 2) { it.toUShort(0) }
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val dfName: UByteArray? by lazy {
		dos.findTlv(0x84u)?.contentAsBytesBer
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val proprietaryNoBer: UByteArray? by lazy {
		dos.findTlv(0x85u)?.contentAsBytesBer
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val proprietarySecurityAttribute: UByteArray? by lazy {
		dos.findTlv(0x86u)?.contentAsBytesBer
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val fileIdentifierWithFciExtension by lazy {
		dos.findTlv(0x87u)?.contentAsBytesBer?.let {
			doIf(it.size == 2) { it.toUShort(0) }
		}
	}

	/**
	 * Short EF.
	 *
	 * Note that if the card supports selection by short EF identifiers (see 5.3.1.1) and if tag '88' is absent, then in
	 * the second byte of the file identifier (tag '83'), bits 5 to 1 encode the short EF identifier.
	 */
	@OptIn(ExperimentalUnsignedTypes::class)
	val shortEf: UByte? by lazy {
		dos.findTlv(0x88u)?.contentAsBytesBer?.let {
			doIf(it.size == 1) {
				// short ef is encoded as upper bytes
				it[0].toUInt().shr(3).toUByte()
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val lifeCycleStatus: LifeCycleStatus? by lazy {
		dos.findTlv(0x8Au)?.contentAsBytesBer?.let {
			doIf(it.size == 1) {
				LifeCycleStatus.fromStatusByte(it[0])
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val securityAttributeExtendedReference by lazy {
		// TODO: parse security attributes, sec.5.4.3.3
		dos
			.findTlv(0x8Bu)
			?.contentAsBytesBer
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val securityAttributeCompact by lazy {
		// TODO: parse security attributes, sec.5.4.3.1
		dos
			.findTlv(0x8Cu)
			?.contentAsBytesBer
			?.toTlvBer()
			?.tlv
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val fileIdentifierWithSecurityEnvTemplate by lazy {
		// TODO: parse security environment templates (see 6.3.4)
		dos.findTlv(0x8Du)?.contentAsBytesBer?.let {
			doIf(it.size == 2) { it.toUShort(0) }
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val channelSecurityAttribute by lazy {
		// TODO: Channel security attribute (see 5.4.3 and Table 15)
		dos.findTlv(0x8Eu)?.contentAsBytesBer?.let {
			doIf(it.size == 1) { it[0] }
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val securityAttributeTemplateForDo by lazy {
		// TODO: Security attribute template for data objects (see 5.4.3)
		dos.findTlv(0xA0u)?.contentAsBytesBer
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val securityAttributeTemplateProprietary by lazy {
		dos.findTlv(0xA1u)?.contentAsBytesBer
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val fileReferences by lazy {
		// TODO: parse
		// Template consisting of one or more pairs of data objects:
		// Short EF identifier (tag '88') - File reference (tag '51', L > 2, see 5.3.1.2)
		dos
			.findTlv(0xA2u)
			?.contentAsBytesBer
			?.toTlvBer()
			?.tlv
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val securityAttributeExtended by lazy {
		// TODO: parse Security attribute template in expanded format (see 5.4.3.2)
		dos
			.findTlv(0xABu)
			?.contentAsBytesBer
			?.toTlvBer()
			?.tlv
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val cryptoMechanisms by lazy {
		// TODO: parse Cryptographic mechanism identifier template (see 5.4.2)
		dos
			.findTlv(0xACu)
			?.contentAsBytesBer
			?.toTlvBer()
			?.tlv
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromTlv(tlv: Tlv): Fcp {
			require(tlv.tag == StandardTags.fcp)
			val children = requireNotNull(tlv.asConstructed?.childList()) { "FCP is not a constructed TLV object" }

			return Fcp(children)
		}
	}
}

class Fmd(
	private val dos: List<Tlv>,
) : FileInfo {
	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromTlv(tlv: Tlv): Fmd {
			require(tlv.tag == StandardTags.fmd)

			return Fmd(tlv.asConstructed!!.childList())
		}
	}
}

class Fci(
	private val dos: List<Tlv>,
) : FileInfo {
	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun fromTlv(tlv: Tlv): FileInfo {
			// TODO: implement
			return FileInfo.Unknown(tlv.toBer())
		}
	}
}
