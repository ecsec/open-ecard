package org.openecard.richclient.pinmanagement

import javafx.stage.Stage
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import org.openecard.richclient.sc.CardWatcher

class PinManager(
	stage: Stage,
	cardWatcher: CardWatcher,
) {
	private val bgTaskScope = CoroutineScope(Dispatchers.IO + CoroutineName("PinManagerTasks"))
	private val uiFactory = PinUiFactory(stage, cardWatcher, bgTaskScope)

	init {
		stage.addEventHandler(javafx.stage.WindowEvent.WINDOW_CLOSE_REQUEST) {
			// cleanup when pin management is stopped
			bgTaskScope.cancel()
		}
	}

	fun openManagerDialog() {
		val controller = uiFactory.createSelectionUi()
		controller.start()
	}

	fun closeManagementDialog() {
		uiFactory.closeStage()
	}

	fun toFront() {
		uiFactory.dialogStage.toFront()
	}
}
