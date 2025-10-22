import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.feature.Feature
import platform.CoreNFC.NFCISO7816TagProtocol

private val logger = KotlinLogging.logger { }

class IosTerminalConnection(
	override val terminal: IosTerminal,
) : TerminalConnection {
	internal val tag: NFCISO7816TagProtocol?
		get() = terminal.currentSession?.tag

	override var card: IosNfcCard = IosNfcCard(this)

	override fun isCardConnected() = terminal.currentSession?.tag?.available == true

	override fun disconnect(disposition: CardDisposition) =
		logger.debug {
			"Note: disconnect is NOP on iOS"
		}

	override fun reconnect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
		disposition: CardDisposition,
	) = logger.debug {
		"Note: reconnect is NOP on iOS"
	}

	override fun getFeatures() = emptySet<Feature>()

	override fun beginTransaction() = logger.debug { "Note: beginTransaction is NOP on android" }

	override fun endTransaction() = logger.debug { "Note: endTransaction is NOP on android" }
}
