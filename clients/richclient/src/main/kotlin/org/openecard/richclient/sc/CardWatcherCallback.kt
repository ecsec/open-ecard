package org.openecard.richclient.sc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

interface CardWatcherCallback {
	fun onInitialState(cardState: CardState)

	fun onTerminalAdded(terminalName: String)

	fun onTerminalRemoved(terminalName: String)

	fun onCardInserted(terminalName: String)

	fun onCardRemoved(terminalName: String)

	fun onCardRecognized(
		terminalName: String,
		cardType: String,
	)

	companion object {
		fun CardWatcherCallback.registerWith(
			watcher: CardWatcher,
			scope: CoroutineScope = CoroutineScope(Dispatchers.Default),
		): Job =
			scope.launch {
				val sink = watcher.registerSink()
				sink.collect {
					when (it) {
						is CardStateEvent.InitialCardState -> onInitialState(it.cardState)
						is CardStateEvent.TerminalAdded -> onTerminalAdded(it.terminalName)
						is CardStateEvent.TerminalRemoved -> onTerminalRemoved(it.terminalName)
						is CardStateEvent.CardInserted -> onCardInserted(it.terminalName)
						is CardStateEvent.CardRemoved -> onCardRemoved(it.terminalName)
						is CardStateEvent.CardRecognized -> onCardRecognized(it.terminalName, it.cardType)
					}
				}
			}
	}
}
