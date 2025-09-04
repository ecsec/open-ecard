package org.openecard.addons.tr03124.transport

import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.xml.StartPaos
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.sc.pace.cvc.CertificateDescription

interface EserviceClient {
	val certTracker: EserviceCertTracker

	@Throws(BindingException::class)
	suspend fun fetchToken(tokenUrl: String): TcToken

	fun buildEidServerInterface(startPaos: StartPaos): EidServerInterface

	suspend fun redirectToEservice(): BindingResponse

	suspend fun redirectToEservice(
		minorError: String,
		errorMsg: String? = null,
	): BindingResponse
}
