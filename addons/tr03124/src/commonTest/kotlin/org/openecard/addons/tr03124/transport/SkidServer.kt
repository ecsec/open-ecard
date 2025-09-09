package org.openecard.addons.tr03124.transport

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable

class SkidServer(
	val idsUrl: String,
	val fsCidUrl: String,
	val brokerUrl: String,
	val client: HttpClient =
		HttpClient(CIO) {
			install(HttpCookies)
			install(ContentNegotiation) {
				json()
			}
			install(Logging) {
				level = LogLevel.ALL
			}
			followRedirects = false
		},
) {
	companion object {
		fun forSystem(baseUrl: String) = SkidServer("$baseUrl/idm/w3", "$baseUrl/fs", "$baseUrl/broker")

		fun forProdSystem() = forSystem(PROD)

		fun forStageSystem() = forSystem(STAGE)

		const val PROD = "https://service.skidentity.de"
		const val STAGE = "https://service.skidentity-test.de"
	}

	@Serializable
	data class ClaimResponse(
		val flowId: String,
	)

	/**
	 * Loads a new TC Token from the server.
	 */
	suspend fun loadTcTokenUrl(): String {
		val fsToken: String =
			client
				.post("$fsCidUrl/cloudid/api/pub/v1/cip/auth") {
					contentType(ContentType.Application.Json)
					setBody("""{"AuthenticationOptions":[],"RequestedAttributes":[],"CIPCmd":"issue"}""")
				}.body()

		val claimRes: ClaimResponse =
			client
				.post("$brokerUrl/api/pub/v2/session/claim") {
					parameter("broker-token", fsToken)
				}.body()

		val tokenUrl: String =
			client
				.submitForm(
					url = "$brokerUrl/api/pub/v1/options/select",
					formParameters =
						parameters {
							append("flow-id", claimRes.flowId)
							append(
								"user-selection",
								"""
								<?xml version="1.0" encoding="utf-8"?>
								<RequestedAttributes xmlns="urn:oasis:names:tc:SAML:profile:privacy" xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata">
								<md:RequestedAttribute Name="http://www.skidentity.de/att/eIdentifier" isRequired="true"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/FirstName" isRequired="false"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/LastName" isRequired="false"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/Street" isRequired="false"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/StreetNumber" isRequired="false"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/City" isRequired="false"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/State" isRequired="false"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/ZipCode" isRequired="false"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/Country" isRequired="false"></md:RequestedAttribute>
								<md:RequestedAttribute Name="http://www.skidentity.de/att/DateOfBirth" isRequired="false"></md:RequestedAttribute>
								</RequestedAttributes>
								""".trim(),
							)
							append("auth-return-url", "$idsUrl/finish/")
							append("option-group", "npa")
						},
				) {
					accept(ContentType.Text.Plain)
				}.body()

		return tokenUrl
	}
}
