package org.openecard.sc.pace.cvc

import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.Tlv
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.serialization.toPrintable

data class PublicKeyReference(
	/**
	 * The Country Code SHALL be the ISO 3166-1 ALPHA-2 code of the certificate holder’s country.
	 */
	val countryCode: String,
	/**
	 * The Holder Mnemonic SHALL be assigned as unique identifier as follows:
	 * - The Holder Mnemonic of a CVCA SHALL be assigned by the CVCA itself.
	 * - The Holder Mnemonic of a DV SHALL be assigned by the domestic CVCA.
	 * - The Holder Mnemonic of an IS SHALL be assigned by the supervising DV.
	 */
	val holderMnemonic: String,
	/**
	 * The Sequence Number SHALL be assigned by the certificate holder.
	 * - The Sequence Number MUST be numeric or alphanumeric:
	 *     - A numeric Sequence Number SHALL consist of the characters “0”...”9”.
	 *     - An alphanumeric Sequence Number SHALL consist of the characters “0”...”9” and “A”...”Z”.
	 * - The Sequence Number MAY start with the ISO 3166-1 ALPHA-2 country code of the certifying certification
	 *   authority, the remaining three characters SHALL be assigned as alphanumeric Sequence Number.
	 * - The Sequence Number MAY be reset if all available Sequence Numbers are exhausted.
	 */
	val sequenceNumber: String,
) {
	fun joinToString(): String = "$countryCode$holderMnemonic$sequenceNumber"

	@OptIn(ExperimentalUnsignedTypes::class)
	fun toTlv(tag: Tag): Tlv = TlvPrimitive(tag, joinToString().encodeToByteArray().toUByteArray().toPrintable())

	companion object {
		@OptIn(ExperimentalUnsignedTypes::class)
		@Throws(IllegalArgumentException::class)
		fun Tlv.toPublicKeyReference(tag: Tag): PublicKeyReference {
			require(this.tag == tag) { "The tag of the TLV ($tag) is not the expected tag." }
			return when (this) {
				is TlvPrimitive -> {
					val data = value.toByteArray()
					val country = data.decodeToString(0, 2)
					val holder = data.decodeToString(2, data.size - 5)
					val sequence = data.decodeToString(data.size - 5)
					PublicKeyReference(country, holder, sequence)
				}
				else -> throw IllegalArgumentException("PublicKeyReference TLV is not primitive")
			}
		}
	}
}
