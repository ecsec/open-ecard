package org.openecard.richclient.pinmanagement.common

import javafx.fxml.FXML
import javafx.scene.control.Label

class MessageViewController {
	@FXML
	lateinit var messageLabel: Label

	fun setMessage(text: String) {
		messageLabel.text = text
	}
}
