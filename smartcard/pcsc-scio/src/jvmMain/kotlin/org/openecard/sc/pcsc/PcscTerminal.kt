package org.openecard.sc.pcsc

import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.TerminalStateType
import javax.smartcardio.CardTerminal
import kotlin.time.Duration

class PcscTerminal internal constructor(
	override val terminals: PcscTerminals,
	override val name: String,
) : Terminal {
	override fun isCardPresent(): Boolean = getState() == TerminalStateType.PRESENT

	override fun getState(): TerminalStateType =
		getScioTerminal().let {
			if (it.isCardPresent) {
				TerminalStateType.PRESENT
			} else {
				TerminalStateType.ABSENT
			}
		}

	override fun connectTerminalOnly(): TerminalConnection =
		terminals.getScioTerminal(name)?.let {
			val card = it.connect("DIRECT")
			PcscTerminalConnection(this, card)
		} ?: throw ReaderUnavailable()

	internal fun connectInternal(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	) = getScioTerminal().let {
		val shareMode = if (shareMode == ShareMode.EXCLUSIVE) "EXCLUSIVE;" else ""
		val protocol = protocol.toScioProtocol()
		val card = it.connect("$shareMode$protocol")
		card
	}

	override fun connect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	): TerminalConnection = PcscTerminalConnection(this, connectInternal(protocol, shareMode))

	override suspend fun waitForCardPresent(timeout: Duration) {
		TODO("Not yet implemented")
	}

	override suspend fun waitForCardAbsent(timeout: Duration) {
		TODO("Not yet implemented")
	}

	@Throws(ReaderUnavailable::class)
	internal fun getScioTerminal(): CardTerminal = terminals.getScioTerminal(name) ?: throw ReaderUnavailable()

	override fun toString(): String = "PcscTerminal[name=$name, isCardPresent=${isCardPresent()}]"
}

internal fun PreferredCardProtocol.toScioProtocol(): String =
	when (this) {
		PreferredCardProtocol.T0 -> "T=0"
		PreferredCardProtocol.T1 -> "T=1"
		PreferredCardProtocol.RAW -> throw IllegalArgumentException("RAW mode is not supported")
		PreferredCardProtocol.ANY -> "*"
	}
