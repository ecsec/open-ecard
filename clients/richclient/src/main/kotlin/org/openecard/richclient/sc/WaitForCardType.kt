package org.openecard.richclient.sc

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import org.openecard.utils.common.returnIf

object WaitForCardType {
	/**
	 * Waits for a card of the specified type to be inserted.
	 * @return Terminals where the detected cards are inserted.
	 */
	suspend fun CardWatcher.waitForCard(vararg cardTypes: String): List<String> {
		val watcher = this
		val events = watcher.registerSink()
		try {
			val cards =
				events
					.mapNotNull { evt ->
						when (evt) {
							is CardStateEvent.InitialCardState ->
								evt.cardState.recognizedCards
									.filter { it.cardType in cardTypes }
									.map { CardState.RecognizedCard(it.terminal, it.cardType) }
									.takeIf { it.isNotEmpty() }
							is CardStateEvent.CardRecognized ->
								evt
									.returnIf {
										it.cardType in cardTypes
									}?.let { listOf(CardState.RecognizedCard(it.terminalName, it.cardType)) }
							else -> null
						}
					}.first()
			return cards.map { it.terminal }
		} finally {
			watcher.unregisterSink(events)
		}
	}
}
