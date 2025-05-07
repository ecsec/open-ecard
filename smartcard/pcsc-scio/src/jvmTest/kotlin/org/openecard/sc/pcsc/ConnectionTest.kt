package org.openecard.sc.pcsc

import org.junit.jupiter.api.Assumptions
import org.openecard.sc.iface.withContext
import org.openecard.sc.pcsc.testutils.WhenPcscStack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
			val terminal = ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") } ?: Assumptions.abort()
			val con = terminal.connectTerminalOnly()
			val features = con.getFeatures()
			assertTrue { features.isEmpty() }
		}
	}
}
