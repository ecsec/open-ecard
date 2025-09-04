package org.openecard.richclient.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import org.openecard.richclient.RichClient

class FXInitializer : Application() {
	override fun start(primaryStage: Stage) {
		Platform.setImplicitExit(false)

		primaryStage.isIconified = true
		primaryStage.hide()

		val client = RichClient()
		client.setup()
	}

	override fun stop() {
	}
}
