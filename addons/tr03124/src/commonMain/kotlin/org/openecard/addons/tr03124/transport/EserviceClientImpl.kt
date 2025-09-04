package org.openecard.addons.tr03124.transport

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.xml.StartPaos
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.addons.tr03124.xml.TcToken.Companion.toTcToken
import kotlin.random.Random

internal class EserviceClientImpl(
	override val certTracker: EserviceCertTracker,
	private val serviceClient: KtorClientBuilder,
	private val random: Random = Random.Default,
) : EserviceClient {
	private var token: TcToken? = null

	override suspend fun fetchToken(tokenUrl: String): TcToken {
		check(token == null) { "Fetching multiple TCTokens with the same client" }
		val tokenClient = serviceClient.tokenClient
		val tokenStr =
			tokenClient
				.get(tokenUrl) {
					headers {
						append(HttpHeaders.Accept, "text/xml, */*;q=0.8")
					}
				}.bodyAsText()
		return tokenStr.toTcToken()
	}

	override fun buildEidServerInterface(startPaos: StartPaos): EidServerInterface {
		val token = checkNotNull(token) { "Trying to build eID-Server client without fetching TCToken first" }
		val paosClient = serviceClient.buildEidServerClient(token)
		return EidServerPaos(this, token.serverAddress, paosClient, startPaos, random)
	}

	override suspend fun redirectToEservice(): BindingResponse {
		TODO("Not yet implemented")
	}

	override suspend fun redirectToEservice(
		minorError: String,
		errorMsg: String?,
	): BindingResponse {
		TODO("Not yet implemented")
	}
}
