package org.openecard.demo.data

import org.openecard.demo.domain.EacOperations
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.SmartcardSalSession
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

data class NpaEacModel(
	val session: SmartcardSalSession,
	val terminalName: String
)

class ConnectNpaEac {
	companion object {
		suspend fun createEacModel(
			terminalFactory: TerminalFactory,
			nfcDetected: () -> Unit
		): EacOperations {
			val model = connectCard(terminalFactory, nfcDetected)

			return EacOperations(model)
		}

		private suspend fun connectCard(
			terminalFactory: TerminalFactory,
			nfcDetected: () -> Unit
		): NpaEacModel {

			val ctx = terminalFactory.load()
			ctx.establishContext()

			val recognition =
				DirectCardRecognition(
					CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType))
				)

			val sal = SmartcardSal(
				ctx,
				setOf(NpaCif),
				recognition,
				PaceFeatureSoftwareFactory()
			)

			val session = sal.startSession()
			val terminal = ctx.getTerminal("") ?: error("NO TERMINAL")

			terminal.waitForCardPresent()
			nfcDetected()

			return NpaEacModel(
				session = session,
				terminalName = terminal.name,
			)
		}
	}
}
