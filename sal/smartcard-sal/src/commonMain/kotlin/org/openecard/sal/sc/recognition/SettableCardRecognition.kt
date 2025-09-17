package org.openecard.sal.sc.recognition

import org.openecard.sc.iface.CardChannel

class SettableCardRecognition : CardRecognition {
	private val recognizedCards: MutableMap<String, String> = mutableMapOf()

	fun addCard(
		terminal: String,
		cardType: String,
	) {
		recognizedCards[terminal] = cardType
	}

	fun removeCard(terminal: String) {
		recognizedCards.remove(terminal)
	}

	override fun recognizeCard(channel: CardChannel): String? =
		recognizedCards[channel.card.terminalConnection.terminal.name]
}
