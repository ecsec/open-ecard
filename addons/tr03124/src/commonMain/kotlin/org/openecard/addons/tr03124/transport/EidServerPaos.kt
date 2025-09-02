package org.openecard.addons.tr03124.transport

import io.ktor.client.HttpClient
import org.openecard.addons.tr03124.xml.AuthenticationProtocolData
import org.openecard.addons.tr03124.xml.DidAuthenticateRequest
import org.openecard.addons.tr03124.xml.RequestType
import org.openecard.addons.tr03124.xml.ResponseType
import org.openecard.addons.tr03124.xml.StartPaos

internal class EidServerPaos(
	val serverUrl: String,
	val httpClient: HttpClient,
	val startPaos: StartPaos,
) : EidServerInterface {
	private fun deliverMessage(msg: String) {
		TODO("Implement message delivery")
	}

	override suspend fun start(): DidAuthenticateRequest {
		TODO("Not yet implemented")
	}

	override fun cancel() {
		TODO("Not yet implemented")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	override fun getServerCertificateHash(): UByteArray {
		TODO("Not yet implemented")
	}

	override suspend fun sendDidAuthResponse(protocolData: AuthenticationProtocolData): AuthenticationProtocolData? {
		TODO("Not yet implemented")
	}

	override fun getFirstDataRequest(): RequestType {
		TODO("Not yet implemented")
	}

	override suspend fun sendDataResponse(message: ResponseType): RequestType? {
		TODO("Not yet implemented")
	}
}
