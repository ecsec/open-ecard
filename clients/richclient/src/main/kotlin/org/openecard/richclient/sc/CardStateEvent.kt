package org.openecard.richclient.sc

sealed interface CardStateEvent {
	data class InitialCardState(
		val cardState: CardState,
	) : CardStateEvent

	data class TerminalAdded(
		val terminalName: String,
	) : CardStateEvent

	data class TerminalRemoved(
		val terminalName: String,
	) : CardStateEvent

	data class CardInserted(
		val terminalName: String,
	) : CardStateEvent

	data class CardRemoved(
		val terminalName: String,
	) : CardStateEvent

	data class CardRecognized(
		val terminalName: String,
		val cardType: String,
	) : CardStateEvent
}
