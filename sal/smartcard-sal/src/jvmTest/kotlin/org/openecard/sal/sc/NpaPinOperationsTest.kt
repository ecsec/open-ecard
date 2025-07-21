package org.openecard.sal.sc

import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.junit.jupiter.api.fail
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions.Apps.Mf
import org.openecard.sal.iface.dids.PaceDid
import org.openecard.sal.iface.dids.PinDid
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sal.sc.testutils.WhenPcscStack
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.apdu.command.SecurityCommandSuccess
import org.openecard.sc.iface.feature.PaceError
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.PcscTerminalFactory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@EnabledIfEnvironmentVariable(named = "NPA_PACE_CAN", matches = ".*")
@EnabledIfEnvironmentVariable(named = "NPA_PACE_PIN", matches = ".*")
@EnabledIfEnvironmentVariable(named = "NPA_PACE_PUK", matches = ".*")
@WhenPcscStack
class NpaPinOperationsTest {
	lateinit var npaCan: String
	lateinit var npaPin: String
	lateinit var npaPuk: String
	val pinWrong = "999999"

	@BeforeTest
	fun loadSecrets() {
		npaCan = System.getenv("NPA_PACE_CAN")
		npaPin = System.getenv("NPA_PACE_PIN")
		npaPuk = System.getenv("NPA_PACE_PUK")
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `suspend recovery`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			Assumptions.assumeTrue(terminal.isCardPresent()) { "Terminal does not contain a card" }

			val recognition = DirectCardRecognition(CompleteTree.calls)
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)

			val session = sal.startSession()
			val con = session.connect(terminal.name)
			Assumptions.assumeTrue(NpaCif.metadata.id == con.cardType) { "Recognized card is not an nPA" }
			val mf = assertNotNull(con.applications.find { it.name == Mf.name })
			mf.connect()

			val pacePin = assertNotNull(mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.pacePin })
			val paceCan = assertNotNull(mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.paceCan })

			val pinStatus = pacePin.passwordStatus()
			when (pinStatus) {
				is SecurityCommandFailure -> {
					if (pinStatus.retries == 2) {
						assertNotNull(pacePin.enterPassword(pinWrong)).let {
							assertEquals(1, it.retries)
							assertTrue(it.verificationFailed)
						}
					} else if (pinStatus.retries == 1) {
						// this is what we want, fallthrough
					} else {
						fail { "PIN status is not usable" }
					}
				}
				is SecurityCommandSuccess -> {
					assertNotNull(pacePin.enterPassword(pinWrong)).let {
						assertEquals(2, it.retries)
						assertTrue(it.verificationFailed)
					}
					assertNotNull(pacePin.enterPassword(pinWrong)).let {
						assertEquals(1, it.retries)
						assertTrue(it.verificationFailed)
					}
				}
			}

			assertNull(paceCan.enterPassword(npaCan))
			assertNull(pacePin.enterPassword(npaPin))

			val pinStatus2 = pacePin.passwordStatus()
			when (pinStatus2) {
				is SecurityCommandFailure -> fail { "PIN status reports error after successful PIN entry" }
				is SecurityCommandSuccess -> {}
			}
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `unblock password`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			Assumptions.assumeTrue(terminal.isCardPresent()) { "Terminal does not contain a card" }

			val recognition = DirectCardRecognition(CompleteTree.calls)
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)

			val session = sal.startSession()
			val con = session.connect(terminal.name)
			Assumptions.assumeTrue(NpaCif.metadata.id == con.cardType) { "Recognized card is not an nPA" }
			val mf = assertNotNull(con.applications.find { it.name == Mf.name })
			mf.connect()

			val pacePin = assertNotNull(mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.pacePin })
			val paceCan = assertNotNull(mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.paceCan })
			val pacePuk = assertNotNull(mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.pacePuk })
			val pin = assertNotNull(mf.dids.filterIsInstance<PinDid>().find { it.name == Mf.Dids.pin })

			val pinStatus = pacePin.passwordStatus()
			when (pinStatus) {
				is SecurityCommandFailure -> {
					if (pinStatus.retries == 2) {
						assertNotNull(pacePin.enterPassword(pinWrong)).let {
							assertEquals(1, it.retries)
							assertTrue(it.verificationFailed)
						}
					} else if (pinStatus.retries == 1) {
						// almost there
						assertNull(paceCan.enterPassword(npaCan))
						assertNotNull(pacePin.enterPassword(pinWrong)).let {
							assertTrue(it.authBlocked)
						}
					} else {
						// there we want to be
					}
				}
				is SecurityCommandSuccess -> {
					assertNotNull(pacePin.enterPassword(pinWrong)).let {
						assertEquals(2, it.retries)
						assertTrue(it.verificationFailed)
					}
					assertNotNull(pacePin.enterPassword(pinWrong)).let {
						assertEquals(1, it.retries)
						assertTrue(it.verificationFailed)
					}
					assertNull(paceCan.enterPassword(npaCan))
					assertNotNull(pacePin.enterPassword(pinWrong)).let {
						assertTrue(it.authBlocked)
					}
				}
			}

			assertNull(pacePuk.enterPassword(npaPuk))
			pin.resetPassword(null, null)

			val pinStatus2 = pacePin.passwordStatus()
			when (pinStatus2) {
				is SecurityCommandFailure -> fail { "PIN status reports error after successful reset with PUK" }
				is SecurityCommandSuccess -> {}
			}

			assertNull(pacePin.enterPassword(npaPin))
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `change password`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			Assumptions.assumeTrue(terminal.isCardPresent()) { "Terminal does not contain a card" }

			val recognition = DirectCardRecognition(CompleteTree.calls)
			val paceFactory = PaceFeatureSoftwareFactory()
			val sal = SmartcardSal(ctx, setOf(NpaCif), recognition, paceFactory)

			val session = sal.startSession()
			val con = session.connect(terminal.name)
			Assumptions.assumeTrue(NpaCif.metadata.id == con.cardType) { "Recognized card is not an nPA" }
			val mf = assertNotNull(con.applications.find { it.name == Mf.name })
			mf.connect()

			val pacePin = assertNotNull(mf.dids.filterIsInstance<PaceDid>().find { it.name == Mf.Dids.pacePin })
			val pin = assertNotNull(mf.dids.filterIsInstance<PinDid>().find { it.name == Mf.Dids.pin })

			val pinStatus = pacePin.passwordStatus()
			when (pinStatus) {
				is SecurityCommandFailure -> {
					fail { "PIN must be in RC3 state to continue" }
				}
				is SecurityCommandSuccess -> {
					assertNull(pacePin.enterPassword(npaPin))
				}
			}

			pin.resetPassword(null, "654321")
			pin.resetPassword(null, npaPin)
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	fun PaceDid.enterPassword(pin: String): SecurityCommandFailure? {
		try {
			establishChannel(pin, null, null)
			return null
		} catch (ex: PaceError) {
			val secErr = ex.securityError
			if (secErr != null) {
				return secErr
			} else {
				throw ex
			}
		}
	}
}
