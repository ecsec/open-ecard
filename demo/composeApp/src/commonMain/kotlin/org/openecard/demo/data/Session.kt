package org.openecard.demo.data

import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.CardInfoDefinition
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.demo.domain.EacOperations
import org.openecard.demo.domain.EgkOperations
import org.openecard.demo.domain.PinOperations
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

class Session {
	companion object {
		fun createPinSession(terminalFactory: TerminalFactory): PinOperations {
			val cardType = NpaDefinitions.cardType
			val cif = NpaCif
			val session = createSession(terminalFactory, cardType, cif)
			return PinOperations(session)
		}

		fun createEacSession(terminalFactory: TerminalFactory): EacOperations {
			val cardType = NpaDefinitions.cardType
			val cif = NpaCif
			val session = createSession(terminalFactory, cardType, cif)

			return EacOperations(session)
		}

		fun createEgkSession(terminalFactory: TerminalFactory): EgkOperations {
			val cardType = EgkCifDefinitions.cardType
			val cif = EgkCif
			val session = createSession(terminalFactory, cardType, cif)
			return EgkOperations(session)
		}

		suspend fun initializeStack(
			session: SmartcardSalSession,
			nfcDetected: () -> Unit,
		): Terminal {
			session.initializeStack()
			val terminal =
				session.sal.terminals
					.list()
					.firstOrNull() ?: throw IllegalStateException("No terminal found")

			terminal.waitForCardPresent()
			nfcDetected()

			return terminal
		}

		private fun createSession(
			terminal: TerminalFactory,
			cardType: String,
			cif: CardInfoDefinition,
		): SmartcardSalSession {
			val ctx = terminal.load()

			val recognition =
				DirectCardRecognition(
					CompleteTree.calls.removeUnsupported(setOf(cardType)),
				)

			val sal =
				SmartcardSal(
					ctx,
					setOf(cif),
					recognition,
					PaceFeatureSoftwareFactory(),
				)
			val session = sal.startSession()

			return session
		}
	}
}
