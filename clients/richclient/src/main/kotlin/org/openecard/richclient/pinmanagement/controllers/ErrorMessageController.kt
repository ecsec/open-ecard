package org.openecard.richclient.pinmanagement.controllers

import javafx.fxml.FXML
import javafx.scene.control.Label

class ErrorMessageController {
	@FXML
	lateinit var messageLabel: Label

	fun setMessage(text: String) {
		messageLabel.text = text
	}
}
