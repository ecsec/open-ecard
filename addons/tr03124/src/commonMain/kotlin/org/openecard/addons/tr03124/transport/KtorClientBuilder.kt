package org.openecard.addons.tr03124.transport

import io.ktor.client.HttpClient
import org.openecard.addons.tr03124.xml.TcToken

interface KtorClientBuilder {
	val tokenClient: HttpClient
	val redirectClient: HttpClient

	fun buildEidServerClient(token: TcToken): HttpClient
}

expect fun newKtorClientBuilder(certTracker: EserviceCertTracker): KtorClientBuilder
