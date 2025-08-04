package org.openecard.sc.pcsc

interface PcscCardWatcherCallbacks {
	fun onTerminalAdded(terminalName: String)

	fun onTerminalRemoved(terminalName: String)

	fun onCardInserted(
		terminalName: String,
		cardName: String,
	)

	fun onCardRecognized(
		terminalName: String,
		cardName: String,
	)

	fun onCardRemoved(
		terminalName: String,
		cardName: String,
	)
}
