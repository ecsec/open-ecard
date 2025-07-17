package org.openecard.sal.sc

import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.DeviceUnsupported
import org.openecard.sal.iface.SalSession
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.info.SmartcardInfoRetriever
import org.openecard.utils.common.generateSessionId

class SmartcardSalSession internal constructor(
	override val sal: SmartcardSal,
	override val sessionId: String,
) : SalSession {
	override fun initializeStack() =
		mapSmartcardError {
			sal.terminals.establishContext()
		}

	override fun shutdownStack() =
		mapSmartcardError {
			sal.terminals.releaseContext()
		}

	override fun devices(): List<String> = mapSmartcardError { sal.terminals.list().map { it.name } }

	override fun connect(
		terminalName: String,
		isExclusive: Boolean,
	): SmartcardDeviceConnection =
		mapSmartcardError {
			var terminalCon: TerminalConnection? = null
			runCatching {
				val terminal =
					sal.terminals.getTerminal(terminalName)
						?: throw DeviceUnavailable("Terminal '$terminalName' is not available")
				terminalCon = terminal.connect()

				val card = terminalCon.card ?: throw DeviceUnavailable("Card is not available in terminal '$terminalName'")
				val channel = card.basicChannel

				// read card information and update channel
				val scInfo = SmartcardInfoRetriever(channel).retrieve(withSelectMf = false, withEfDir = false)
				card.capabilities = scInfo.capabilities

				val cardType =
					sal.cardRecognition.recognizeCard(channel)
						?: throw DeviceUnsupported("The requested smartcard could not be recognized")
				val cif =
					sal.cifs.find { it.metadata.id == cardType }
						?: throw DeviceUnsupported("The requested smartcard has no CIF available")

				val connectionId = sal.random.generateSessionId()
				SmartcardDeviceConnection(connectionId, this, channel, cif)
			}.onFailure {
				terminalCon?.disconnect()
			}.getOrThrow()
		}
}
