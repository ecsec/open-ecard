package org.openecard.addons.tr03124.transport

import kotlin.coroutines.cancellation.CancellationException

interface CertValidationClient {
	@Throws(UntrustedCertificateError::class, CancellationException::class)
	suspend fun checkCert(url: String)
}
