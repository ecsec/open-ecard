package org.openecard.sc.pcsc

import org.junit.jupiter.api.Assumptions
import org.openecard.sc.iface.info.SmartcardInfoRetriever
import org.openecard.sc.iface.withContext
import org.openecard.sc.pcsc.testutils.WhenPcscStack
import kotlin.test.Test

@WhenPcscStack
class TestSmartcardInformation {
	@Test
	fun `get terminal features`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.isCardPresent() }
					?: Assumptions.abort { "Necessary terminal not available" }
			val con = terminal.connect()
			val channel = con.card!!.basicChannel

			val infoBuilder = SmartcardInfoRetriever(channel)
			val info = infoBuilder.retrieve()
			val histBytes = channel.card.atr.historicalBytes
		}
	}
}
