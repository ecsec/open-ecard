/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.asn1.ASN1ObjectIdentifier
import org.openecard.bouncycastle.asn1.ASN1OctetString
import org.openecard.bouncycastle.asn1.ASN1Sequence
import org.openecard.bouncycastle.asn1.ASN1Set
import org.openecard.bouncycastle.asn1.ASN1String
import org.openecard.bouncycastle.asn1.DERTaggedObject
import org.openecard.crypto.common.asn1.eac.oid.CVCertificatesObjectIdentifier
import java.io.IOException
import java.nio.charset.Charset
import java.security.cert.CertificateException

private val LOG = KotlinLogging.logger { }

/**
 * See BSI-TR-03110, version 2.10, part 3, section C.
 *
 * <pre>
 * CertificateDescription ::= SEQUENCE {
 * descriptionType OBJECT IDENTIFIER,
 * issuerName [1] UTF8String,
 * issuerURL [2] PrintableString OPTIONAL,
 * subjectName [3] UTF8String,
 * subjectURL [4] PrintableString OPTIONAL,
 * termsOfUsage [5] ANY DEFINED BY descriptionType,
 * redirectURL [6] PrintableString OPTIONAL,
 * commCertificates [7] SET OF OCTET STRING OPTIONAL
 * }
</pre> *
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class CertificateDescription private constructor(
	seq: ASN1Sequence,
) {
	private val descriptionType: String

	/**
	 * Returns the IssuerName.
	 *
	 * @return IssuerName
	 */
	var issuerName: String? = null
		private set

	/**
	 * Returns the IssuerURL.
	 *
	 * @return IssuerURL
	 */
	var issuerURL: String? = null
		private set

	/**
	 * Returns the SubjectName.
	 *
	 * @return SubjectName
	 */
	var subjectName: String? = null
		private set

	/**
	 * Returns the SubjectURL.
	 *
	 * @return SubjectURL
	 */
	var subjectURL: String? = null
		private set
	private var termsOfUsage: String? = null
	private var termsOfUsageBytes: ByteArray? = null

	/**
	 * Returns the RedirectURL.
	 *
	 * @return RedirectURL
	 */
	var redirectURL: String? = null
		private set
	private var commCertificates: List<ByteArray>? = null

	/**
	 * Returns the certificate description as a byte array.
	 *
	 * @return Certificate description as a byte array
	 */
	val encoded: ByteArray
	private var termsOfUsageMimeType: String? = null

	/**
	 * Creates a new CertificateDescription.
	 *
	 * @param seq Encoded CertificateDescription
	 */
	init {
		try {
			encoded = seq.encoded
			val elements = seq.objects
			descriptionType = ASN1ObjectIdentifier.getInstance(elements.nextElement()).toString()

			while (elements.hasMoreElements()) {
				val taggedObject = DERTaggedObject.getInstance(elements.nextElement())
				val tag = taggedObject.getTagNo()
				val obj = taggedObject.getObject()

				when (tag) {
					1 -> issuerName = (obj as ASN1String).string
					2 -> issuerURL = (obj as ASN1String).string
					3 -> subjectName = (obj as ASN1String).string
					4 -> subjectURL = (obj as ASN1String).string
					5 ->
						when (descriptionType) {
							CVCertificatesObjectIdentifier.id_plainFormat -> {
								termsOfUsageMimeType = "text/plain"
								termsOfUsage = (obj as ASN1String).string
							}

							CVCertificatesObjectIdentifier.id_htmlFormat -> {
								termsOfUsageMimeType = "text/html"
								termsOfUsage = (obj as ASN1String).string
							}

							CVCertificatesObjectIdentifier.id_pdfFormat -> {
								termsOfUsageMimeType = "application/pdf"
								termsOfUsageBytes = (obj as ASN1OctetString).octets
							}
						}

					6 -> redirectURL = (obj as ASN1String).string
					7 -> {
						val commCerts = (obj as ASN1Set).objects
						commCertificates = commCerts.toList().map { (it as ASN1OctetString).octets }
					}

					else -> throw IllegalArgumentException("Unknown object in CertificateDescription.")
				}
			}
		} catch (e: IOException) {
			LOG.error(e) { "Cannot parse CertificateDescription." }
			throw CertificateException("Cannot parse CertificateDescription.")
		}
	}

	/**
	 * Returns DescriptionType.
	 *
	 * @return DescriptionType
	 */
	fun getDescriptionType(): String = descriptionType

	/**
	 * Returns the TermsOfUsage.
	 *
	 * @return TermsOfUsage
	 */
	@Deprecated("")
	fun getTermsOfUsage(): Any? = termsOfUsage

	val termsOfUsageString: String
		/**
		 * Get the terms of usage as String.
		 *
		 * @return The terms of usage as string.
		 * @throws IllegalStateException If the mimeType of the terms of usage is application/pdf.
		 */
		get() = termsOfUsage ?: throw IllegalStateException("Terms of usage are not available in a string type.")

	/**
	 * Get the terms of usage as byte array.
	 * <br></br>
	 * The intension of this method is to serve the bytes of the terms of usage in case they are in pdf format. If the
	 * terms of usage are in `plain text` or `HTML` format (represented by a String) the getBytes method of
	 * the String object is invoked with the UTF-8 charset.
	 *
	 * @return The terms of usage as byte array.
	 */
	fun getTermsOfUsageBytes(): ByteArray {
		if (termsOfUsageBytes != null) {
			return termsOfUsageBytes!!
		} else if (termsOfUsage != null) {
			return termsOfUsage!!.toByteArray(Charset.forName("UTF-8"))
		} else {
			throw IllegalStateException("No terms of use available.")
		}
	}

	/**
	 * Returns the CommCertificates.
	 *
	 * @return CommCertificates
	 */
	fun getCommCertificates(): List<ByteArray> = commCertificates!!

	/**
	 * Get the MimeType of the Terms of Usage in the Certificate Description.
	 *
	 * @return The MimeType of the terms of usage. The possible values are:
	 * <br></br>
	 *
	 *  * text/plain
	 *  * text/html
	 *  * application/pdf
	 *
	 */
	fun getTermsOfUsageMimeType(): String = termsOfUsageMimeType!!

	val isTermsOfUsagePdf: Boolean
		/**
		 * Indicates whether the Terms of Usage are in PDF format.
		 *
		 * @return `TRUE` if the Terms of Usage are in PDF format else `FALSE`.
		 */
		get() = termsOfUsageMimeType == "application/pdf"

	val isTermsOfUsageHtml: Boolean
		/**
		 * Indicates whether the Terms of Usage are in HTML format.
		 *
		 * @return `TRUE` if the Terms of Usage are in HTML format else `FALSE`.
		 */
		get() = termsOfUsageMimeType == "text/html"

	val isTermsOfUsageText: Boolean
		/**
		 * Indicates whether the Terms of Usage are in plain text format.
		 *
		 * @return `TRUE` if the Terms of Usage are in plain text format else `FALSE`.
		 */
		get() = termsOfUsageMimeType == "text/plain"

	companion object {
		/**
		 * Creates a new CertificateDescription.
		 *
		 * @param obj Encoded CertificateDescription
		 * @return CertificateDescription
		 * @throws CertificateException
		 */
		@JvmStatic
		@Throws(CertificateException::class)
		fun getInstance(obj: Any): CertificateDescription {
			if (obj is CertificateDescription) {
				return obj
			} else if (obj is ASN1Set) {
				return CertificateDescription(obj as ASN1Sequence)
			} else if (obj is ByteArray) {
				try {
					return CertificateDescription((ASN1Sequence.fromByteArray(obj) as ASN1Sequence?)!!)
				} catch (e: IOException) {
					LOG.error(e) { "Cannot parse CertificateDescription" }
					throw IllegalArgumentException("Cannot parse CertificateDescription")
				}
			}
			throw IllegalArgumentException("Unknown object in factory: " + obj.javaClass)
		}
	}
}
