package org.openecard.sc.pace.cvc

import org.openecard.sc.pace.cvc.TermsOfUse.Companion.toTermsOfUse
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.TagClass
import org.openecard.sc.tlv.TlvException
import org.openecard.sc.tlv.findTlv
import org.openecard.sc.tlv.toObjectIdentifier
import org.openecard.sc.tlv.toString
import org.openecard.sc.tlv.toTlvBer

/**
 * Certificate Description Extension according to TR-03110-4, Sec.2.2.6.
 */
class CertificateDescription
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		rawData: UByteArray,
		val issuerName: String,
		val issuerUrl: String?,
		val subjectName: String,
		val subjectUrl: String?,
		val termsOfUse: TermsOfUse,
		val redirectUrl: String?,
		val commCertificates: Set<UByteArray>?,
	) {
		@OptIn(ExperimentalUnsignedTypes::class)
		private val rawData: UByteArray = rawData.copyOf()

		@OptIn(ExperimentalUnsignedTypes::class)
		val asBytes: UByteArray get() = rawData.copyOf()

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			@Throws(TlvException::class, IllegalArgumentException::class, NoSuchElementException::class)
			fun UByteArray.toCertificateDescription(): CertificateDescription {
				val tlv = requireNotNull(this.toTlvBer().tlv.asConstructed) { "Given tag is not constructed" }
				require(tlv.tag == Tag.SEQUENCE_TAG) { "Certificate Description object is not a sequence" }
				val content = tlv.childList()

				val descType =
					content.findTlv(Tag.OID_TAG)?.toObjectIdentifier()
						?: throw IllegalArgumentException("Description type is missing")
				val issName =
					content
						.findTlv(Tag(TagClass.CONTEXT, false, 1u))
						?.asConstructedAsserted
						?.let { requireNotNull(it.child) }
						?.toString(Tag.STRING_UTF8_TAG)
						?: throw IllegalArgumentException("Issuer name is missing")
				val issUrl =
					content
						.findTlv(Tag(TagClass.CONTEXT, false, 2u))
						?.asConstructedAsserted
						?.let { requireNotNull(it.child) }
						?.toString(Tag.STRING_PRINTABLE_TAG)
				val subName =
					content
						.findTlv(Tag(TagClass.CONTEXT, false, 3u))
						?.asConstructedAsserted
						?.let { requireNotNull(it.child) }
						?.toString(Tag.STRING_UTF8_TAG)
						?: throw IllegalArgumentException("Subject name is missing")
				val subUrl =
					content
						.findTlv(Tag(TagClass.CONTEXT, false, 4u))
						?.asConstructedAsserted
						?.let { requireNotNull(it.child) }
						?.toString(Tag.STRING_PRINTABLE_TAG)
				val tou =
					content
						.findTlv(Tag(TagClass.CONTEXT, false, 5u))
						?.asConstructedAsserted
						?.let { requireNotNull(it.child) }
						?.toTermsOfUse(descType)
						?: throw IllegalArgumentException("Description value is missing")
				val redirectUrl =
					content
						.findTlv(Tag(TagClass.CONTEXT, false, 6u))
						?.asConstructedAsserted
						?.let { requireNotNull(it.child) }
						?.toString(Tag.STRING_PRINTABLE_TAG)
				val commCerts =
					content
						.findTlv(Tag(TagClass.CONTEXT, false, 7u))
						?.asConstructedAsserted
						?.let { requireNotNull(it.child) }
						?.let {
							require(it.tag == Tag.SET_TAG)
							val commDos = it.asConstructedAsserted.childList()
							commDos.map { cdo -> cdo.contentAsBytesBer }.toSet()
						}

				return CertificateDescription(this, issName, issUrl, subName, subUrl, tou, redirectUrl, commCerts)
			}
		}
	}
