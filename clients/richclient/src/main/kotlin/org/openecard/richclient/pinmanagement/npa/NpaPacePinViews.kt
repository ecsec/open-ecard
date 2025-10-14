package org.openecard.richclient.pinmanagement.npa

import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import org.openecard.richclient.pinmanagement.PinManagementStage

class NpaPacePinViews(
	private val stage: PinManagementStage,
) {
	fun showChangeFlow(onSubmit: (String, String) -> Unit) {
		val (view, controller) = loadFXML<Parent, PinChangeViewController>("PinChangeView.fxml")
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
		stage.show(view)
	}

	fun showCanFlow(onSubmit: (String) -> Unit) {
		val (view, controller) = loadFXML<Parent, CanEntryViewController>("CanEntryView.fxml")
		controller.onSubmit = { can, errorLabel ->
			when {
				can.isBlank() -> errorLabel.text = "CAN cannot be empty."
				can.length !in 5..6 -> errorLabel.text = "CAN must be 5 or 6 digits."
				else -> onSubmit(can)
			}
		}
		stage.show(view)
	}

	fun showPinRecoveryFlow(onSubmit: (String) -> Unit) {
		val (view, controller) = loadFXML<Parent, PinEntryViewController>("PinEntryView.fxml")
		controller.setTitle("Enter your PIN - last attempt.")
		controller.onSubmit = { pin, errorLabel ->
			when {
				pin.isBlank() -> errorLabel.text = "PIN cannot be empty."
				pin.length !in 5..6 -> errorLabel.text = "PIN must be 5 or 6 digits."
				else -> onSubmit(pin)
			}
		}
		stage.show(view)
	}

	fun showCanAndPinFlow(onSubmit: (String, String) -> Unit) {
		val (canView, canController) = loadFXML<Parent, CanEntryViewController>("CanEntryView.fxml")
		canController.onSubmit = { can, canError ->
			when {
				can.isBlank() -> canError.text = "CAN cannot be empty."
				can.length !in 5..6 -> canError.text = "CAN must be 5 or 6 digits."
				else -> {
					val (pinView, pinController) = loadFXML<Parent, PinEntryViewController>("PinEntryView.fxml")
					pinController.setTitle("Enter your PIN - last attempt.")
					pinController.onSubmit = { pin, pinError ->
						when {
							pin.isBlank() -> pinError.text = "PIN cannot be empty."
							pin.length !in 5..6 -> pinError.text = "PIN must be 5 or 6 digits."
							else -> onSubmit(can, pin)
						}
					}
					stage.show(pinView)
				}
			}
		}
		stage.show(canView)
	}

	fun showPukFlow(onSubmit: (String) -> Unit) {
		val (view, controller) = loadFXML<Parent, PukEntryViewController>("PukEntryView.fxml")
		controller.onSubmit = { puk, errorLabel ->
			when {
				puk.isBlank() -> errorLabel.text = "PUK cannot be empty."
				puk.length != 10 -> errorLabel.text = "PUK must be exactly 10 digits."
				else -> onSubmit(puk)
			}
		}
		stage.show(view)
	}

	private fun <V : Parent, C> loadFXML(fileName: String): Pair<V, C> {
		val loader = FXMLLoader(javaClass.getResource("/fxml/$fileName"))
		val view = loader.load<V>()
		val controller = loader.getController<C>()
		return view to controller
	}
}
