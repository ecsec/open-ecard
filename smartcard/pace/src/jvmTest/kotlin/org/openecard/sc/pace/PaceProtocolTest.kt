package org.openecard.sc.pace

import org.junit.jupiter.api.Assumptions
import org.openecard.sc.apdu.command.ReadBinary
import org.openecard.sc.apdu.command.Select
import org.openecard.sc.apdu.command.transmit
import org.openecard.sc.iface.feature.PacePinId
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.testutils.WhenPcscStack
import org.openecard.sc.pcsc.PcscTerminalFactory
import java.security.cert.CertificateFactory
import kotlin.test.Test
import kotlin.test.assertTrue

@WhenPcscStack
class PaceProtocolTest {
	val egkCan = "change for execution"

	@OptIn(ExperimentalUnsignedTypes::class)
	@Test
	fun `establish secure messaging egk`() {
		PcscTerminalFactory.instance.load().withContext { ctx ->
			val terminal =
				ctx.list().find { it.name.startsWith("REINER SCT cyberJack RFID basis") }
					?: Assumptions.abort { "Necessary terminal not available" }
			val con = terminal.connect()
			val channel = checkNotNull(con.card).basicChannel

			val paceProtocol = PaceProtocol()
			val response = paceProtocol.execute(channel, PacePinId.CAN, egkCan, null)

			// read a protected file (EF.C.CA.CS.E256) to see if secure messaging is working
			Select.selectEfIdentifier(0x2F07u).transmit(channel)
			val cert = ReadBinary.readCurrentEf().transmit(channel)
			val certs = CertificateFactory.getInstance("X.509").generateCertificates(cert.toByteArray().inputStream())
			assertTrue { certs.isNotEmpty() }
		}
	}
}
