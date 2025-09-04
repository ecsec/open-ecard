package org.openecard.addons.tr03124.transport

interface CertValidationClient {
	@Throws(UntrustedCertificateError::class)
	suspend fun checkCert(url: String)
}
