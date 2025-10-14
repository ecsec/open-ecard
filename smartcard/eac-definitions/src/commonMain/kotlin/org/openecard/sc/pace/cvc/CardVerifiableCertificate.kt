package org.openecard.sc.pace.cvc

import org.openecard.sc.pace.cvc.CardVerifiableCertificateBody.Companion.toCardVerifiableCertificateBody
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.Tlv.Companion.tagAt
import org.openecard.sc.tlv.TlvException
import org.openecard.sc.tlv.toTlvBer

/**
 * Card Verifiable Certificate (CVC) data structure according to TR-03110-3, Sec. C.
 */
class CardVerifiableCertificate
	@OptIn(ExperimentalUnsignedTypes::class)
	constructor(
		private val original: UByteArray,
		private val tlv: Tlv,
		body: CardVerifiableCertificateBody,
		val signature: UByteArray,
	) : CardVerifiableCertificateBody by body {
		val isTerminalCertificate by lazy { chat.role.code == 0u }

		@OptIn(ExperimentalUnsignedTypes::class)
		val originalBytes: UByteArray get() = original.copyOf()

		@OptIn(ExperimentalUnsignedTypes::class)
		val bodyBytes: UByteArray get() = tlv.contentAsBytesBer.copyOf()

		companion object {
			@OptIn(ExperimentalUnsignedTypes::class)
			@Throws(TlvException::class, IllegalArgumentException::class)
			fun UByteArray.toCardVerifiableCertificate(): CardVerifiableCertificate {
				val tlv = requireNotNull(this.toTlvBer().tlv.asConstructed) { "Given data does not contain a constructed tag" }
				require(tlv.tag == CvcTags.cvc) { "Data does not contain a CVC tag" }
				require(tlv.childList().size == 2) { "CVC does not contain exactly 2 elements" }

				val body =
					tlv.childList().tagAt(0, CvcTags.body)?.toCardVerifiableCertificateBody()
						?: throw IllegalArgumentException("CVC is missing the body element")
				val signature =
					tlv.childList().tagAt(1, CvcTags.signature)?.contentAsBytesBer
						?: throw IllegalArgumentException("CVC is missing the signature element")

				return CardVerifiableCertificate(this.copyOf(), tlv, body, signature)
			}
		}
	}
