package org.openecard.sc.pace

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.assertNotNull
import org.openecard.sc.apdu.StatusWord
import org.openecard.sc.apdu.StatusWordResult
import org.openecard.sc.apdu.command.ReadBinary
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.apdu.toCommandApdu
import org.openecard.sc.iface.feature.PaceEstablishChannelRequest
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.testutils.WhenPcscStack
import org.openecard.sc.pcsc.PcscTerminalFactory
import org.openecard.utils.common.hex
import java.security.cert.CertificateFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@WhenPcscStack
class PaceProtocolTest {
	val egkCan = "123123"

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `establish secure messaging egk`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			Assumptions.assumeTrue(terminal.isCardPresent()) { "Terminal does not contain a card" }

			val con = terminal.connect()
			val channel = checkNotNull(con.card).basicChannel

			val paceProtocol = PaceProtocol(channel)
			val req = PaceEstablishChannelRequest(PacePinId.CAN, egkCan, null, null)
			val response = runBlocking { paceProtocol.establishChannel(req) }

			// read a protected file (EF.C.CA.CS.E256) to see if secure messaging is working
			Select.selectApplicationId(hex("A000000167455349474E")).transmit(channel)
			Select.selectEfIdentifier(0xC504u).transmit(channel)
			val cert = ReadBinary.readCurrentEf(forceExtendedLength = true).transmit(channel)

			val certs = CertificateFactory.getInstance("X.509").generateCertificates(cert.toByteArray().inputStream())
			assertTrue { certs.isNotEmpty() }
		}
	}

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `test internal authenticate on egk`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			Assumptions.assumeTrue(terminal.isCardPresent()) { "Terminal does not contain a card" }

			val con = terminal.connect()
			val channel = checkNotNull(con.card).basicChannel

			val paceProtocol = PaceProtocol(channel)
			val req = PaceEstablishChannelRequest(PacePinId.CAN, egkCan, null, null)
			val response = runBlocking { paceProtocol.establishChannel(req) }

			channel.transmit(hex("002241A406840109800100").toCommandApdu())

			val apdu = hex("00880000209C8CE4E11FE957A78D0780A110F1F97584A59037D4F0B8129C3A97BF9259826400").toCommandApdu()
			val res = channel.transmit(apdu)
			assertNotNull(res)
			assertEquals(StatusWord.CONDITIONS_OF_USE_UNSATISFIED, res.status.type)
		}
	}
}
