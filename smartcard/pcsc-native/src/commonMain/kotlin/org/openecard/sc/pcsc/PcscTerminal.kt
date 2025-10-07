package org.openecard.sc.pcsc

import au.id.micolous.kotlin.pcsc.Context
import au.id.micolous.kotlin.pcsc.connect
import au.id.micolous.kotlin.pcsc.getStatus
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.Terminal
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.TerminalStateType

class PcscTerminal(
	override val name: String,
	override val terminals: PcscTerminals,
	private val context: Context,
) : Terminal {
	override fun isCardPresent(): Boolean = getState() == TerminalStateType.PRESENT

	override fun getState(): TerminalStateType =
		mapScioError {
			val status = context.getStatus(name)
			return status.eventState.toSc()
		}

	override fun connectTerminalOnly(): TerminalConnection =
		mapScioError {
			val card =
				context.connect(
					name,
					au.id.micolous.kotlin.pcsc.ShareMode.Direct,
					setOf(au.id.micolous.kotlin.pcsc.Protocol.Raw),
				)
			return PcscTerminalConnection(card, this)
		}

	override fun connect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
	): TerminalConnection =
		mapScioError {
			val card = context.connect(name, shareMode.toPcsc(), protocol.toPcsc())
			return PcscTerminalConnection(card, this)
		}

	override suspend fun waitForCardPresent() {
		TODO("Not yet implemented")
	}

	override suspend fun waitForCardAbsent() {
		TODO("Not yet implemented")
	}
}
