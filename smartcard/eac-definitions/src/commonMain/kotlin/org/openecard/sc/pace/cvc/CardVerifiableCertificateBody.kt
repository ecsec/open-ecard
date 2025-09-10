package org.openecard.sc.pace.cvc

import org.openecard.sc.pace.cvc.Chat.Companion.toChat
import org.openecard.sc.pace.cvc.CvcDate.Companion.toCvcDate
import org.openecard.sc.pace.cvc.CvcExtensions.Companion.toCvcExtensions
import org.openecard.sc.pace.cvc.PublicKey.Companion.toPublicKey
import org.openecard.sc.pace.cvc.PublicKeyReference.Companion.toPublicKeyReference
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvException
import org.openecard.sc.tlv.findTlv
import org.openecard.sc.tlv.toUInt

interface CardVerifiableCertificateBody {
	val profileId: UInt
	val certificateAuthorityReference: PublicKeyReference
	val publicKey: PublicKey
	val certificateHolderReference: PublicKeyReference
	val chat: Chat<*>
	val validFrom: CvcDate
	val validUntil: CvcDate
	val extensions: CvcExtensions

	companion object {
		@Throws(TlvException::class, IllegalArgumentException::class, NoSuchElementException::class)
		fun Tlv.toCardVerifiableCertificateBody(): CardVerifiableCertificateBody {
			val tlv = requireNotNull(this.asConstructed) { "Given tag is not constructed" }
			require(tlv.tag == CvcTags.body) { "Data does not contain a CVC body tag" }
			val content = tlv.childList()

			val profileId =
				content.findTlv(CvcTags.profileIdentifier)?.toUInt(CvcTags.profileIdentifier)
					?: throw IllegalArgumentException("Profile Identifier is missing")
			val car =
				content
					.findTlv(
						CvcTags.certificateAuthorityReference,
					)?.toPublicKeyReference(CvcTags.certificateAuthorityReference)
					?: throw IllegalArgumentException("CAR is missing")
			val pk =
				content
					.findTlv(
						CvcTags.publicKey,
					)?.toPublicKey(CvcTags.publicKey)
					?: throw IllegalArgumentException("Public key is missing")
			val chr =
				content
					.findTlv(
						CvcTags.certificateHolderReference,
					)?.toPublicKeyReference(CvcTags.certificateHolderReference)
					?: throw IllegalArgumentException("CHR is missing")
			val chat =
				content
					.findTlv(
						CvcTags.chat,
					)?.toChat()
					?: throw IllegalArgumentException("CHAT is missing")
			val validFrom =
				content
					.findTlv(
						CvcTags.generationDate,
					)?.toCvcDate(CvcTags.generationDate)
					?: throw IllegalArgumentException("Generation date is missing")
			val validUntil =
				content
					.findTlv(
						CvcTags.expirationDate,
					)?.toCvcDate(CvcTags.expirationDate)
					?: throw IllegalArgumentException("Expiration date is missing")
			val extensions =
				content
					.findTlv(
						CvcTags.certExtensions,
					)?.toCvcExtensions()
					?: CvcExtensions(listOf())

			return CardVerifiableCertificateBodyImpl(profileId, car, pk, chr, chat, validFrom, validUntil, extensions)
		}
	}
}

internal class CardVerifiableCertificateBodyImpl(
	override val profileId: UInt,
	override val certificateAuthorityReference: PublicKeyReference,
	override val publicKey: PublicKey,
	override val certificateHolderReference: PublicKeyReference,
	override val chat: Chat<*>,
	override val validFrom: CvcDate,
	override val validUntil: CvcDate,
	override val extensions: CvcExtensions,
) : CardVerifiableCertificateBody
