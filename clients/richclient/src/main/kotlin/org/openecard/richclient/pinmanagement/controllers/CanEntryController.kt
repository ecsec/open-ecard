package org.openecard.richclient.pinmanagement.controllers

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.PasswordField

class CanEntryController {
	@FXML lateinit var canField: PasswordField

	@FXML lateinit var errorLabel: Label

	var onSubmit: ((String, Label) -> Unit)? = null

	@FXML
	fun handleSubmit() {
		onSubmit?.invoke(canField.text, errorLabel)
	}

	fun setError(message: String) {
		errorLabel.text = message
	}
}
