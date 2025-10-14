package org.openecard.richclient.pinmanagement.common

import javafx.fxml.FXMLLoader
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MessageController(
	private val rootPane: StackPane,
	private val bgTaskScope: CoroutineScope,
) {
	fun showMessage(
		message: String,
		after: () -> Unit,
	) {
		val loader = FXMLLoader()
		loader.location = javaClass.getResource("/fxml/MessageView.fxml")
		val layout = loader.load<VBox>()
		val controller = loader.getController<MessageViewController>()
		controller.setMessage(message)

		bgTaskScope.launch(Dispatchers.JavaFx) {
			rootPane.children.setAll(layout)
			delay(3.seconds)
			after()
		}
	}
}
