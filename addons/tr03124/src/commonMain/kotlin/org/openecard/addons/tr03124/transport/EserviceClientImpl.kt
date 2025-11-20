package org.openecard.addons.tr03124.transport

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.isSuccess
import org.openecard.addons.tr03124.BindingException
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.InvalidServerData
import org.openecard.addons.tr03124.NoTcToken
import org.openecard.addons.tr03124.UnkownServerError
import org.openecard.addons.tr03124.runEacCatching
import org.openecard.addons.tr03124.xml.StartPaos
import org.openecard.addons.tr03124.xml.TcToken
import org.openecard.addons.tr03124.xml.TcToken.Companion.toTcToken
import org.openecard.addons.tr03124.xml.TcTokenXml.Companion.parseTcToken
import kotlin.random.Random

private val log = KotlinLogging.logger { }

internal class EserviceClientImpl(
	override val tcTokenUrl: String,
	override val certTracker: EserviceCertTracker,
	private val serviceClient: KtorClientBuilder,
	private val random: Random = Random.Default,
) : EserviceClient {
	private var checkedTokenUrl: String? = null
	private var token: TcToken? = null
	private var tokenOk: TcToken.TcTokenOk? = null

	override suspend fun fetchToken(): TcToken.TcTokenOk {
		log.info { "Fetching TCToken from '$tcTokenUrl'" }
		if (!tcTokenUrl.startsWith("https://")) {
			throw InvalidServerData(this, "Insecure TCToken URL used: $tcTokenUrl")
		}
		check(token == null) { "Fetching multiple TCTokens with the same client" }
		this.checkedTokenUrl = tcTokenUrl
		val tokenClient = serviceClient.tokenClient
		val tokenRes =
			tokenClient
				.get(tcTokenUrl) {
					headers {
						append(HttpHeaders.Accept, "text/xml, */*;q=0.8")
					}
				}
		if (!tokenRes.status.isSuccess()) {
			throw NoTcToken("TCToken retrieval failed with HTTP error code ${tokenRes.status.value}")
		}

		val tokenStr = tokenRes.bodyAsText()
		try {
			val receivedToken = tokenStr.parseTcToken().toTcToken()
			token = receivedToken

			// make some basic checks as specified in TR-03124-1, Sec.
			val tokenOk = checkToken(receivedToken)
			this.tokenOk = tokenOk
			return tokenOk
		} catch (ex: IllegalArgumentException) {
			throw InvalidServerData(this, "The retrieved TCToken could not be parsed correctly", ex)
		}
	}

	@Throws(BindingException::class)
	private fun checkToken(token: TcToken): TcToken.TcTokenOk {
		log.info { "Performing basic validation checks on received TCToken" }
		when (token) {
			is TcToken.TcTokenOk -> {
				val nonHttpsUrl =
					listOfNotNull(token.refreshAddress, token.serverAddress, token.communicationErrorAddress).any {
						!it.startsWith("https://")
					}
				if (nonHttpsUrl) {
					throw InvalidServerData(this, "Insecure URL scheme used in TCToken")
				}
				return token
			}
			is TcToken.TcTokenError -> {
				if (!token.communicationErrorAddress.startsWith("https://")) {
					// forbid to use this address by deleting the token
					this.token = null
					throw InvalidServerData(this, "Insecure URL scheme used in TCToken")
				}
				throw UnkownServerError(this, "Server aborted by sending an error TCToken")
			}
		}
	}

	override fun buildEidServerInterface(startPaos: StartPaos): EidServerInterface {
		log.info { "Creating PAOS client" }
		val token = checkNotNull(tokenOk) { "Trying to build eID-Server client without fetching TCToken first" }
		val paosClient = serviceClient.buildEidServerClient(token)
		return EidServerPaos(this, token.serverAddress, paosClient, startPaos, random)
	}

	override suspend fun redirectToEservice(): BindingResponse =
		determineRefreshUrl()?.let {
			BindingResponse.RedirectResponse(HttpStatusCode.SeeOther.value, it.addOk())
		} ?: reportCommunicationError()
			?: returnCommunicationErrorPage()

	override suspend fun redirectToEservice(
		minorError: String,
		errorMsg: String?,
	): BindingResponse =
		determineRefreshUrl()?.let {
			BindingResponse.RedirectResponse(HttpStatusCode.SeeOther.value, it.addError(minorError, errorMsg))
		} ?: reportCommunicationError()
			?: returnCommunicationErrorPage()

	private suspend fun <T> ignoreCertErrors(block: suspend () -> T): T? =
		try {
			runEacCatching(this, null) {
				block()
			}
		} catch (ex: BindingException) {
			// ignore and return null
			log.warn(ex) { "Failure during refresh determination" }
			null
		}

	private suspend fun determineRefreshUrl(): String? {
		log.info { "Determining refresh URL" }
		return ignoreCertErrors {
			val tokenUrl = checkedTokenUrl
			val token = tokenOk
			if (tokenUrl == null || token == null) {
				// no refresh detection possible as there is no token
				log.info { "No refresh URL determination possible as there is no non-error TCToken" }
				return@ignoreCertErrors null
			}

			var nextAddr = token.refreshAddress
			log.debug { "Checking URL '$nextAddr'" }

			// if SOP matches, we only need to check the certificate
			if (certTracker.matchesSop(tokenUrl, nextAddr)) {
				log.debug { "SOP matches, checking certificate" }
				val certClient = serviceClient.checkCertClient
				// check certificate
				certClient.checkCert(nextAddr)

				log.info { "Refresh URL is '$nextAddr'" }
				return@ignoreCertErrors nextAddr
			}

			val cl = serviceClient.redirectClient
			// follow redirects
			var finalUrl: String? = null
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
					return@ignoreCertErrors null
				}
				val newUrl =
					resp.headers["Location"] ?: // missing location
						return@ignoreCertErrors null

				if (certTracker.matchesSop(tokenUrl, nextAddr)) {
					log.info { "Refresh URL is '$nextAddr'" }
					// set result, so loop terminates
					finalUrl = newUrl
				} else if (!newUrl.startsWith("https://")) {
					log.warn { "Received non https URL in redirect: $newUrl" }
					return@ignoreCertErrors null
				} else {
					nextAddr = newUrl
					log.debug { "Checking URL '$nextAddr'" }
				}
			} while (finalUrl == null)

			finalUrl
		}
	}

	private fun reportCommunicationError() =
		token?.communicationErrorAddress?.let {
			BindingResponse.RedirectResponse(HttpStatusCode.SeeOther.value, it.addError("communicationError"))
		}

	private fun returnCommunicationErrorPage() =
		BindingResponse.ReferencedContentResponse(
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
