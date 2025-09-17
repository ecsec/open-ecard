package org.openecard.richclient.sc

interface CardState {
	val terminals: List<String>
	val terminalsNoCard: List<String>
	val terminalsWithCard: List<String>
	val recognizedCards: List<RecognizedCard>

	data class RecognizedCard(
		val terminal: String,
		val cardType: String,
	)
}
