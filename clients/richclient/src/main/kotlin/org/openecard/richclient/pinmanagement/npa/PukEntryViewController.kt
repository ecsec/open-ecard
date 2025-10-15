package org.openecard.richclient.pinmanagement.npa

import javafx.fxml.FXML
import javafx.scene.Parent
import javafx.scene.control.Label
import javafx.scene.control.PasswordField
import org.openecard.richclient.gui.JfxUtils
import org.openecard.richclient.pinmanagement.PasswordEntryCallback
import org.openecard.richclient.pinmanagement.PinManagementStage

class PukEntryViewController {
	@FXML
	lateinit var pukField: PasswordField

	@FXML
	lateinit var errorLabel: Label

	var onSubmit: PasswordEntryCallback<PukEntryViewController>? = null

	@FXML
	fun handleSubmit() {
		if (validateInput()) {
			checkNotNull(onSubmit).invoke(this, pukField.text)
		}
	}

	private fun validateInput(): Boolean {
		val puk = pukField.text
		return when {
			puk.isBlank() -> {
				errorLabel.text = "PUK cannot be empty."
				false
			}
			puk.length != 10 -> {
				errorLabel.text = "PUK must be exactly 10 digits."
				false
			}
			else -> true
		}
	}

	fun setError(message: String) {
		errorLabel.text = message
	}

	companion object {
		fun PinManagementStage.showPukFlow(onSubmit: PasswordEntryCallback<PukEntryViewController>): PukEntryViewController {
			val (view, controller) = JfxUtils.loadFxml<Parent, PukEntryViewController>("PukEntry.fxml")
			controller.onSubmit = onSubmit
			replaceView(view)
			return controller
		}
	}
}
