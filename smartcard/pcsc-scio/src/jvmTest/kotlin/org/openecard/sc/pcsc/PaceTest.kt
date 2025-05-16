package org.openecard.sc.pcsc

import org.junit.jupiter.api.Assumptions
import org.openecard.sc.iface.feature.PaceCapability
import org.openecard.sc.iface.feature.PaceFeature
import org.openecard.sc.iface.withContext
import org.openecard.sc.pcsc.testutils.WhenPcscStack
import kotlin.test.Test
import kotlin.test.assertEquals

@WhenPcscStack
class PaceTest {
	@Test
	fun `get terminal features`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID Standard") }
					?: Assumptions.abort { "Necessary terminal not available" }
			val con = terminal.connectTerminalOnly()
			val features = con.getFeatures()
			val paceFeature = features.filterIsInstance<PaceFeature>().first()
			val capabilities = paceFeature.getPaceCapabilities()
			assertEquals(
				setOf(PaceCapability.GENERIC_PACE, PaceCapability.GERMAN_EID, PaceCapability.DESTROY_CHANNEL),
				capabilities,
			)
		}
	}
}
