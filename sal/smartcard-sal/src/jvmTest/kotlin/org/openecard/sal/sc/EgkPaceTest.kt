package org.openecard.sal.sc

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.fail
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.EgkCif
import org.openecard.cif.bundled.EgkCifDefinitions
import org.openecard.cif.definition.acl.DidStateReference
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.sal.iface.MissingAuthentications
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sal.sc.testutils.WhenPcscStack
import org.openecard.sc.apdu.checkOk
import org.openecard.sc.apdu.command.InternalAuthenticate
import org.openecard.sc.apdu.command.Mse
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.PcscTerminalFactory
import org.openecard.sc.tlv.Tag
import org.openecard.sc.tlv.TlvPrimitive
import org.openecard.utils.serialization.toPrintable
import java.security.cert.CertificateFactory
import kotlin.random.Random
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@EnabledIfEnvironmentVariable(named = "EGK_PACE_CAN", matches = ".*")
@WhenPcscStack
class EgkPaceTest {
	lateinit var egkCan: String

	@BeforeTest
	fun loadCan() {
		egkCan = System.getenv("EGK_PACE_CAN")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `execute pace with can`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			Assumptions.assumeTrue(terminal.isCardPresent()) { "Terminal does not contain a card" }

			val recognition = DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(EgkCifDefinitions.cardType)))
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(EgkCif), recognition, paceFactory)

			val session = sal.startSession()
			val con = session.connect(terminal.name)
			Assumptions.assumeTrue(EgkCif.metadata.id == con.deviceType) { "Recognized card is not an eGK" }
			val mf = assertNotNull(con.applications.find { it.name == EgkCifDefinitions.Apps.Mf.name })
			val app = assertNotNull(con.applications.find { it.name == EgkCifDefinitions.Apps.ESign.name })
			mf.connect()

			val certDs = assertNotNull(app.datasets.find { it.name == "EF.C.CH.AUT.E256" })
			assertFalse { certDs.missingReadAuthentications.isSolved }

			val missing =
				certDs.missingReadAuthentications
					.removeUnsupported(
						listOf(
							DidStateReference.forName(EgkCifDefinitions.Apps.Mf.Dids.autPace),
						),
					)
			when (missing) {
				MissingAuthentications.Unsolveable -> {
					fail { "PACE should be the only DID needed for this DS" }
				}

				is MissingAuthentications.MissingDidAuthentications -> {
					val authOption = missing.options.first()
					assertTrue { authOption.size == 1 }
					val did = authOption.first().authDid
					when (did) {
						is PaceDid -> {
							assertFalse { did.capturePasswordInHardware() }
							runBlocking { did.establishChannel(egkCan, null, null) }
						}

						else -> {
							assertFails { "Non PACE DID found" }
						}
					}
				}
			}

			app.connect()
			val certData = certDs.read()
			val certs = CertificateFactory.getInstance("X.509").generateCertificates(certData.toByteArray().inputStream())
			assertTrue { certs.isNotEmpty() }

			mf.connect()
			val efDirDs = assertNotNull(mf.datasets.find { it.name == "EF.DIR" })
			val efDirData = efDirDs.read()
			assertTrue { efDirData.isNotEmpty() }

			// get certs needed for PoPP-Light protocol
			val cvcAutDs = mf.datasets.find { it.name == EgkCifDefinitions.Apps.Mf.Datasets.ef_c_eGK_aut_cvc_e256 }
			val cvcAut = cvcAutDs?.read()
			// println("cvcAut: ${cvcAut?.toHexString()}")
			val cvcCaDs = mf.datasets.find { it.name == EgkCifDefinitions.Apps.Mf.Datasets.ef_c_ca_cs_e256 }
			val cvcCa = cvcCaDs?.read()
			// println("cvcCa: ${cvcCa?.toHexString()}")
			// println("x509: ${certs.first().encoded.toHexString()}")

			// perform
			val challenge = Random.nextBytes(24)
			// println("challenge: ${challenge.toHexString()}")

			val mse =
				Mse.mseSet(
					Mse.P1Flags(false, false, true, false),
					Mse.Tag.CRT_EXTERNAL_AUTHENTICATE,
					listOf(
						TlvPrimitive(
							Tag.forTagNumWithClass(0x84u),
							"09".hexToUByteArray().toPrintable(),
						),
						TlvPrimitive(
							Tag.forTagNumWithClass(0x80u),
							"00".hexToUByteArray().toPrintable(),
						),
					),
				)
			// println("mse: ${mse.apdu}")
			val mseRes = con.channel.transmit(mse.apdu)
			// println("mseRes: $mseRes")
			mseRes.checkOk()

			val intAuth = InternalAuthenticate.withData(challenge.toUByteArray(), algorithm = 0x00u)
			// println("intAuth: $intAuth")
			val intAuthRes = con.channel.transmit(intAuth.apdu)
			// println("intAuthRes: $intAuthRes")
			intAuthRes.checkOk()
		}
	}
}
