package org.openecard.sc.pcsc

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ReaderUnavailable
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.TerminalStateType
import java.util.concurrent.CancellationException
import javax.smartcardio.CardTerminal

private const val WAIT_INTERVAL = 500L

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
		mapScioError {
			terminals.getScioTerminal(name)?.let {
				val card = it.connect("DIRECT")
				PcscTerminalConnection(this, card)
			} ?: throw ReaderUnavailable()
		}

	internal fun connectInternal(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	) = mapScioError {
		getScioTerminal().let {
			val shareMode = if (shareMode == ShareMode.EXCLUSIVE) "EXCLUSIVE;" else ""
			val protocol = protocol.toScioProtocol()
			val card = it.connect("$shareMode$protocol")
			card
		}
	}

	override fun connect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	): TerminalConnection = PcscTerminalConnection(this, connectInternal(protocol, shareMode))

	override suspend fun waitForCardPresent() {
		val t = getScioTerminal()
		waitForCard {
			mapScioError {
				t.waitForCardPresent(WAIT_INTERVAL)
			}
		}
	}

	override suspend fun waitForCardAbsent() {
		val t = getScioTerminal()
		waitForCard {
			mapScioError {
				t.waitForCardAbsent(WAIT_INTERVAL)
			}
		}
	}

	@Throws(ReaderUnavailable::class)
	internal fun getScioTerminal(): CardTerminal =
		mapScioError {
			terminals.getScioTerminal(name)
				?: throw ReaderUnavailable()
		}

	override fun toString(): String {
		val cardPresentString = runCatching { isCardPresent().toString() }.getOrElse { "Error(${it.message})" }
		return "PcscTerminal[name=$name, isCardPresent=$cardPresentString]"
	}
}

internal fun PreferredCardProtocol.toScioProtocol(): String =
	when (this) {
		PreferredCardProtocol.T0 -> "T=0"
		PreferredCardProtocol.T1 -> "T=1"
		PreferredCardProtocol.RAW -> throw IllegalArgumentException("RAW mode is not supported")
		PreferredCardProtocol.ANY -> "*"
	}

private suspend inline fun waitForCard(crossinline waitFun: suspend () -> Boolean) =
	coroutineScope {
		while (true) {
			if (!isActive) {
				// job has been cancelled, stop loop
				throw CancellationException("Waiting for a card event has been cancelled.")
			}
			if (waitFun()) {
				// change detected
				break
			}
		}
	}
