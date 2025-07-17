package org.openecard.sal.sc

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.fail
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sal.sc.testutils.WhenPcscStack
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.PcscTerminalFactory
import java.security.cert.CertificateFactory
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@WhenPcscStack
class EgkPaceTest {
	val egkCan = "change for execution"

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `execute pace with can`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			Assumptions.assumeTrue(terminal.isCardPresent()) { "Terminal does not contain a card" }

			val recognition = DirectCardRecognition(CompleteTree.calls)
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(EgkCif), recognition, paceFactory)

			val session = sal.startSession()
			val con = session.connect(terminal.name)
			Assumptions.assumeTrue(EgkCif.metadata.id == con.cardType) { "Recognized card is not an eGK" }
			val mf = assertNotNull(con.applications.find { it.name == EgkCifDefinitions.appMf })
			val app = assertNotNull(con.applications.find { it.name == EgkCifDefinitions.appDFESIGN })
			mf.connect()

			val certDs = assertNotNull(app.datasets.find { it.name == "EF.C.CH.AUT.E256" })
			assertFalse { certDs.missingReadAuthentications.isSolved }

			val missing =
				certDs.missingReadAuthentications
					.removeUnsupported(
						listOf(
							DidStateReference.forName(EgkCifDefinitions.autPace),
						),
					)
			when (missing) {
				MissingAuthentications.Unsolveable -> fail { "PACE should be the only DID needed for this DS" }
				is MissingAuthentications.MissingDidAuthentications -> {
					val authOption = missing.options.first()
					assertTrue { authOption.size == 1 }
					val did = authOption.first().authDid
					when (did) {
						is PaceDid -> {
							assertFalse { did.capturePasswordInHardware() }
							runBlocking { did.establishChannel(egkCan, null, null) }
						}
						else -> assertFails { "Non PACE DID found" }
					}
				}
			}

			app.connect()
			val certData = certDs.read()
			val certs = CertificateFactory.getInstance("X.509").generateCertificates(certData.toByteArray().inputStream())
			assertTrue { certs.isNotEmpty() }
		}
	}
}
