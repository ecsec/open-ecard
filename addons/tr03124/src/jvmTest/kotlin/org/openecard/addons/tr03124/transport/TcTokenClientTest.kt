package org.openecard.addons.tr03124.transport

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
			val client = EserviceClientImpl(certTracker, clientFactory)

			val token = client.fetchToken(tokenUrl)
			assertEquals("https://testpaos.governikus-eid.de:443/ecardpaos/paosreceiver", token.serverAddress)
		}
}
