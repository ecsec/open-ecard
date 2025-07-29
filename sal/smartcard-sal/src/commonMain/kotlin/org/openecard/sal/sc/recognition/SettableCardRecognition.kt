package org.openecard.sal.sc.recognition

import org.openecard.sc.iface.CardChannel

class SettableCardRecognition : CardRecognition {
	// TODO: use cardInfo
	private val recognizedCards: MutableMap<String, Unit> = mutableMapOf()

	fun addCard(
		terminal: String,
		cif: Unit,
	) {
		recognizedCards.put(terminal, cif)
	}

	fun removeCard(terminal: String) {
		recognizedCards.remove(terminal)
	}

	override fun recognizeCard(channel: CardChannel): String? {
		recognizedCards[channel.card.terminalConnection.terminal.name]
		TODO("Not yet implemented")
	}
}
