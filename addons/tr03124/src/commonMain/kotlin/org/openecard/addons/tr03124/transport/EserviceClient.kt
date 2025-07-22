package org.openecard.addons.tr03124.transport

import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.TcToken
import org.openecard.addons.tr03124.Tr03124Binding

interface EserviceClient {
	suspend fun fetchToken(tokenUrl: String): TcToken

	fun setCertificateDescription(certDescription: Any)

	suspend fun redirectToEservice(): BindingResponse

	suspend fun redirectToEservice(
		minorError: String,
		errorMsg: String?,
	): BindingResponse
}
