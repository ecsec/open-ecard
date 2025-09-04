package org.openecard.addons.tr03124.transport

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.headers
import io.ktor.http.isSuccess
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.InvalidServerData
import org.openecard.addons.tr03124.NoTcToken
import org.openecard.addons.tr03124.UnkownServerError
import org.openecard.addons.tr03124.xml.StartPaos
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.addons.tr03124.xml.TcToken.Companion.toTcToken
import kotlin.random.Random

internal class EserviceClientImpl(
	override val certTracker: EserviceCertTracker,
	private val serviceClient: KtorClientBuilder,
	private val random: Random = Random.Default,
) : EserviceClient {
	private var tokenUrl: String? = null
	private var token: TcToken? = null

	override suspend fun fetchToken(tokenUrl: String): TcToken {
		check(token == null) { "Fetching multiple TCTokens with the same client" }
		this.tokenUrl = tokenUrl
		val tokenClient = serviceClient.tokenClient
		val tokenRes =
			tokenClient
				.get(tokenUrl) {
					headers {
						append(HttpHeaders.Accept, "text/xml, */*;q=0.8")
					}
				}
		if (!tokenRes.status.isSuccess()) {
			throw NoTcToken("TCToken retrieval failed with HTTP error code ${tokenRes.status.value}")
		}

		val tokenStr = tokenRes.bodyAsText()
		val receivedToken = tokenStr.toTcToken()
		token = receivedToken

		// make some basic checks as specified in TR-03124-1, Sec.
		checkToken(receivedToken)

		return receivedToken
	}

	@Throws(BindingException::class)
	private fun checkToken(token: TcToken) {
		// check if this is an error token and we need to stop right away
		if (token.refreshAddress.isEmpty() && token.serverAddress.isEmpty()) {
			throw UnkownServerError(this, "Server aborted by sending an error TCToken")
		}

		val nonHttpsUrl =
			listOfNotNull(token.refreshAddress, token.serverAddress, token.communicationErrorAddress).any {
				!it.startsWith("https://")
			}
		if (nonHttpsUrl) {
			throw InvalidServerData(this, "Insecure URL scheme used in TCToken")
		}
	}

	override fun buildEidServerInterface(startPaos: StartPaos): EidServerInterface {
		val token = checkNotNull(token) { "Trying to build eID-Server client without fetching TCToken first" }
		val paosClient = serviceClient.buildEidServerClient(token)
		return EidServerPaos(this, token.serverAddress, paosClient, startPaos, random)
	}

	override suspend fun redirectToEservice(): BindingResponse =
		determineRefreshUrl()?.let {
			BindingResponse.RedirectResponse(HttpStatusCode.SeeOther.value, it.addOk())
		} ?: reportCommunicationError()

	override suspend fun redirectToEservice(
		minorError: String,
		errorMsg: String?,
	): BindingResponse =
		determineRefreshUrl()?.let {
			BindingResponse.RedirectResponse(HttpStatusCode.SeeOther.value, it.addError(minorError, errorMsg))
		} ?: reportCommunicationError()

	private suspend fun determineRefreshUrl(): String? {
		try {
			val tokenUrl = tokenUrl
			val token = token
			if (tokenUrl == null || token == null) {
				// no refresh detection possible as there is no token
				return null
			}

			var nextAddr = token.refreshAddress

			// if SOP matches, we only need to check the certificate
			if (certTracker.matchesSop(tokenUrl, nextAddr)) {
				val certClient = serviceClient.checkCertClient
				// check certificate
				certClient.checkCert(nextAddr)

				return nextAddr
			}

			val cl = serviceClient.redirectClient
			// follow redirects
			do {
				val resp = cl.get(nextAddr)
				if (resp.status !in
					setOf(
						HttpStatusCode.Found,
						HttpStatusCode.SeeOther,
						HttpStatusCode.TemporaryRedirect,
					)
				) {
					// status code not allowed
					return null
				}
				val newUrl =
					resp.headers["Location"] ?: // missing location
						return null

				if (certTracker.matchesSop(tokenUrl, nextAddr)) {
					return newUrl
				} else {
					nextAddr = newUrl
				}
			} while (true)
		} catch (ex: UntrustedCertificateError) {
			// catch cert errors and return null
			return null
		}
	}

	private fun reportCommunicationError(): BindingResponse =
		token?.communicationErrorAddress?.let {
			BindingResponse.RedirectResponse(HttpStatusCode.SeeOther.value, it.addError("communicationError"))
		}
			?: BindingResponse.ContentResponse(
				HttpStatusCode.BadRequest.value,
				BindingResponse.ContentCode.COMMUNICATION_ERROR,
			)

	private fun String.addOk(): String {
		val refreshUrl = URLBuilder(this)
		refreshUrl.parameters.append("ResultMajor", "ok")
		return refreshUrl.buildString()
	}

	private fun String.addError(
		minorError: String,
		errorMsg: String? = null,
	): String {
		val refreshUrl = URLBuilder(this)
		refreshUrl.parameters.append("ResultMajor", "error")
		refreshUrl.parameters.append("ResultMinor", minorError)
		errorMsg?.let { refreshUrl.parameters.append("ResultMessage", errorMsg) }
		return refreshUrl.buildString()
	}
}
