package org.openecard.addons.tr03124.transport

import org.openecard.addons.tr03124.xml.AuthenticationProtocolData
import org.openecard.addons.tr03124.xml.DidAuthenticateRequest
import org.openecard.addons.tr03124.xml.Eac1Input
import org.openecard.addons.tr03124.xml.RequestType
import org.openecard.addons.tr03124.xml.ResponseType
import org.openecard.addons.tr03124.xml.StartPaos
import org.openecard.addons.tr03124.xml.TcToken
import kotlin.random.Random

internal class EidServerPaos(
	val token: TcToken,
	val eserviceClient: EserviceClient,
	val startPaos: StartPaos,
	private val random: Random = Random.Default,
) : EidServerInterface {
	init {
		TODO("configure server connection based on PSK, or existing eService connection")
	}

	override suspend fun start(): DidAuthenticateRequest {
		TODO("connect and set certificate in eservice client")
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
