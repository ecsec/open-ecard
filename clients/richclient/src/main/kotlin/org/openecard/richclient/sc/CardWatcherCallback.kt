package org.openecard.richclient.sc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

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

	/**
	 * Class providing no-op default methods for all callback functions.
	 */
	open class CardWatcherCallbackDefault : CardWatcherCallback {
		override fun onInitialState(cardState: CardState) {}

		override fun onTerminalAdded(terminalName: String) {}

		override fun onTerminalRemoved(terminalName: String) {}

		override fun onCardInserted(terminalName: String) {}

		override fun onCardRemoved(terminalName: String) {}

		override fun onCardRecognized(
			terminalName: String,
			cardType: String,
		) {}
	}

	companion object {
		fun CardWatcherCallback.registerWith(
			watcher: CardWatcher,
			scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
		): Job =
			scope.launch {
				val sink = watcher.registerSink()
				try {
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
				} catch (ex: CancellationException) {
					// we got cancelled, so remove the callback from the watcher
					watcher.unregisterSink(sink)
					throw ex
				}
			}
	}
}
