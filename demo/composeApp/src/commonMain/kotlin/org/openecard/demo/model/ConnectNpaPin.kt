package org.openecard.demo.model

import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.demo.data.PinOperations
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.CardChannel
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

class ConnectNpaPin {
	companion object {
		suspend fun createPinModel(
			terminalFactory: TerminalFactory,
			nfcDetected: () -> Unit
		): PinOperations {
			val app = connectCard(terminalFactory, nfcDetected)
			return PinOperations(app)
		}

		private suspend fun connectCard(
			terminal: TerminalFactory,
			nfcDetected: () -> Unit
		): SmartcardApplication {
			val ctx = terminal.load()
			ctx.establishContext()
			val sal =
				SmartcardSal(
					ctx,
					setOf(NpaCif),
					object : CardRecognition {
						override fun recognizeCard(channel: CardChannel) = NpaDefinitions.cardType
					},
					PaceFeatureSoftwareFactory(),
				)
			val session = sal.startSession()
			val terminal = ctx.getTerminal("") ?: error("No terminal")

			terminal.waitForCardPresent()
			nfcDetected()

			val connection = session.connect(terminal.name)

			connection.channel.card.setContactless = true

			if (connection.deviceType != NpaCif.metadata.id) {
				throw IllegalStateException("Card is not an nPA")
			}

			return connection.applications.find { it.name == NpaDefinitions.Apps.Mf.name }
				?: throw IllegalStateException("MF application not found")
		}
	}
}
