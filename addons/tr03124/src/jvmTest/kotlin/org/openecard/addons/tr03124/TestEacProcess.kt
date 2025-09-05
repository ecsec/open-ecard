package org.openecard.addons.tr03124

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.openecard.addons.tr03124.testutils.WhenPcscStack
import org.openecard.addons.tr03124.transport.GovernikusTestServer
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.withContextSuspend
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.PcscTerminalFactory
import kotlin.test.BeforeTest
import kotlin.test.Test

@EnabledIfEnvironmentVariable(named = "NPA_PACE_PIN", matches = ".*")
@WhenPcscStack
class TestEacProcess {
	lateinit var npaPin: String

	@BeforeTest
	fun loadCan() {
		npaPin = System.getenv("NPA_PACE_PIN")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun executeEacWithGovernikusTest() {
		// use runBlocking as we want a lifelike test
		runBlocking {
			PcscTerminalFactory.instance.load().withContextSuspend { ctx ->
				val recognition =
					DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType)))

				// find terminal with nPA
				val npaTerminal =
					ctx
						.list()
						.filter { it.isCardPresent() }
						.asSequence()
						.mapNotNull {
							val con = it.connect()
							try {
								val channel = con.card?.basicChannel
								if (channel == null) {
									null
								} else {
									val cardType = recognition.recognizeCard(channel)
									if (cardType == NpaDefinitions.cardType) {
										con.terminal.name
									} else {
										null
									}
								}
							} finally {
								con.disconnect()
							}
						}.firstOrNull()
				Assumptions.assumeTrue(npaTerminal != null) { "No nPA inserted in any terminal" }
				checkNotNull(npaTerminal)

				val paceFactory = PaceFeatureSoftwareFactory()
				val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)

				val clientInfo = ClientInformation(UserAgent("Open-eCard Test", UserAgent.Version(1, 0, 0)))

				val tokenUrl = GovernikusTestServer().loadTcTokenUrl()

				val session = sal.startSession()

				val uiStep = EidActivation.startEacProcess(clientInfo, tokenUrl, session, npaTerminal)
				val paceResp =
					uiStep.getPaceDid().establishChannel(
						npaPin,
						uiStep.guiData.requiredChat.asBytes,
						uiStep.guiData.certificateDescription.asBytes,
					)
				val serverStep = uiStep.processAuthentication(paceResp)
				val result = serverStep.processEidServerLogic()

				assertInstanceOf<BindingResponse.RedirectResponse>(result)
			}
		}
	}
}
