package org.openecard.richclient.sc

import org.openecard.sal.sc.recognition.CardRecognition
import org.openecard.sc.iface.CardChannel

class EventCardRecognition :
	CardRecognition,
	CardWatcherCallback {
	private var recognizedCards: List<CardState.RecognizedCard> = listOf()

	override fun recognizeCard(channel: CardChannel): String? =
		recognizedCards
			.find {
				it.terminal == channel.card.terminalConnection.terminal.name
			}?.cardType

	override fun onInitialState(cardState: CardState) {
		recognizedCards = cardState.recognizedCards
	}

	override fun onTerminalAdded(terminalName: String) {
	}

	override fun onTerminalRemoved(terminalName: String) {
		recognizedCards = recognizedCards.filterNot { it.terminal == terminalName }
	}

	override fun onCardInserted(terminalName: String) {
	}

	override fun onCardRemoved(terminalName: String) {
		recognizedCards = recognizedCards.filterNot { it.terminal == terminalName }
	}

	override fun onCardRecognized(
		terminalName: String,
		cardType: String,
	) {
		recognizedCards = recognizedCards + CardState.RecognizedCard(terminalName, cardType)
	}
}
