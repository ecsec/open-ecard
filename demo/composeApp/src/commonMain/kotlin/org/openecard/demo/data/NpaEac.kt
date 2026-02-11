package org.openecard.demo.data

import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.demo.domain.EacOperations
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

class NpaEac {
	companion object {
		fun createEacSession(terminalFactory: TerminalFactory): EacOperations {
			val session = createSession(terminalFactory)

			return EacOperations(session)
		}

		private fun createSession(terminalFactory: TerminalFactory): SmartcardSalSession {
			val ctx = terminalFactory.load()
			ctx.establishContext()

			val recognition =
				DirectCardRecognition(
					CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType)),
				)

			val sal =
				SmartcardSal(
					ctx,
					setOf(NpaCif),
					recognition,
					PaceFeatureSoftwareFactory(),
				)

			val session = sal.startSession()

			return session
		}
	}
}
