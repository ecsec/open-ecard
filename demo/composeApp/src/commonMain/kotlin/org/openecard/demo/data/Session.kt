package org.openecard.demo.data

import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.demo.domain.EacOperations
import org.openecard.demo.domain.EgkOperations
import org.openecard.demo.domain.PinOperations
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

class Session {
	companion object {
		fun createPinSession(terminalFactory: TerminalFactory): PinOperations {
			val cardType = NpaDefinitions.cardType
			val session = createSession(terminalFactory, cardType)
			return PinOperations(session)
		}

		fun createEacSession(terminalFactory: TerminalFactory): EacOperations {
			val cardType = NpaDefinitions.cardType
			val session = createSession(terminalFactory, cardType)

			return EacOperations(session)
		}

		fun createEgkSession(terminalFactory: TerminalFactory): EgkOperations {
			val cardType = EgkCifDefinitions.cardType
			val session = createSession(terminalFactory, cardType)
			return EgkOperations(session)
		}

		private fun createSession(
			terminal: TerminalFactory,
			cardType: String,
		): SmartcardSalSession {
			val ctx = terminal.load()

			val recognition =
				DirectCardRecognition(
					CompleteTree.calls.removeUnsupported(setOf(cardType)),
				)

			val sal =
				SmartcardSal(
					ctx,
					setOf(NpaCif, EgkCif),
					recognition,
					PaceFeatureSoftwareFactory(),
				)
			val session = sal.startSession()

			return session
		}
	}
}
