package org.openecard.sc.pcsc

import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.TerminalConnection
import org.openecard.sc.iface.feature.Feature

class PcscTerminalConnection(
	private val hwCard: au.id.micolous.kotlin.pcsc.Card,
	override val terminal: PcscTerminal,
) : TerminalConnection {
	override val isCardConnected: Boolean
		get() {
			return mapScioError { hwCard.status().present }
		}

	private var _card: PcscCard? = getCardInstance()

	private fun getCardInstance(): PcscCard? =
		mapScioError {
			if (hwCard.status().present) PcscCard(hwCard, this) else null
		}

	override val card: PcscCard? get() {
		return _card
	}

	override fun disconnect(disposition: CardDisposition) =
		mapScioError {
			hwCard.disconnect(disposition.toPcscDisconnect())
		}

	override fun reconnect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
		disposition: CardDisposition,
	) = mapScioError {
		hwCard.reconnect(shareMode.toPcsc(), setOf(protocol.toPcsc()), disposition.toPcscConnect())
		_card = getCardInstance()
	}

	override fun getFeatures(): Set<Feature> {
		TODO("Not yet implemented")
	}

	override fun beginTransaction() =
		mapScioError {
			hwCard.beginTransaction()
		}

	override fun endTransaction() =
		mapScioError {
			hwCard.endTransaction()
		}
}
