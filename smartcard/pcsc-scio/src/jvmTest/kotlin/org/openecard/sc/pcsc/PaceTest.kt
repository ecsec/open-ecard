package org.openecard.sc.pcsc

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions
import org.openecard.sc.iface.feature.PaceCapability
import org.openecard.sc.iface.feature.PaceEstablishChannelRequest
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.iface.withContext
import org.openecard.sc.pcsc.testutils.WhenPcscStack
import kotlin.test.Test
import kotlin.test.assertTrue

@WhenPcscStack
class PaceTest {
	@Test
	fun `get terminal features`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { "^REINER SCT cyberJack RFID (standard|komfort).*".toRegex().matches(it.name) }
					?: Assumptions.abort { "Necessary terminal not available" }
			Assumptions.assumeTrue(terminal.isCardPresent()) { "Terminal does not contain a card" }

			val con = terminal.connect()
			val features = con.getFeatures()
			val paceFeature = features.filterIsInstance<PaceFeature>().first()
			val capabilities = paceFeature.getPaceCapabilities()

			assertTrue { PaceCapability.GENERIC_PACE in capabilities }
			assertTrue { PaceCapability.GERMAN_EID in capabilities }
			// QES only present in komfort reader

			con.beginTransaction()
			val paceRequest = PaceEstablishChannelRequest(PacePinId.CAN, null, null, null)
			val paceResp = runBlocking { paceFeature.establishChannel(paceRequest) }
		}
	}
}
