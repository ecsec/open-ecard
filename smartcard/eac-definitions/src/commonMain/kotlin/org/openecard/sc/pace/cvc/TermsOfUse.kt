package org.openecard.sc.pace.cvc

import org.openecard.sc.pace.oid.CvCertificatesObjectIdentifier
import org.openecard.sc.tlv.ObjectIdentifier
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.toString

sealed interface TermsOfUse {
	val type: ObjectIdentifier

	class PlainText(
		override val type: ObjectIdentifier,
		val text: String,
	) : TermsOfUse

	class Html(
		override val type: ObjectIdentifier,
		val html: String,
	) : TermsOfUse

	class Pdf(
		override val type: ObjectIdentifier,
		val pdf: ByteArray,
	) : TermsOfUse

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		fun Tlv.toTermsOfUse(descType: ObjectIdentifier): TermsOfUse =
			when (descType.value) {
				CvCertificatesObjectIdentifier.id_plainFormat -> {
					PlainText(descType, this.toString(Tag.STRING_UTF8_TAG))
				}
				CvCertificatesObjectIdentifier.id_htmlFormat -> {
					Html(descType, this.toString(Tag.STRING_IA5_TAG))
				}
				CvCertificatesObjectIdentifier.id_pdfFormat -> {
					require(this.tag == Tag.OCTETSTRING_TAG)
					Pdf(descType, this.contentAsBytesBer.toByteArray())
				}
				else -> throw IllegalArgumentException("Unspecified type identifier for TermsOfUse")
			}
	}
}
