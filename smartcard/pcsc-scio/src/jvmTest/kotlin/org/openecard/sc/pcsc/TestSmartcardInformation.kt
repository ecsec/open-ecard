package org.openecard.sc.pcsc

import org.junit.jupiter.api.Assumptions
import org.openecard.sc.apdu.command.FileControlInformation
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.info.SmartcardInfoRetriever
import org.openecard.sc.iface.withContext
import org.openecard.sc.pcsc.testutils.WhenPcscStack
import kotlin.test.Ignore
import kotlin.test.Test

@WhenPcscStack
class TestSmartcardInformation {
	@Test
	@Ignore
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

	@Ignore
	@Test
	fun `print CardCapabilities`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.isCardPresent() }
					?: Assumptions.abort { "Necessary terminal not available" }
			val con = terminal.connect()
			val channel = con.card!!.basicChannel

			val infoBuilder = SmartcardInfoRetriever(channel)
			val info = infoBuilder.retrieve(withEfAtr = true, withEfDir = false)

			info.capabilities?.let { cap ->
				println("Selection Methods")
				println("=================")
				cap.selectionMethods.let {
					println("selectDfByFullName = ${it.selectDfByFullName}")
					println("selectDfByPartialName = ${it.selectDfByPartialName}")
					println("selectDfByPath = ${it.selectDfByPath}")
					println("selectDfByFileId = ${it.selectDfByFileId}")
					println("selectDfImplicit = ${it.selectDfImplicit}")
					println("supportsShortEf = ${it.supportsShortEf}")
					println("supportsRecordNumber = ${it.supportsRecordNumber}")
					println("supportsRecordIdentifier = ${it.supportsRecordIdentifier}")
				}

				println("")
				println("Data Coding")
				println("=================")
				cap.dataCoding?.let {
					println("tlvEfs = ${it.tlvEfs}")
					println("writeOneTime = ${it.writeOneTime}")
					println("writeProprietary = ${it.writeProprietary}")
					println("writeOr = ${it.writeOr}")
					println("writeAnd = ${it.writeAnd}")
					println("ffValidAsTlvFirstByte = ${it.ffValidAsTlvFirstByte}")
					println("dataUnitsQuartets = ${it.dataUnitsQuartets}")
				}

				println("")
				println("Command Coding")
				println("=================")
				cap.commandCoding?.let {
					println("supportsCommandChaining = ${it.supportsCommandChaining}")
					println("supportsExtendedLength = ${it.supportsExtendedLength}")
					println("logicalChannel = ${it.logicalChannel}")
					println("maximumLogicalChannels = ${it.maximumLogicalChannels}")
				}
			}
		}
	}

	@Test
	@Ignore
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
