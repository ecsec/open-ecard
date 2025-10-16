package org.openecard.richclient.pinmanagement.npa

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import org.openecard.richclient.MokoResourceBundle
import org.openecard.richclient.gui.JfxUtils
import org.openecard.richclient.pinmanagement.CanPinEntryCallback
import org.openecard.richclient.pinmanagement.PinManagementStage

class CanEntryViewController {
	@FXML
	private lateinit var infoTextFlow: TextFlow

	@FXML
	private lateinit var canField: PasswordField

	@FXML
	private lateinit var pinField: PasswordField

	@FXML
	lateinit var errorLabel: Label

	@FXML
	private lateinit var resources: MokoResourceBundle

	var onSubmit: CanPinEntryCallback<CanEntryViewController>? = null

	@FXML
	fun initialize() {
		val description = resources.getString("pinmanage_npa_pin_suspend_description")
		infoTextFlow.children.setAll(Text(description))
	}

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
				errorLabel.text = resources.getString("pinmanage_npa_pin_suspend_error_empty_can")
				false
			}
			can.length !in 5..6 -> {
				errorLabel.text = resources.getString("pinmanage_npa_pin_suspend_error_invalid_can")
				false
			}
			pin.isBlank() -> {
				errorLabel.text = resources.getString("pinmanage_npa_pin_suspend_error_empty_pin")
				false
			}
			pin.length !in 5..6 -> {
				errorLabel.text = resources.getString("pinmanage_npa_pin_suspend_error_invalid_pin")
				false
			}
			else -> true
		}
	}

	fun setError(message: String) {
		errorLabel.text = message
	}

	companion object {
		fun PinManagementStage.showCanPinFlow(onSubmit: CanPinEntryCallback<CanEntryViewController>): CanEntryViewController {
			val (view, controller) = JfxUtils.loadFxml<Parent, CanEntryViewController>("CanPinEntry.fxml")
			controller.onSubmit = onSubmit
			replaceView(view)
			return controller
		}
	}
}
