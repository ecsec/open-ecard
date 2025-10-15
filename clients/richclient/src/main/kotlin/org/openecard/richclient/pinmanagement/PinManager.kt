package org.openecard.richclient.pinmanagement

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.stage.Stage
import javafx.stage.WindowEvent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.openecard.richclient.sc.CardWatcher

class PinManager(
	cardWatcher: CardWatcher,
	private val stage: Stage,
) {
	private val bgTaskScope = CoroutineScope(Dispatchers.IO + CoroutineName("PinManagerTasks"))
	private val uiFactory = PinUiFactory(stage, cardWatcher, bgTaskScope)

	init {
		addOnCloseHandler {
			// cleanup when pin management is stopped
			bgTaskScope.cancel()
		}
	}

	fun addOnCloseHandler(eventHandler: EventHandler<WindowEvent>) {
		stage.addEventHandler(
			WindowEvent.WINDOW_CLOSE_REQUEST,
			eventHandler,
		)
	}

	fun openManagerDialog() {
		val controller = uiFactory.createSelectionUi()
		controller.start()
	}

	fun closeManagementDialog() {
		Platform.runLater {
			stage.close()
		}
	}

	fun toFront() {
		Platform.runLater {
			stage.toFront()
		}
	}

	companion object {
		fun create(cardWatcher: CardWatcher): PinManager {
			val stage = Stage()
			return PinManager(cardWatcher, stage)
		}
	}
}
