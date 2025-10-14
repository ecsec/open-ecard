package org.openecard.richclient.pinmanagement.selection

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import kotlinx.coroutines.CoroutineScope
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
	var selectedTerminal: TerminalInfo? = null

	fun registerWatcher(
		onUpdate: () -> Unit,
		onError: (String) -> Unit,
	) {
		// run callback in task scope, so it gets removed when we are finished
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
						if (selectedTerminal?.terminalName == terminalName) {
							selectedTerminal = null
						}
						onUpdate()
					}
				}
			}

			override fun onTerminalRemoved(terminalName: String) {
				Platform.runLater {
					if (selectedTerminal?.terminalName == terminalName) {
						selectedTerminal = null
					}
				}
			}
		}.registerWith(cardWatcher, bgTaskScope)
	}

	fun selectTerminal(terminal: TerminalInfo) {
		selectedTerminal = terminal
	}
}
