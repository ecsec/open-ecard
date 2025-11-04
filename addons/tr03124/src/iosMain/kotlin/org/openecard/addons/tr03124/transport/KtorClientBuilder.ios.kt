package org.openecard.addons.tr03124.transport

import io.ktor.client.HttpClient
import org.openecard.addons.tr03124.xml.TcToken

actual fun newKtorClientBuilder(certTracker: EserviceCertTracker): KtorClientBuilder =
	CertTrackingClientBuilder(certTracker)

class CertTrackingClientBuilder(
	certTracker: EserviceCertTracker,
) : KtorClientBuilder {
	override val tokenClient: HttpClient
		get() = TODO("Not yet implemented")
	override val redirectClient: HttpClient
		get() = TODO("Not yet implemented")
	override val checkCertClient: CertValidationClient
		get() = TODO("Not yet implemented")

	override fun buildEidServerClient(token: TcToken.TcTokenOk): HttpClient {
		TODO("Not yet implemented")
	}
}
