/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/
package org.openecard.crypto.common.asn1.cvc

import org.openecard.common.tlv.TLV
import org.openecard.common.util.ByteUtils
import java.security.cert.CertificateEncodingException
import java.util.Calendar

/**
 * Implements a Card Verifiable Certificate.
 *
 * See BSI-TR-03110, version 2.10, part 3, section C.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class CardVerifiableCertificate(
	cvc: TLV,
) {
	/**
	 * Returns the body of the certificate.
	 *
	 * @return Body
	 */
	val body: ByteArray

	/**
	 * Returns the signature of the certificate.
	 *
	 * @return Signature
	 */
	val signature: ByteArray

	/**
	 * Returns the Certificate Profile Identifier (CPI).
	 *
	 * @return CPI
	 */
	var cPI: ByteArray? = null
		private set

	// Certification Authority Reference (CAR)
	private var car: PublicKeyReference? = null

	// Certificate Holder Reference (CHR)
	private var chr: PublicKeyReference? = null

	// Public key
	private var publicKey: PublicKey? = null

	// Certificate Holder Authorization Template (CHAT)
	private var chat: CHAT? = null

	// Certificate Effective Date
	private var effectiveDate: Calendar? = null

	// Certificate Expiration Date
	private var expirationDate: Calendar? = null

	/**
	 * Returns the certificate extensions.
	 *
	 * @return Extensions
	 */
	var extensions: ByteArray? = null
		private set

	/**
	 * Returns the certificate.
	 *
	 * @return Certificate
	 */
	val certificate: TLV

	/**
	 * Create a new Card Verifiable Certificate.
	 *
	 * @param cvc CardVerifiableCertificate
	 * @throws java.security.cert.CertificateException
	 * @throws org.openecard.common.tlv.TLVException
	 */
	constructor(cvc: ByteArray) : this(TLV.fromBER(cvc))

	/**
	 * Create a new Card Verifiable Certificate.
	 *
	 * @param cvc TLV encoded certificate
	 * @throws java.security.cert.CertificateException
	 */
	init {
		try {
			// TLV encoded body and signature
			certificate = cvc

			// Certificate body
			val bodyObject = cvc.findChildTags(TAG_BODY.toLong())[0]
			body = bodyObject.value

			// Certificate signature
			val signatureObject = cvc.findChildTags(TAG_SIGNATURE.toLong())[0]
			signature = signatureObject.value

			// Certificate body elements
			val bodyElements = bodyObject.getChild().asList()

			for (item in bodyElements) {
				val itemTag = item.tagNumWithClass.toInt()

				when (itemTag) {
					TAG_CPI ->
						this.cPI =
							bodyObject.findChildTags(TAG_CPI.toLong())[0].value

					TAG_CAR ->
						car =
							PublicKeyReference(
								bodyObject.findChildTags(TAG_CAR.toLong())[0].value,
							)

					TAG_PUBLIC_KEY ->
						publicKey =
							PublicKey.Companion.getInstance(
								bodyObject.findChildTags(TAG_PUBLIC_KEY.toLong())[0],
							)

					TAG_CHR ->
						chr =
							PublicKeyReference(
								bodyObject.findChildTags(TAG_CHR.toLong())[0].value,
							)

					TAG_CHAT -> chat = CHAT(bodyObject.findChildTags(TAG_CHAT.toLong())[0])

					TAG_EFFECTIVE_DATE -> {
						val effectiveDateObject =
							bodyObject.findChildTags(TAG_EFFECTIVE_DATE.toLong())[0]
						effectiveDate = parseDate(effectiveDateObject.value)
					}

					TAG_EXPIRATION_DATE -> {
						val expirationDateObject =
							bodyObject.findChildTags(TAG_EXPIRATION_DATE.toLong())[0]
						expirationDate = parseDate(expirationDateObject.value)
					}

					TAG_EXTENSION ->
						extensions =
							bodyObject.findChildTags(TAG_EXTENSION.toLong())[0].value

					else -> {}
				}
			}
			verify()
		} catch (e: Exception) {
			throw CertificateEncodingException("Malformed CardVerifiableCertificates: " + e.message)
		}
	}

	/**
	 * See See BSI-TR-03110, version 2.10, part 3, section C.
	 */
	@Throws(CertificateEncodingException::class)
	private fun verify() {
		if (this.cPI == null ||
			car == null ||
			publicKey == null ||
			chr == null ||
			chat == null ||
			effectiveDate == null ||
			expirationDate == null
		) {
			throw CertificateEncodingException("Malformed CardVerifiableCertificates")
		}
	}

    /*
     * Parses the date.
     * Format YYMMDD (6 Bytes). Note: Januar = 0 not 1!
     */
	private fun parseDate(date: ByteArray): Calendar {
		val cal = Calendar.getInstance()

		cal.set(Calendar.YEAR, 2000 + (date[0] * 10) + date[1])
		cal.set(Calendar.MONTH, (date[2] * 10) + date[3] - 1)
		cal.set(Calendar.DATE, (date[4] * 10) + date[5])

		return cal
	}

	val cHAT: CHAT
		/**
		 * Returns the Certificate Holder Authorization Template (CHAT).
		 *
		 * @return CHAT
		 */
		get() = chat!!

	val cHR: PublicKeyReference
		/**
		 * Returns the Certificate Holder Reference (CHR).
		 *
		 * @return CHR
		 */
		get() = chr!!

	val cAR: PublicKeyReference
		/**
		 * Returns the Certification Authority Reference (CAR).
		 *
		 * @return CAR
		 */
		get() = car!!

	/**
	 * Returns the public key.
	 *
	 * @return Public key
	 */
	fun getPublicKey(): PublicKey = publicKey!!

	/**
	 * Returns the effective date of the certificate.
	 *
	 * @return Effective date
	 */
	fun getEffectiveDate(): Calendar = effectiveDate!!

	/**
	 * Returns the expiration date of the certificate.
	 *
	 * @return Expiration date
	 */
	fun getExpirationDate(): Calendar = expirationDate!!

	/**
	 * Compares the certificate.
	 *
	 * @param certificate Certificate
	 * @return True if the certificate is equal
	 */
	fun compare(certificate: CardVerifiableCertificate): Boolean = ByteUtils.compare(this.signature, certificate.signature)

	companion object {
		// Card Verifiable Certificate
		private const val TAG_CVC = 0x7F21

		// Certificate Body
		private const val TAG_BODY = 0x7F4E

		// Certificate Signature
		private const val TAG_SIGNATURE = 0x5F37

		// Certificate Profile Identifier (CPI)
		private const val TAG_CPI = 0x5F29

		// Certification Authority Reference (CAR)
		private const val TAG_CAR = 0x42

		// Public Key
		private const val TAG_PUBLIC_KEY = 0x7F49

		// Certificate Holder Reference (CHR)
		private const val TAG_CHR = 0x5F20

		// Certificate Holder Authorisation Template (CHAT)
		private const val TAG_CHAT = 0x7F4C

		// Certificate Effective Date
		private const val TAG_EFFECTIVE_DATE = 0x5F25

		// Certificate Expiration Date
		private const val TAG_EXPIRATION_DATE = 0x5F24

		// Certificate Extension
		private const val TAG_EXTENSION = 0x65
	}
}
