package org.openecard.sc.pcsc

import org.openecard.sc.iface.withContext
import kotlin.test.Test
import kotlin.test.assertEquals

class ConnectionTest {
	@Test
	fun `connect terminal twice`() {
		PcscTerminalFactory.instance.load().withContext { ctx1 ->
			PcscTerminalFactory.instance.load().withContext { ctx2 ->
				assertEquals(ctx1.list().map { it.name }, ctx2.list().map { it.name })
			}
		}
	}

	@Test
	fun `get terminal features`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal = ctx.list()[1]
			val con = terminal.connectTerminalOnly()
			val features = con.features
		}
	}
}
