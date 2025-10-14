package org.openecard.richclient.pinmanagement

import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope

class PinManagementStage(
	val stage: Stage,
	val bgTaskScope: CoroutineScope,
) {
	val rootPane = StackPane()

	init {
		Platform.runLater {
			stage.scene = Scene(rootPane, 400.0, 350.0)
			stage.title = "PIN Management"
			stage.show()
		}
	}

	fun show(view: Parent) {
		Platform.runLater {
			rootPane.children.setAll(view)
		}
	}
}
