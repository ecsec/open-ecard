package org.openecard.addons.tr03124.transport

import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import org.openecard.addons.tr03124.xml.TcToken

private val logger = KotlinLogging.logger { }

interface KtorClientBuilder {
	/**
	 * Client for fetching the TCToken.
	 * This client follows redirects and must make sure, that only https is used.
	 */
	val tokenClient: HttpClient

	/**
	 * Client for redirecting to web session.
	 * This client does not follow redirects, as the exact redirect procedure is implemented in [EserviceClient].
	 */
	val redirectClient: HttpClient

	/**
	 * Client to check TLS channel when redirecting to websession.
	 * This client must not perform any HTTP request.
	 * It only checks the certificate against the authorization certificate.
	 */
	val checkCertClient: CertValidationClient

	/**
	 * Returns a client for use with the eID-Server.
	 * Depending on the TCToken, this is an attached client using the channel of the TCToken, or a PSK client.
	 */
	fun buildEidServerClient(token: TcToken.TcTokenOk): HttpClient
}

expect fun newKtorClientBuilder(certTracker: EserviceCertTracker): KtorClientBuilder
