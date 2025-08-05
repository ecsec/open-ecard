package org.openecard.richclient

interface PcscCardWatcherCallbacks {
	fun onTerminalAdded(terminalName: String)

	fun onTerminalRemoved(terminalName: String)

	fun onCardInserted(terminalName: String)

	fun onCardRecognized(
		terminalName: String,
		cardType: String?,
	)

	fun onCardRemoved(terminalName: String)
}
