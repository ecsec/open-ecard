package org.openecard.demo

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json

actual fun governikusClient(): HttpClient = HttpClient(Darwin) { followRedirects = false }

actual fun skidentityClient(): HttpClient =
	HttpClient(Darwin) {
		install(HttpCookies)
		install(ContentNegotiation) {
			json()
		}
		install(Logging) {
			level = LogLevel.ALL
		}
		followRedirects = false
	}
