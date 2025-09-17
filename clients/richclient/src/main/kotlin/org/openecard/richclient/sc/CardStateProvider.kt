package org.openecard.richclient.sc

interface CardStateProvider {
	val state: CardState

	/**
	 * Waits for a card of the specified type to be inserted.
	 * @return Terminals where the detected cards are inserted.
	 */
	suspend fun waitForCardType(vararg types: String): String
}
