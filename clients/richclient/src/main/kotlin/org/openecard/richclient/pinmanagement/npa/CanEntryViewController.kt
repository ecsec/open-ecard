package org.openecard.richclient.pinmanagement.npa

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import org.openecard.richclient.gui.JfxUtils
import org.openecard.richclient.pinmanagement.CanPinEntryCallback
import org.openecard.richclient.pinmanagement.PinManagementStage

class CanEntryViewController {
	@FXML
	lateinit var canLabel: Label

	@FXML
	lateinit var canField: PasswordField

	@FXML
	lateinit var pinLabel: Label

	@FXML
	lateinit var pinField: PasswordField

	@FXML
	lateinit var errorLabel: Label

	var onSubmit: CanPinEntryCallback<CanEntryViewController>? = null

	@FXML
	fun handleSubmit() {
		if (validateInput()) {
			val cb = checkNotNull(onSubmit)
			cb.invoke(this, canField.text, pinField.text)
		}
	}

	private fun validateInput(): Boolean {
		val can = canField.text
		val pin = pinField.text

		return when {
			can.isBlank() -> {
				errorLabel.text = "CAN cannot be empty."
				false
			}
			can.length !in 5..6 -> {
				errorLabel.text = "CAN must be 5 or 6 digits."
				false
			}
			else ->
				when {
					pin.isBlank() -> {
						errorLabel.text = "PIN cannot be empty."
						false
					}
					pin.length !in 5..6 -> {
						errorLabel.text = "PIN must be 5 or 6 digits."
						false
					}
					else -> true
				}
		}
	}

	fun setError(message: String) {
		errorLabel.text = message
	}

	companion object {
		fun PinManagementStage.showCanPinFlow(onSubmit: CanPinEntryCallback<CanEntryViewController>): CanEntryViewController {
			val (view, controller) = JfxUtils.loadFxml<Parent, CanEntryViewController>("CanPinEntry.fxml")
			controller.onSubmit = onSubmit
			controller.pinLabel.text = "Enter your PIN - last attempt."
			replaceView(view)
			return controller
		}
	}
}
