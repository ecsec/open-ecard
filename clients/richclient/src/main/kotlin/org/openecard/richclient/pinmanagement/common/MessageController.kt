package org.openecard.richclient.pinmanagement.common

import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import org.openecard.richclient.gui.JfxUtils
import org.openecard.richclient.pinmanagement.PinManagementStage
import org.openecard.richclient.res.MR
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MessageController(
	private val stage: PinManagementStage,
	private val bgTaskScope: CoroutineScope,
) {
	fun showMessage(
		message: String,
		waitDelay: Duration = 3.seconds,
		after: () -> Unit,
	) {
		val (view, controller) = JfxUtils.loadFxml<Parent, MessageViewController>("Message.fxml")
		controller.setMessage(message)

		bgTaskScope.launch(Dispatchers.JavaFx) {
			stage.replaceView(view)
			delay(waitDelay)
			after()
		}
	}

	fun showErrorDialog(
		message: String,
		dialogTitle: String = MR.strings.pinmanage_common_error_title.localized(),
		waitDelay: Duration = 3.seconds,
		after: () -> Unit,
	) {
		val (view, controller) = JfxUtils.loadFxml<Parent, ErrorMessageViewController>("ErrorMessage.fxml")
		controller.setMessage(message)

		bgTaskScope.launch(Dispatchers.JavaFx) {
			val errorStage =
				Stage().apply {
					title = dialogTitle
					scene = Scene(view)
					sizeToScene()
					initModality(Modality.WINDOW_MODAL)
				}

			errorStage.show()
			delay(waitDelay)
			errorStage.close()

			after()
		}
	}
}
