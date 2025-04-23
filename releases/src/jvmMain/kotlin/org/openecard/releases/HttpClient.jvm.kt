package org.openecard.releases

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.logging.Logging

actual fun createHttpClient(): HttpClient =
	HttpClient(CIO) {
		// Configure client here
		install(Logging)
// 		install(ContentNegotiation) {
// 			json()
// 		}
	}
