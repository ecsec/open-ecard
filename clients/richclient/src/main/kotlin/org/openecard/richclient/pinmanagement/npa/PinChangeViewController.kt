package org.openecard.richclient.pinmanagement.npa

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.PasswordField

class PinChangeViewController {
	@FXML
	private lateinit var oldPinField: PasswordField

	@FXML
	private lateinit var newPinField: PasswordField

	@FXML
	private lateinit var repeatPinField: PasswordField

	@FXML
	private lateinit var errorLabel: Label

	var onSubmit: ((String, String, String, Label) -> Unit)? = null

	@FXML
	fun handleSubmit() {
		onSubmit?.invoke(oldPinField.text, newPinField.text, repeatPinField.text, errorLabel)
	}
}
