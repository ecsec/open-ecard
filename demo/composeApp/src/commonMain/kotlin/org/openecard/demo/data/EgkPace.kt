package org.openecard.demo.data

import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.demo.domain.EgkOperations
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

class EgkPace {
	companion object {
		fun createEgkSession(terminalFactory: TerminalFactory): EgkOperations {
			val session = createSession(terminalFactory)
			return EgkOperations(session)
		}

		private fun createSession(terminalFactory: TerminalFactory): SmartcardSalSession {
			val ctx = terminalFactory.load()

			val recognition =
				DirectCardRecognition(
					CompleteTree.calls.removeUnsupported(setOf(EgkCifDefinitions.cardType)),
				)

			val sal =
				SmartcardSal(
					ctx,
					setOf(EgkCif),
					recognition,
					PaceFeatureSoftwareFactory(),
				)

			val session = sal.startSession()

			return session
		}
	}
}
