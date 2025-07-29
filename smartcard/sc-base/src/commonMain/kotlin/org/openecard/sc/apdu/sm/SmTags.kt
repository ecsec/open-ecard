package org.openecard.sc.apdu.sm

import org.openecard.sc.tlv.Tag

object SmBasicTags {
	/**
	 * Plain value not encoded in BER-TLV
	 */
	val plain = SmTagBoth(0x80u)

	/**
	 * Cryptogram (plain value encoded in BER-TLV and including SM data objects, i.e., an SM field)
	 */
	val plainCryptoWithSmDos = SmTagBoth(0x82u)

	/**
	 * Cryptogram (plain value encoded in BER-TLV, but not including SM data objects)
	 */
	val plainCryptoNoSmDos = SmTagBoth(0x84u)

	/**
	 * Padding-content indicator byte followed by cryptogram (plain value not encoded in BER-TLV)
	 */
	val paddingCrypto = SmTagBoth(0x86u)

	/**
	 * Command header (CLA INS P1 P2, four bytes)
	 */
	val commandHeader = SmTagAuthenticated(0x89u)

	/**
	 * Cryptographic checksum (at least four bytes)
	 */
	val mac = SmTagUnauthenticated(0x8Eu)

	/**
	 * Hash-code
	 */
	val hash = SmTagBoth(0x90u)

	/**
	 * Certificate (data not encoded in BER-TLV)
	 */
	val certificate = SmTagBoth(0x92u)

	/**
	 * Security environment identifier (SEID byte)
	 */
	val securityEnvironment = SmTagBoth(0x94u)

	/**
	 * One or two bytes encoding Ne in the unsecured command-response pair (possibly empty)
	 */
	val expectedLength = SmTagBoth(0x96u)

	/**
	 * Processing status (SW1-SW2, two bytes; possibly empty)
	 */
	val sw = SmTagAuthenticated(0x99u)

	/**
	 * Input data element for the computation of a digital signature (the value field is signed)
	 */
	val signatureInput = SmTagBoth(0x9Au)

	/**
	 * Public key
	 */
	val publicKey = SmTagBoth(0x9Cu)

	/**
	 * Digital signature
	 */
	val signature = SmTagUnauthenticated(0x9Eu)
}

object SmTemplateTags {
	/**
	 * Input template for the computation of a hash-code (the template is hashed)
	 */
	val hashInput = SmTagBoth(0xA0u)

	/**
	 * Input template for the verification of a cryptographic checksum (the template is included)
	 */
	val macInput = SmTagUnauthenticated(0xA2u)

	/**
	 * Control reference template for authentication (AT)
	 */
	val authControl = SmTagBoth(0xA4u)

	/**
	 * Control reference template for key agreement (KAT)
	 */
	val keyAgreeControl = SmTagBoth(0xA6u)

	/**
	 * Input template for the verification of a digital signature (the template is signed)
	 */
	val signatureVerification = SmTagUnauthenticated(0xA8u)

	/**
	 * Control reference template for hash-code (HT)
	 */
	val hashControl = SmTagBoth(0xAAu)

	/**
	 * Input template for the computation of a digital signature (the concatenated value fields are signed)
	 */
	val signatureInputWithValue = SmTagBoth(0xACu)

	/**
	 * Input template for the verification of a certificate (the concatenated value fields are certified)
	 */
	val certificateVerifyInputWithValue = SmTagBoth(0xAEu)

	/**
	 * Plain value encoded in BER-TLV and including SM data objects, i.e., an SM field
	 */
	val plainWithSmDos = SmTagBoth(0xB0u)

	/**
	 * Plain value encoded in BER-TLV, but not including SM data objects
	 */
	val plainNoSmDos = SmTagBoth(0xB2u)

	/**
	 * Control reference template for cryptographic checksum (CCT)
	 */
	val macControl = SmTagBoth(0xB4u)

	/**
	 * Control reference template for digital signature (DST)
	 */
	val signatureControl = SmTagBoth(0xB6u)

	/**
	 * Control reference template for confidentiality (CT)
	 */
	val cryptoControl = SmTagBoth(0xB8u)

	/**
	 * Response descriptor template
	 */
	val responseDescriptor = SmTagBoth(0xBAu)

	/**
	 * Input template for the computation of a digital signature (the template is signed)
	 */
	val signatureInputWithDo = SmTagBoth(0xBCu)

	/**
	 * Input template for the verification of a certificate (the template is certified)
	 */
	val certificateVerifyInputWithDo = SmTagUnauthenticated(0xBEu)
}

sealed interface SmTagList {
	val tags: List<Tag>
}

class SmTagUnauthenticated(
	num: ULong,
) {
	val tag = Tag.forTagNumWithClass(num)
}

class SmTagAuthenticated(
	num: ULong,
) {
	val tag = Tag.forTagNumWithClass(num or 0x1u)
}

class SmTagBoth(
	num: ULong,
) : SmTagList {
	val unauth = SmTagUnauthenticated(num)
	val auth = SmTagAuthenticated(num)
	override val tags: List<Tag> = listOf(unauth.tag, auth.tag)
}
