package org.openecard.richclient.sc

class CardStateManager :
	PcscCardWatcherCallbacks,
	CardStateProvider {
	private val terminals: MutableList<String> = mutableListOf()
	private val terminalsNoCard: MutableList<String> = mutableListOf()
	private val terminalsWithCard: MutableList<String> = mutableListOf()
	private val recognizedCards: MutableList<CardState.RecognizedCard> = mutableListOf()

	/**
	 * Get snapshot of current state.
	 */
	override val state: CardState
		get() {
			synchronized(this) {
				val terminals = this.terminals.toList()
				val terminalsNoCard = this.terminalsNoCard.toList()
				val terminalsWithCard = this.terminalsWithCard.toList()
				val recognizedCards = this.recognizedCards.toList()
				return object : CardState {
					override val terminals: List<String> get() = terminals
					override val terminalsNoCard: List<String> get() = terminalsNoCard
					override val terminalsWithCard: List<String> get() = terminalsWithCard
					override val recognizedCards: List<CardState.RecognizedCard> get() = recognizedCards
				}
			}
		}

	override fun onTerminalAdded(terminalName: String) {
		synchronized(this) {
			terminals.add(terminalName)
			terminalsNoCard.add(terminalName)
		}
	}

	override fun onTerminalRemoved(terminalName: String) {
		synchronized(this) {
			terminals.remove(terminalName)
			terminalsNoCard.remove(terminalName)
			terminalsWithCard.remove(terminalName)
			recognizedCards.removeIf { it.terminal == terminalName }
		}
	}

	override fun onCardInserted(terminalName: String) {
		synchronized(this) {
			terminalsNoCard.remove(terminalName)
			terminalsWithCard.add(terminalName)
		}
	}

	override fun onCardRecognized(
		terminalName: String,
		cardType: String,
	) {
		synchronized(this) {
			recognizedCards.add(CardState.RecognizedCard(terminalName, cardType))
		}
	}

	override fun onCardRemoved(terminalName: String) {
		synchronized(this) {
			recognizedCards.removeIf { it.terminal == terminalName }
			terminalsWithCard.remove(terminalName)
			terminalsNoCard.add(terminalName)
		}
	}

	override suspend fun waitForCardType(vararg types: String): String {
		TODO("Not yet implemented")
	}
}
