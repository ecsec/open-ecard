package org.openecard.demo.data

import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.demo.domain.EgkOperations
import org.openecard.sal.sc.SmartcardDeviceConnection
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

class ConnectEgk {
	companion object {
		suspend fun createConnectedModel(
			terminalFactory: TerminalFactory,
			nfcDetected: () -> Unit
		): EgkOperations {
			val connection = connectCard(terminalFactory, nfcDetected)
			return EgkOperations(connection)
		}

		private suspend fun connectCard(
			terminalFactory: TerminalFactory,
			nfcDetected: () -> Unit
		): SmartcardDeviceConnection {

			val ctx = terminalFactory.load()
			ctx.establishContext()


			val recognition = DirectCardRecognition(
				CompleteTree.calls.removeUnsupported(setOf(EgkCifDefinitions.cardType))
			)

			val sal = SmartcardSal(
				ctx,
				setOf(EgkCif),
				recognition,
				PaceFeatureSoftwareFactory()
			)

			val session = sal.startSession()
			val terminal = ctx.getTerminal("") ?: error("No terminal")

			terminal.waitForCardPresent()
			nfcDetected()

			val connection = session.connect(terminal.name)

			if (connection.deviceType != EgkCif.metadata.id) {
				throw IllegalStateException("Card is not an eGK")
			}

			return connection
		}
	}
}
