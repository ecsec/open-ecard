package org.openecard.sc.pcsc

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Disabled
import org.openecard.sc.apdu.command.FileControlInformation
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.info.SmartcardInfoRetriever
import org.openecard.sc.iface.withContext
import org.openecard.sc.pcsc.testutils.WhenPcscStack
import kotlin.test.Test

@WhenPcscStack
class TestSmartcardInformation {
	@Test
	@Disabled
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

	@Test
	@Disabled
	fun `read fci`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.isCardPresent() }
					?: Assumptions.abort { "Necessary terminal not available" }
			val con = terminal.connect()
			val channel = con.card!!.basicChannel

			var select = Select.selectEfIdentifier(0x2f00u, fileControlInfo = FileControlInformation.FCI)
			val fci = runCatching { select.transmit(channel) }.getOrNull()
			val fcp = runCatching { select.copy(fileControlInfo = FileControlInformation.FCP).transmit(channel) }.getOrNull()
			val fmd = runCatching { select.copy(fileControlInfo = FileControlInformation.FMD).transmit(channel) }.getOrNull()
			select.copy()
		}
	}
}
