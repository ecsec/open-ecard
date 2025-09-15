package org.openecard.richclient.sc

interface CardState {
	val terminals: Set<String>
	val terminalsNoCard: Set<String>
	val terminalsWithCard: Set<String>
	val recognizedCards: List<RecognizedCard>

	data class RecognizedCard(
		val terminal: String,
		val cardType: String,
	)

	data class ImmutableCardState(
		override val terminals: Set<String>,
		override val terminalsNoCard: Set<String>,
		override val terminalsWithCard: Set<String>,
		override val recognizedCards: List<RecognizedCard>,
	) : CardState {
		fun addTerminal(terminalName: String): ImmutableCardState =
			copy(
				terminals = terminals + terminalName,
				terminalsNoCard = terminalsNoCard + terminalName,
			)

		fun removeTerminal(terminalName: String): ImmutableCardState =
			copy(
				terminals = terminals - terminalName,
				terminalsNoCard = terminalsNoCard - terminalName,
				terminalsWithCard = terminalsWithCard - terminalName,
				recognizedCards = recognizedCards.filterNot { it.terminal == terminalName },
			)

		fun insertCard(terminalName: String): ImmutableCardState =
			copy(
				terminalsNoCard = terminalsNoCard - terminalName,
				terminalsWithCard = terminalsWithCard + terminalName,
			)

		fun recognizeCard(
			terminalName: String,
			cardType: String,
		): ImmutableCardState =
			copy(
				recognizedCards = recognizedCards + RecognizedCard(terminalName, cardType),
			)

		fun removeCard(terminalName: String): ImmutableCardState =
			copy(
				recognizedCards = recognizedCards.filterNot { it.terminal == terminalName },
				terminalsWithCard = terminalsWithCard - terminalName,
				terminalsNoCard = terminalsNoCard + terminalName,
			)

		companion object {
			val Empty = ImmutableCardState(setOf(), setOf(), setOf(), listOf())
		}
	}
}
