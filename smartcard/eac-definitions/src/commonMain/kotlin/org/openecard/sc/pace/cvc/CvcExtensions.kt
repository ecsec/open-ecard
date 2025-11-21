package org.openecard.sc.pace.cvc
import org.openecard.sc.pace.oid.CvCertificatesObjectIdentifier
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvConstructed
import org.openecard.sc.tlv.findTlv
import org.openecard.sc.tlv.toObjectIdentifier
import org.openecard.utils.serialization.toPrintable

/**
 * Certificate Extensions for Terminal Authentication Version 2 according to TR-03110-3 Sec. C.3.
 */
class CvcExtensions(
	val dos: List<Tlv>,
	val extensions: List<CvcExtension>,
) {
	val certificateDescriptionReference: CvcExtension.CertificateDescriptionReference? by lazy {
		extensions.asSequence().filterIsInstance<CvcExtension.CertificateDescriptionReference>().firstOrNull()
	}

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(IllegalArgumentException::class)
		fun Tlv.toCvcExtensions(tag: Tag = CvcTags.certExtensions): CvcExtensions {
			require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
			return when (this) {
				is TlvConstructed -> {
					val extensions = mutableListOf<CvcExtension>()

					// evaluate child list
					for (next in this.childList()) {
						val childs = next.asConstructedAsserted.childList()
						check(childs.isNotEmpty()) { "Discretionary data is empty" }
						val oid = childs.first().toObjectIdentifier()
						when (oid.value) {
							CvCertificatesObjectIdentifier.id_description,
							-> {
								val hash =
									childs.findTlv(0x80u)?.contentAsBytesBer
										?: throw IllegalArgumentException("CertificateDescription reference is missing hash entry")
								extensions.add(CvcExtension.CertificateDescriptionReference(oid, hash.toPrintable()))
							}
							// TODO: parse more such as id_sector
						}
					}

					CvcExtensions(this.childList(), extensions)
				}
				else -> throw IllegalArgumentException("CVC Extensions TLV is not constructed")
			}
		}
	}
}
