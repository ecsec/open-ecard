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

import io.github.oshai.kotlinlogging.KotlinLogging
import java.security.cert.CertificateException
import java.util.*

private val LOG = KotlinLogging.logger { }

/**
 * Implements a chain of Card Verifiable Certificates.
 *
 * See BSI-TR-03110, version 2.10, part 3, section 2.
 *
 * See BSI-TR-03110, version 2.10, part 3, section C.
 * This class only accepts one terminal certificate. All further instances must be of type DV or CA.
 *
 * @author Moritz Horsch
 */
class CardVerifiableCertificateChain(
	certificates: List<CardVerifiableCertificate>,
) {
	private val certs = ArrayList<CardVerifiableCertificate>()
	private val cvcaCerts = ArrayList<CardVerifiableCertificate>()
	private val dvCerts = ArrayList<CardVerifiableCertificate>()

	/**
	 * Returns the certificates of the terminal.
	 *
	 * @return Terminal certificates
	 */
	var terminalCertificate: CardVerifiableCertificate? = null
		private set

	/**
	 * Creates a new certificate chain.
	 *
	 * @param certificates Certificates
	 * @throws CertificateException
	 */
	init {
		parseChain(certificates)
		// FIXME not working yet with all servers.
		// verify();
		LOG.warn { "Verification of the certificate chain is disabled." }
	}

	/**
	 * Parses the certificate chain.
	 *
	 * @param certificates Certificates
	 */
	@Throws(CertificateException::class)
	private fun parseChain(certificates: List<CardVerifiableCertificate>) {
		for (cvc in certificates) {
			if (containsCertificate(cvc)) {
				continue
			}

			val role = cvc.cHAT.role

			if (role == CHAT.Role.CVCA) {
				cvcaCerts.add(cvc)
				certs.add(cvc)
			} else if (role == CHAT.Role.DV_OFFICIAL || role == CHAT.Role.DV_NON_OFFICIAL) {
				dvCerts.add(cvc)
				certs.add(cvc)
			} else if (
				role == CHAT.Role.AUTHENTICATION_TERMINAL ||
				role == CHAT.Role.INSPECTION_TERMINAL ||
				role == CHAT.Role.SIGNATURE_TERMINAL
			) {
				if (this.terminalCertificate == null) {
					this.terminalCertificate = cvc
					certs.add(cvc)
				}
			} else {
				throw CertificateException("Malformed certificate.")
			}
		}
	}

	/**
	 * Verifies the certificate chain.
	 * [1] The CAR and the CHR of the CVCA certificates should be equal.
	 * [2] The CAR of a DV certificate should refer to the CHR of a CVCA certificate.
	 * [3] The CAR of a terminal certificate should refer to the CHR of a DV certificate.
	 *
	 * @throws CertificateException
	 */
	@Throws(CertificateException::class)
	fun verify() {
		verify(this.terminalCertificate.toList(), dvCerts)
		verify(dvCerts, cvcaCerts)
		verify(cvcaCerts, cvcaCerts)
	}

	@Throws(CertificateException::class)
	private fun verify(
		authorities: List<CardVerifiableCertificate>,
		holders: List<CardVerifiableCertificate>,
	) {
		val ai = authorities.iterator()
		while (ai.hasNext()) {
			val authority = ai.next()

			for (holder in holders) {
				if (authority.cAR == holder.cHR) {
					break
				}

				if (!ai.hasNext()) {
					val msg = "Malformed certificate chain: Cannot find a CHR for the CAR (${authority.cAR})."
					throw CertificateException(msg)
				}
			}
		}
	}

	/**
	 * Checks if the certificate chain contains the given certificate.
	 *
	 * @param cvc Certificate
	 * @return True if the chain contains the certificate, false otherwise
	 */
	fun containsCertificate(cvc: CardVerifiableCertificate): Boolean {
		for (c in certs) {
			if (c.compare(cvc)) {
				return true
			}
		}
		return false
	}

	/**
	 * Adds a new certificate to the chain.
	 *
	 * @param certificate Certificate
	 * @throws CertificateException
	 */
	@Throws(CertificateException::class)
	fun addCertificate(certificate: CardVerifiableCertificate) {
		parseChain(certificate.toList())
	}

	/**
	 * Adds new certificates to the chain.
	 *
	 * @param certificates Certificate
	 * @throws CertificateException
	 */
	@Throws(CertificateException::class)
	fun addCertificates(certificates: List<CardVerifiableCertificate>) {
		parseChain(certificates)
	}

	val cVCACertificates: MutableList<CardVerifiableCertificate>
		/**
		 * Returns the certificates of the Country Verifying CAs (CVCA).
		 *
		 * @return CVCA certificates
		 */
		get() = cvcaCerts

	val dVCertificates: MutableList<CardVerifiableCertificate>
		/**
		 * Returns the certificates of the Document Verifiers (DV).
		 *
		 * @return DV certificates
		 */
		get() = dvCerts

	val certificates: MutableList<CardVerifiableCertificate>
		/**
		 * Returns the certificate chain.
		 *
		 * @return Certificate chain
		 */
		get() = certs

	/**
	 * Returns the certificate chain from the CAR.
	 *
	 * @param car Certification Authority Reference (CAR)
	 * @return Certificate chain
	 * @throws CertificateException
	 */
	@Throws(CertificateException::class)
	fun getCertificateChainFromCAR(car: ByteArray): CardVerifiableCertificateChain =
		getCertificateChainFromCAR(PublicKeyReference(car))

	/**
	 * Returns the certificate chain from the CAR.
	 *
	 * @param car Certification Authority Reference (CAR)
	 * @return Certificate chain
	 * @throws CertificateException
	 */
	@Throws(CertificateException::class)
	fun getCertificateChainFromCAR(car: PublicKeyReference): CardVerifiableCertificateChain {
		val certChain = buildChain(certs, car)
		return CardVerifiableCertificateChain(certChain)
	}

	private fun buildChain(
		certs: List<CardVerifiableCertificate>,
		car: PublicKeyReference,
	): List<CardVerifiableCertificate> {
		val certChain = mutableListOf<CardVerifiableCertificate>()

		for (c in certs) {
			if (c.cAR.compare(car)) {
				certChain.add(c)
				certChain.addAll(buildChain(certs, c.cHR))
			}
		}

		return certChain
	}
}

private fun CardVerifiableCertificate?.toList(): List<CardVerifiableCertificate> =
	this?.let {
		listOf(it)
	} ?: emptyList()
