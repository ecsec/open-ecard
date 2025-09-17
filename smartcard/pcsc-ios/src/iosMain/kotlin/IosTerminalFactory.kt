import org.openecard.sc.iface.SmartCardStackMissing
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.Terminals
import platform.CoreNFC.NFCReaderSession

class IosTerminalFactory internal constructor() : TerminalFactory {
	override val name: String
		get() = "IosNFC"

	companion object {
		val instance = IosTerminalFactory()
	}

	val nfcAvailable =
		NFCReaderSession.readingAvailable

	private val terminals = IosTerminals(this)

	override fun load() =
		if (nfcAvailable) {
			terminals
		} else {
			throw SmartCardStackMissing("NFC not available on this device.", null)
		}
}

class IosTerminals internal constructor(
	override val factory: TerminalFactory,
) : Terminals {
	val iosTerminal: IosTerminal by lazy {
		IosTerminal(
			this,
			"IosNFCTerminal",
		)
	}

	override fun list(): List<Terminal> = listOf(iosTerminal)

	override fun getTerminal(name: String) = iosTerminal

	override val supportsControlCommand = false

	override val isEstablished: Boolean
		get() = iosTerminal.sessionActive

	override fun establishContext() {
		iosTerminal.activate()
	}

	override fun releaseContext() {
		iosTerminal.deActivate()
	}
}
