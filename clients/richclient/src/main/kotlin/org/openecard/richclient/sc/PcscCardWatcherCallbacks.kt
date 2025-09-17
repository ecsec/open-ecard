package org.openecard.richclient.sc

interface PcscCardWatcherCallbacks {
	fun onTerminalAdded(terminalName: String)

	fun onTerminalRemoved(terminalName: String)

	fun onCardInserted(terminalName: String)

	fun onCardRecognized(
		terminalName: String,
		cardType: String,
	)

	fun onCardRemoved(terminalName: String)

	class BatchPcscCardWatcherCallbacks(
		val callbacks: List<PcscCardWatcherCallbacks>,
	) : PcscCardWatcherCallbacks {
		override fun onTerminalAdded(terminalName: String) {
			callbacks.forEach { it.onTerminalAdded(terminalName) }
		}

		override fun onTerminalRemoved(terminalName: String) {
			callbacks.forEach { it.onTerminalRemoved(terminalName) }
		}

		override fun onCardInserted(terminalName: String) {
			callbacks.forEach { it.onCardInserted(terminalName) }
		}

		override fun onCardRecognized(
			terminalName: String,
			cardType: String,
		) {
			callbacks.forEach { it.onCardRecognized(terminalName, cardType) }
		}

		override fun onCardRemoved(terminalName: String) {
			callbacks.forEach { it.onCardRemoved(terminalName) }
		}
	}
}
