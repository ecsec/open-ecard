package org.openecard.richclient.pinmanagement.npa

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import org.openecard.richclient.pinmanagement.common.MessageViewController
import java.util.Timer
import java.util.TimerTask

class NpaPacePinView(
	private val stage: Stage,
) {
	private val rootPane = StackPane()

	init {
		Platform.runLater {
			stage.scene = Scene(rootPane, 400.0, 350.0)
			stage.title = "PIN Management"
			stage.show()
		}
	}

	fun showChangeFlow(onSubmit: (String, String) -> Unit) {
		val (view, controller) = loadFXML<Parent, PinChangeController>("PinChangeView.fxml")
		controller.onSubmit = { old, new, repeat, errorLabel ->
			when {
				old.isBlank() || new.isBlank() || repeat.isBlank() ->
					errorLabel.text = "PIN fields cannot be empty."

				old.length !in 5..6 || new.length !in 5..6 || repeat.length !in 5..6 ->
					errorLabel.text = "Each PIN must be 5 or 6 digits."

				new != repeat ->
					errorLabel.text = "New PINs do not match."

				else -> onSubmit(old, new)
			}
		}
		show(view)
	}

	fun showCanFlow(onSubmit: (String) -> Unit) {
		val (view, controller) = loadFXML<Parent, CanEntryController>("CanEntryView.fxml")
		controller.onSubmit = { can, errorLabel ->
			when {
				can.isBlank() -> errorLabel.text = "CAN cannot be empty."
				can.length !in 5..6 -> errorLabel.text = "CAN must be 5 or 6 digits."
				else -> onSubmit(can)
			}
		}
		show(view)
	}

	fun showPinRecoveryFlow(onSubmit: (String) -> Unit) {
		val (view, controller) = loadFXML<Parent, PinEntryController>("PinEntryView.fxml")
		controller.setTitle("Enter your PIN - last attempt.")
		controller.onSubmit = { pin, errorLabel ->
			when {
				pin.isBlank() -> errorLabel.text = "PIN cannot be empty."
				pin.length !in 5..6 -> errorLabel.text = "PIN must be 5 or 6 digits."
				else -> onSubmit(pin)
			}
		}
		show(view)
	}

	fun showCanAndPinFlow(onSubmit: (String, String) -> Unit) {
		val (canView, canController) = loadFXML<Parent, CanEntryController>("CanEntryView.fxml")
		canController.onSubmit = { can, canError ->
			when {
				can.isBlank() -> canError.text = "CAN cannot be empty."
				can.length !in 5..6 -> canError.text = "CAN must be 5 or 6 digits."
				else -> {
					val (pinView, pinController) = loadFXML<Parent, PinEntryController>("PinEntryView.fxml")
					pinController.setTitle("Enter your PIN - last attempt.")
					pinController.onSubmit = { pin, pinError ->
						when {
							pin.isBlank() -> pinError.text = "PIN cannot be empty."
							pin.length !in 5..6 -> pinError.text = "PIN must be 5 or 6 digits."
							else -> onSubmit(can, pin)
						}
					}
					show(pinView)
				}
			}
		}
		show(canView)
	}

	fun showPukFlow(onSubmit: (String) -> Unit) {
		val (view, controller) = loadFXML<Parent, PukEntryController>("PukEntryView.fxml")
		controller.onSubmit = { puk, errorLabel ->
			when {
				puk.isBlank() -> errorLabel.text = "PUK cannot be empty."
				puk.length != 10 -> errorLabel.text = "PUK must be exactly 10 digits."
				else -> onSubmit(puk)
			}
		}
		show(view)
	}

	fun showMessage(
		message: String,
		after: () -> Unit,
	) {
		val loader = FXMLLoader()
		loader.location = javaClass.getResource("/fxml/MessageView.fxml")
		val layout = loader.load<VBox>()
		val controller = loader.getController<MessageViewController>()
		controller.setMessage(message)

		Platform.runLater {
			rootPane.children.setAll(layout)
		}

		Timer()
			.schedule(
				object : TimerTask() {
					override fun run() {
						Platform.runLater {
							after()
						}
					}
				},
				3000,
			)
	}

	private fun show(view: Parent) {
		Platform.runLater {
			rootPane.children.setAll(view)
		}
	}

	private fun <V : Parent, C> loadFXML(fileName: String): Pair<V, C> {
		val loader = FXMLLoader(javaClass.getResource("/fxml/$fileName"))
		val view = loader.load<V>()
		val controller = loader.getController<C>()
		return view to controller
	}
}
