package org.openecard.sal.sc

import org.openecard.sal.iface.DeviceUnavailable
import org.openecard.sal.iface.DeviceUnsupported
import org.openecard.sal.iface.SalSession
import org.openecard.sc.iface.CardCapabilities
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.info.SmartcardInfoRetriever
import org.openecard.utils.common.generateSessionId

class SmartcardSalSession internal constructor(
	override val sal: SmartcardSal,
	override val sessionId: String,
	private val readSmartcardInfo: Boolean,
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

				val cardType =
					sal.cardRecognition.recognizeCard(channel)
						?: throw DeviceUnsupported("The requested smartcard could not be recognized")
				val cif =
					sal.cifs.find { it.metadata.id == cardType }
						?: throw DeviceUnsupported("The requested smartcard has no CIF available")

				// determine suitable capabilites based on atr, static capabilites of by reading card information
				var capabilities: CardCapabilities? = channel.card.capabilities
				val staticCapabilities by lazy { cif.capabilities?.let { StaticCardCapabilities(it) } }
				if (capabilities == null && sal.preferStaticCardCapabilities) {
					capabilities = staticCapabilities
				}
				if (capabilities == null && readSmartcardInfo) {
					val scInfo = SmartcardInfoRetriever(channel).retrieve(withSelectMf = true, withEfDir = false)
					capabilities = scInfo.capabilities
				}
				// if we still don't have anything, try static again
				if (capabilities == null) {
					capabilities = staticCapabilities
				}
				// update in card
				card.capabilities = capabilities

				val connectionId = sal.random.generateSessionId()
				SmartcardDeviceConnection(connectionId, this, channel, cif)
			}.onFailure {
				terminalCon?.disconnect()
			}.getOrThrow()
		}
}
