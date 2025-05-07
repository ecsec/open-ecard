package org.openecard.sc.pcsc

import org.openecard.sc.iface.Card
import org.openecard.sc.iface.CardDisposition
import org.openecard.sc.iface.Feature
import org.openecard.sc.iface.PreferredCardProtocol
import org.openecard.sc.iface.ShareMode
import org.openecard.sc.iface.TerminalConnection

class PcscTerminalConnection(
	override val terminal: PcscTerminal,
	internal var scioCard: javax.smartcardio.Card,
) : TerminalConnection {
	private var _card: Card? = null
	override val card: Card?
		get() = _card

	init {
		setInternalCard()
	}

	private fun setInternalCard() {
		if (scioCard.protocol != "DIRECT") {
			_card = PcscCard(this, scioCard)
		}
	}

	override val isCardConnected: Boolean
		get() = card != null

	override fun disconnect(disposition: CardDisposition) {
		scioCard.disconnect(disposition != CardDisposition.LEAVE)
	}

	override fun reconnect(
		protocol: PreferredCardProtocol,
		shareMode: ShareMode,
		disposition: CardDisposition,
	) {
		scioCard.disconnect(disposition != CardDisposition.LEAVE)
		scioCard = terminal.connectInternal(protocol, shareMode)
		setInternalCard()
	}

	fun controlCommand(
		code: Int,
		command: ByteArray,
	): ByteArray = scioCard.transmitControlCommand(code, command)

	override val features: Set<Feature> by lazy {
		val info = FeatureInfo(this)
		info.featureMap.toFeatures(this)
	}

	override fun beginTransaction() {
		scioCard.beginExclusive()
	}

	override fun endTransaction() {
		scioCard.endExclusive()
	}
}
