/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

package org.openecard.crypto.tls.auth

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.bouncycastle.tls.*
import org.openecard.crypto.tls.CertificateVerifier
import org.openecard.crypto.tls.verify.CertificateVerifierBuilder
import org.openecard.crypto.tls.verify.ExpirationVerifier
import org.openecard.crypto.tls.verify.HostnameVerifier
import org.openecard.crypto.tls.verify.KeyLengthVerifier
import java.io.IOException

private val LOG = KotlinLogging.logger { }

/**
 * Implementation of the TlsAuthentication interface for certificate verification.
 *
 * @author Tobias Wich
 */
class DynamicAuthentication
/**
 * Create a new DynamicAuthentication using the given parameters.
 * They can later be changed using the setter functions.
 *
 * @param hostname Name or IP of the host that will be used for certificate validation when a verifier is set.
 * @param certVerifier Verifier used for server certificate checks.
 * @param credentialFactory Factory that provides client credentials when they are requested from the server.
 */
(
	private var hostname: String,
	private var certVerifier: CertificateVerifier?,
	private var credentialFactory: CredentialFactory?,
) : TlsAuthentication,
	ContextAware {
	/**
	 * Returns the certificate chain which is processed during the TLS authentication.
	 *
	 * @return The certificate chain of the last certificate validation or null if none is available.
	 */
	var serverCertificate: TlsServerCertificate? = null
		private set
	private var context: TlsContext? = null

	/**
	 * Nullary constructor.
	 * If no parameters are set later through setter functions, this instance will perform no server certificate checks
	 * and return an empty client certificate list.
	 *
	 * @param hostName Name or IP of the host that will be used for certificate validation when a verifier is set.
	 */
	constructor(hostName: String) : this(
		hostName,
		CertificateVerifierBuilder()
			.and(HostnameVerifier())
			.and(KeyLengthVerifier())
			.and(ExpirationVerifier())
			.build(),
		null,
	)

	override fun setContext(context: TlsContext) {
		this.context = context
	}

	/**
	 * Sets the host name for the certificate verification step.
	 *
	 * @see .notifyServerCertificate
	 * @param hostname Name or IP of the host that will be used for certificate validation, when a verifier is set.
	 */
	fun setHostname(hostname: String) {
		this.hostname = hostname
	}

	/**
	 * Sets the implementation for the certificate verification step.
	 *
	 * @see .notifyServerCertificate
	 * @see CertificateVerifier
	 *
	 * @param certVerifier Verifier to use for server certificate checks.
	 */
	fun setCertificateVerifier(certVerifier: CertificateVerifier?) {
		this.certVerifier = certVerifier
	}

	/**
	 * Adds a certificate verifier to the chain of the certificate verifiers.
	 *
	 * @param certVerifier The verifier to add.
	 */
	fun addCertificateVerifier(certVerifier: CertificateVerifier) {
		var builder = CertificateVerifierBuilder()
		this.certVerifier?.let {
			builder = builder.and(it)
		}
		this.certVerifier = builder.and(certVerifier).build()
	}

	/**
	 * Sets the factory which is used to find and create a credential reference for the authentication.
	 *
	 * @see .getClientCredentials
	 * @param credentialFactory Factory that provides client credentials when they are requested from the server.
	 */
	fun setCredentialFactory(credentialFactory: CredentialFactory?) {
		this.credentialFactory = credentialFactory
	}

	/**
	 * Verify the server certificate of the TLS handshake.
	 * In case no implementation is set (via [.setCertificateVerifier]), no action is
	 * performed.<br></br>
	 * The actual implementation is responsible for the types of verification that are performed. Besides the usual
	 * hostname and certificate chain verification, those types could also include CRL and OCSP checking.
	 *
	 * @see CertificateVerifier
	 *
	 * @param serverCert Certificate chain of the server as transmitted in the TLS handshake.
	 * @throws IOException when certificate verification failed.
	 */
	@Throws(IOException::class)
	override fun notifyServerCertificate(serverCert: TlsServerCertificate?) {
		val noServerCert = serverCert == null || serverCert.certificate == null || serverCert.certificate.isEmpty
		if (noServerCert) {
			throw TlsFatalAlert(AlertDescription.handshake_failure)
		} else {
			// save server certificate
			this.serverCertificate = serverCert
			// try to validate
			if (certVerifier != null) {
				// perform validation depending on the available parameters
				certVerifier!!.isValid(serverCert, hostname)
			} else {
				// no verifier available
				LOG.warn { "No certificate verifier available, skipping certificate verification." }
			}
		}
	}

	/**
	 * Gets the client credentials based on the credential factory saved in this instance, or an empty credential.
	 * From RFC 4346 sec. 7.4.6:
	 *
	 * If no suitable certificate is available, the client SHOULD send a certificate message containing no
	 * certificates.
	 *
	 * @param cr Certificate request as received in the TLS handshake.
	 * @see CredentialFactory
	 */
	override fun getClientCredentials(cr: CertificateRequest): TlsCredentials? {
		if (credentialFactory != null) {
			if (credentialFactory is ContextAware) {
				(credentialFactory as ContextAware).setContext(context!!)
			}
			val credentials = credentialFactory!!.getClientCredentials(cr)
			if (!credentials.isEmpty()) {
				val cred = credentials[0]
				// in case the credential understands the context supply it
				if (cred is ContextAware) {
					(cred as ContextAware).setContext(context!!)
				}
				return cred
			}
		}
		// fall back to no auth, when no credential is found
		return null
	}
}
