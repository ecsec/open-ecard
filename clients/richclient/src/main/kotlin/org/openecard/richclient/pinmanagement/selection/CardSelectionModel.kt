package org.openecard.richclient.pinmanagement.selection

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.sc.CardState
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith

class CardSelectionModel(
	private val supportedCardTypes: Set<String>,
	private val cardWatcher: CardWatcher,
	private val bgTaskScope: CoroutineScope,
) {
	val terminals: ObservableList<TerminalInfo> = FXCollections.observableArrayList()

	private var watcher: Job? = null

	fun registerWatcher(onUpdate: () -> Unit) {
		// run callback in task scope, so it gets removed when we are finished
		watcher =
			object : CardWatcherCallback.CardWatcherCallbackDefault() {
				override fun onInitialState(cardState: CardState) {
					val recognized =
						cardState.recognizedCards
							.filter { supportedCardTypes.contains(it.cardType) }
							.map { TerminalInfo(it.terminal, it.cardType) }

					Platform.runLater {
						terminals.setAll(recognized)
						onUpdate()
					}
				}

				override fun onCardRecognized(
					terminalName: String,
					cardType: String,
				) {
					if (supportedCardTypes.contains(cardType) &&
						terminals.none { it.terminalName == terminalName }
					) {
						Platform.runLater {
							terminals.add(TerminalInfo(terminalName, cardType))
							onUpdate()
						}
					}
				}

				override fun onCardRemoved(terminalName: String) {
					Platform.runLater {
						val removed = terminals.find { it.terminalName == terminalName }
						if (removed != null) {
							terminals.remove(removed)
							onUpdate()
						}
					}
				}
			}.registerWith(cardWatcher, bgTaskScope)
	}

	fun stopWatcher() {
		watcher?.cancel()
		watcher = null
	}
}
