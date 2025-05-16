package org.openecard.sc.pcsc

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Disabled
import org.openecard.sc.iface.waitForCardPresent
import org.openecard.sc.iface.withContext
import org.openecard.sc.pcsc.testutils.WhenPcscStack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@WhenPcscStack
class ConnectionTest {
	@Test
	fun `connect context twice`() {
		PcscTerminalFactory.instance.load().withContext { ctx1 ->
			PcscTerminalFactory.instance.load().withContext { ctx2 ->
				assertEquals(ctx1.list().map { it.name }, ctx2.list().map { it.name })
			}
		}
	}

	@Test
	fun `get terminal features`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			val con = terminal.connectTerminalOnly()
			val features = con.getFeatures()
			assertTrue { features.isEmpty() }
		}
	}

	@Disabled
	@Test
	fun `wait for card`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			println("Waiting for card insert ...")
			terminal.waitForCardPresent(5000.toDuration(DurationUnit.MILLISECONDS))
		}
	}
}
