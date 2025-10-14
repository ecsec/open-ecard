package org.openecard.richclient.pinmanagement.npa

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.PasswordField

class PinEntryViewController {
	@FXML
	lateinit var titleLabel: Label

	@FXML
	lateinit var pinField: PasswordField

	@FXML
	lateinit var errorLabel: Label

	var onSubmit: ((String, Label) -> Unit)? = null

	@FXML
	fun handleSubmit() {
		onSubmit?.invoke(pinField.text, errorLabel)
	}

	fun setTitle(text: String) {
		titleLabel.text = text
	}

	fun setError(message: String) {
		errorLabel.text = message
	}
}
