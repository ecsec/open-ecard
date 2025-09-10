package org.openecard.sc.pace.cvc

import org.openecard.sc.pace.oid.CvCertificatesObjectIdentifier
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvConstructed
import org.openecard.sc.tlv.buildTlv
import org.openecard.sc.tlv.findTlv
import org.openecard.sc.tlv.tlvStandard
import org.openecard.sc.tlv.toObjectIdentifier
import org.openecard.utils.common.BitSet
import org.openecard.utils.common.bitSetOf

/**
 * Certificate Holder Authorization Template according to TR-03110-3, Sec. C.1.5 and TR-03110-4, Sec. 2.2.2.
 */
sealed class Chat<Self : Chat<Self>>(
	val terminalType: ObjectIdentifier,
	private val accessRights: BitSet,
) {
	abstract val role: Role

	@OptIn(ExperimentalUnsignedTypes::class)
	val asTlv: TlvConstructed get() {
		return buildTlv(CvcTags.chat) {
			generic(terminalType.tlvStandard)
			primitive(Tag.forTagNumWithClass(0x53u), accessRights.toUByteArray().reversedArray())
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	val asBytes: UByteArray get() = asTlv.toBer()

	abstract fun copy(): Self

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(IllegalArgumentException::class)
		fun Tlv.toChat(tag: Tag = CvcTags.chat): Chat<*> {
			require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
			return when (this) {
				is TlvConstructed -> {
					val dos = this.childList()
					val identifier =
						dos.findTlv(Tag.OID_TAG)?.toObjectIdentifier()
							?: throw IllegalArgumentException("No terminal type present in CHAT")
					val rightsData =
						dos.findTlv(Tag.forTagNumWithClass(0x53u))?.contentAsBytesBer
							?: throw IllegalArgumentException("No rights flags present in CHAT")

					when (identifier.value) {
						CvCertificatesObjectIdentifier.id_IS -> TODO("Implement Inspection Terminal")
						CvCertificatesObjectIdentifier.id_AT ->
							AuthenticationTerminalChat(
								identifier,
								bitSetOf(*rightsData.reversedArray()),
							)
						CvCertificatesObjectIdentifier.id_ST -> TODO("Implement Signature Terminal")
						else -> throw IllegalArgumentException("Unknown terminal type ($identifier)")
					}
				}
				else -> throw IllegalArgumentException("CHAT TLV is not primitive")
			}
		}

		@OptIn(ExperimentalUnsignedTypes::class)
		internal fun roleType(
			terminalType: ObjectIdentifier,
			accessRights: BitSet,
			bitRange: IntProgression,
		): Role =
			when (accessRights.slice(bitRange).toUByteArray()[0].toUInt()) {
				3u -> Role.CVCA
				2u -> Role.DV_OFFICIAL
				1u -> Role.DV_NON_OFFICIAL
				0u ->
					when (terminalType.value) {
						CvCertificatesObjectIdentifier.id_IS -> Role.INSPECTION_TERMINAL
						CvCertificatesObjectIdentifier.id_AT -> Role.AUTHENTICATION_TERMINAL
						CvCertificatesObjectIdentifier.id_ST -> Role.SIGNATURE_TERMINAL
						else -> throw IllegalStateException("CHAT loaded with unsupported terminal type")
					}
				else -> throw IllegalStateException("Logic error in CHAT Role evaluation")
			}
	}

	enum class Role(
		val code: UInt,
	) {
		CVCA(3u),
		DV_OFFICIAL(2u),
		DV_NON_OFFICIAL(1u),
		INSPECTION_TERMINAL(0u),
		AUTHENTICATION_TERMINAL(0u),
		SIGNATURE_TERMINAL(0u),
	}
}
