package org.openecard.releases

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*

actual fun createHttpClient(): HttpClient {
	return HttpClient(CIO) {
		// Configure client here
		install(Logging)
//		install(ContentNegotiation) {
//			json()
//		}
	}
}
