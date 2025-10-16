package org.openecard.richclient.pinmanagement

import javafx.application.Platform
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.openecard.richclient.MR

class PinManagementStage(
	val stage: Stage,
) {
	init {
		Platform.runLater {
			stage.title = defaultTitle()
		}
	}

	fun showScene(scene: Scene) {
		Platform.runLater {
			stage.scene = scene
			if (!stage.isShowing) {
				stage.show()
			}
		}
	}

	fun replaceView(root: Parent) {
		Platform.runLater {
			stage.scene.root = root
		}
	}

	companion object {
		const val DEFAULT_WIDTH: Double = 420.0
		const val DEFAULT_HEIGHT: Double = 300.0

		fun defaultTitle(): String = MR.strings.pinmanage_name.localized()

		fun makeScene(root: Parent): Scene = Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT)
	}
}
