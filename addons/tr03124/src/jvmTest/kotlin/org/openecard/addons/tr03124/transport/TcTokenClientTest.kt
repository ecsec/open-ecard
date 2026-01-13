package org.openecard.addons.tr03124.transport

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.test.Test
import kotlin.test.assertEquals

class TcTokenClientTest {
	@EnabledIfEnvironmentVariable(named = "EXTERNAL_TESTS", matches = "(1|true)")
	@Test
	fun `track certificates`() =
		runTest {
			val tokenUrl = GovernikusTestServer().loadTcTokenUrl()

			val certTracker = EserviceCertTracker()
			val clientFactory = newKtorClientBuilder(certTracker)
			val client = EserviceClientImpl(tokenUrl, certTracker, clientFactory)

			val token = client.fetchToken()
			assertEquals("https://testpaos.governikus-eid.de:443/ecardpaos/paosreceiver", token.serverAddress)
		}

	@Test
	fun testPooling() =
		runTest {
			val certTracker = EserviceCertTracker()
			val b = CertTrackingClientBuilder(certTracker)

			val c1 = b.tokenClient
			// val b11 = c1.get("https://test.governikus-eid.de/").bodyAsText()
			val b12 = c1.get("https://electrologic.org/").bodyAsText()
			val c2 = b.buildAttachedClient()
// 			val b21 = c2.get("https://test.governikus-eid.de/index.html").bodyAsText()
			val b22 = c2.get("https://electrologic.org/").bodyAsText()
		}
}
