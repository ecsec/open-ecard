package org.openecard.demo

import androidx.compose.runtime.Composable
import kotlinx.coroutines.suspendCancellableCoroutine
import org.openecard.addons.tr03124.BindingResponse
import org.openecard.addons.tr03124.ClientInformation
import org.openecard.addons.tr03124.EidActivation
import org.openecard.addons.tr03124.UserAgent
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.withContextSuspend
import org.openecard.sc.pace.PaceFeatureSoftwareFactory

@OptIn(ExperimentalUnsignedTypes::class)
suspend fun doNFC(terminalFactory: TerminalFactory?): Unit? =
	terminalFactory?.load()?.withContextSuspend { ctx ->
		val recognition =
			DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType)))

		val paceFactory = PaceFeatureSoftwareFactory()
		val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)
		val clientInfo = ClientInformation(UserAgent("Open-eCard Test", UserAgent.Version(1, 0, 0)))

		val session = sal.startSession()

		when (val terminal = ctx.getTerminal("")) {
			null -> {
				throw Error("NO TERMINAL")
			}

			else -> {
				terminal.waitForCardPresent()
				val uiStep = EidActivation.startEacProcess(clientInfo, "", session, terminal.name)

				val paceResp =
					uiStep.getPaceDid().establishChannel(
						"123123",
						uiStep.guiData.requiredChat.asBytes,
						uiStep.guiData.certificateDescription.asBytes,
					)
				val serverStep = uiStep.processAuthentication(paceResp)
				val result = serverStep.processEidServerLogic()

				println(result)
			}
		}
	}
