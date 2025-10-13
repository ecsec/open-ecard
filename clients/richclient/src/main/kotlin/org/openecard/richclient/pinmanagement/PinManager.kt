package org.openecard.richclient.pinmanagement

import javafx.stage.Stage
import org.openecard.richclient.pinmanagement.ui.PinUiFactory
import org.openecard.richclient.sc.CardWatcher

class PinManager(
	stage: Stage,
	cardWatcher: CardWatcher,
) {
	private val uiFactory = PinUiFactory(stage, cardWatcher)

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
