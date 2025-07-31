package org.openecard.addons.tr03124.transport

import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.sc.pace.cvc.CertificateDescription

interface EserviceClient {
	suspend fun fetchToken(tokenUrl: String): TcToken

	@OptIn(ExperimentalUnsignedTypes::class)
	fun addCertificateForValidation(certificateHash: UByteArray)

	fun setCertificateDescription(certDescription: CertificateDescription)

	suspend fun redirectToEservice(): BindingResponse

	suspend fun redirectToEservice(
		minorError: String,
		errorMsg: String?,
	): BindingResponse
}
