/****************************************************************************
 * Copyright (C) 2012-2014 HS Coburg.
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
 */
package org.openecard.sal.protocol.eac.anytype

import iso.std.iso_iec._24727.tech.schema.DIDAuthenticationDataType
import org.openecard.binding.tctoken.ex.ErrorTranslations
import org.openecard.common.anytype.AuthDataMap
import org.openecard.common.util.ByteUtils
import org.openecard.common.util.StringUtils
import org.openecard.crypto.common.asn1.cvc.CHAT
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificate
import org.openecard.crypto.common.asn1.cvc.CardVerifiableCertificateChain
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Implements the EAC1InputType data structure.
 * See BSI-TR-03112, version 1.1.2, part 7, section 4.6.5.
 *
 * @author Dirk Petrautzki
 * @author Moritz Horsch
 * @author Tobias Wich
 */
class EAC1InputType(
	baseType: DIDAuthenticationDataType,
) {
	private val authMap: AuthDataMap

	/**
	 * Returns the set of certificates.
	 *
	 * @return Certificates
	 */
	val certificates: ArrayList<CardVerifiableCertificate>

	/**
	 * Returns the certificate description.
	 *
	 * @return Certificate description
	 */
	@get:Deprecated("See BSI TR-03112-7 Sec. 3.6.4.1")
	val certificateDescription: ByteArray?

	/**
	 * Gets the provider info.
	 *
	 * @return Provider info.
	 */
	val providerInfo: ByteArray?

	/**
	 * Returns the required CHAT.
	 *
	 * @return Required CHAT
	 */
	val requiredCHAT: ByteArray

	/**
	 * Returns the optional CHAT.
	 *
	 * @return Optional CHAT
	 */
	val optionalCHAT: ByteArray

	/**
	 * Returns the AuthenticatedAuxiliaryData.
	 *
	 * @return AuthenticatedAuxiliaryData
	 */
	val authenticatedAuxiliaryData: ByteArray?

	/**
	 * Gets the value from the TransactionInfo element.
	 *
	 * @return Value in the TransactionInfo element, or `null` if none is present.
	 */
	val transactionInfo: String?

	/**
	 * Creates a new EAC1InputType.
	 *
	 * @param baseType DIDAuthenticationDataType
	 * @throws Exception Thrown in cause the type iss errornous.
	 */
	init {
		parseCertificateDescriptionElement(baseType)
		authMap = AuthDataMap(baseType)

		certificates = ArrayList<CardVerifiableCertificate>()
		for (element in baseType.getAny()) {
			if (element.localName == CERTIFICATE) {
				val value = StringUtils.toByteArray(element.textContent)
				val cvc = CardVerifiableCertificate(value)
				certificates.add(cvc)
			}
		}
		certificateDescription = authMap.getContentAsBytes(CERTIFICATE_DESCRIPTION)

		providerInfo = authMap.getContentAsBytes(PROVIDER_INFO)

		var requiredCHATtmp: ByteArray? = authMap.getContentAsBytes(REQUIRED_CHAT)
		var optionalCHATtmp: ByteArray? = authMap.getContentAsBytes(OPTIONAL_CHAT)
		// HACK: this is only done because some eID Server vendors send raw CHAT values
		// if not present use empty CHAT, so everything can be deselected
		requiredCHATtmp =
			if (requiredCHATtmp == null) {
				CHAT().toByteArray()
			} else {
				fixChatValue(requiredCHATtmp)
			}
		// if not present, use terminal CHAT as optional
		if (optionalCHATtmp == null) {
			val certChain = CardVerifiableCertificateChain(certificates)
			val terminalCert = certChain.terminalCertificate
			optionalCHATtmp = terminalCert!!.cHAT.toByteArray()
		} else {
			optionalCHATtmp = fixChatValue(optionalCHATtmp)
		}
		requiredCHAT = requiredCHATtmp!!
		optionalCHAT = optionalCHATtmp!!

		authenticatedAuxiliaryData = authMap.getContentAsBytes(AUTHENTICATED_AUXILIARY_DATA)

		transactionInfo = authMap.getContentAsString(TRANSACTION_INFO)
	}

	val outputType: EAC1OutputType
		/**
		 * Returns a new EAC1OutputType.
		 *
		 * @return EAC1OutputType
		 */
		get() = EAC1OutputType(authMap)

	/**
	 * Parse the occurrences of the CertificateDescription.
	 *
	 * Note: The current schema says there could be several CertificateDescriptions. But the Technical Guideline says
	 * there have to be  one single CertificateDescription object.
	 *
	 * @param baseType
	 * @throws ElementParsingException
	 */
	private fun parseCertificateDescriptionElement(baseType: DIDAuthenticationDataType) {
		var counter = 0
		for (element in baseType.getAny()) {
			if (element.localName == CERTIFICATE_DESCRIPTION) {
				counter++
				if (counter > 1) {
					throw ElementParsingException(ErrorTranslations.INVALID_CERT)
				}
			}
		}

		if (counter == 0) {
			throw ElementParsingException(ErrorTranslations.INVALID_CERT)
		}
	}

	companion object {
		private val logger: Logger = LoggerFactory.getLogger(EAC1InputType::class.java)

		const val CERTIFICATE: String = "Certificate"
		const val CERTIFICATE_DESCRIPTION: String = "CertificateDescription"
		const val PROVIDER_INFO: String = "ProviderInfo"
		const val REQUIRED_CHAT: String = "RequiredCHAT"
		const val OPTIONAL_CHAT: String = "OptionalCHAT"
		const val AUTHENTICATED_AUXILIARY_DATA: String = "AuthenticatedAuxiliaryData"
		const val TRANSACTION_INFO: String = "TransactionInfo"

		/**
		 * Adds ASN1 Structure to incomplete CHAT values.
		 * Some eID servers only send the CHAT value itself, but there must an OID and a surrounding ASN1 structure. This
		 * function completes the CHAT value with the AuthenticationTerminal OID.
		 *
		 * @param chat CHAT value, possibly without ASN1 structure.
		 * @return CHAT value with ASN1 structure.
		 */
		private fun fixChatValue(chat: ByteArray): ByteArray? {
			if (chat.size == 5) {
				logger.warn("Correcting invalid CHAT value '{}'.", ByteUtils.toHexString(chat))
				val asn1Prefix = "7F4C12060904007F0007030102025305"
				val prefixBytes = StringUtils.toByteArray(asn1Prefix)
				val result = ByteUtils.concatenate(prefixBytes, chat)
				return result
			} else {
				return chat
			}
		}
	}
}
