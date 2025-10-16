package org.openecard.richclient.pinmanagement.npa

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import org.openecard.richclient.MokoResourceBundle
import org.openecard.richclient.gui.JfxUtils
import org.openecard.richclient.pinmanagement.PinChangeCallback
import org.openecard.richclient.pinmanagement.PinManagementStage

class PinChangeViewController {
	@FXML
	private lateinit var oldPinField: PasswordField

	@FXML
	private lateinit var newPinField: PasswordField

	@FXML
	private lateinit var repeatPinField: PasswordField

	@FXML
	private lateinit var errorLabel: Label

	@FXML
	private lateinit var resources: MokoResourceBundle

	var onSubmit: PinChangeCallback<PinChangeViewController>? = null

	@FXML
	fun handleSubmit() {
		if (validateInput()) {
			checkNotNull(onSubmit).invoke(this, oldPinField.text, newPinField.text)
		}
	}

	private fun validateInput(): Boolean {
		val old = oldPinField.text
		val new = newPinField.text
		val repeat = repeatPinField.text

		return when {
			old.isBlank() || new.isBlank() || repeat.isBlank() -> {
				errorLabel.text = resources.getString("pinmanage_npa_pin_change_error_empty_fields")
				false
			}
			old.length !in 5..6 || new.length !in 5..6 || repeat.length !in 5..6 -> {
				errorLabel.text = resources.getString("pinmanage_npa_pin_change_error_invalid_length")
				false
			}
			new != repeat -> {
				errorLabel.text = resources.getString("pinmanage_npa_pin_change_error_mismatch")
				false
			}
			else -> true
		}
	}

	companion object {
		fun PinManagementStage.showChangeFlow(onSubmit: PinChangeCallback<PinChangeViewController>): PinChangeViewController {
			val (view, controller) = JfxUtils.loadFxml<Parent, PinChangeViewController>("PinChange.fxml")
			controller.onSubmit = onSubmit
			replaceView(view)
			return controller
		}
	}
}
