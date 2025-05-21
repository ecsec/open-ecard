package org.openecard.sal.sc.recognition

import org.openecard.sc.iface.TerminalConnection

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

	override fun recognizeCard(connection: TerminalConnection) {
		recognizedCards[connection.terminal.name]
		TODO("return result")
	}
}
