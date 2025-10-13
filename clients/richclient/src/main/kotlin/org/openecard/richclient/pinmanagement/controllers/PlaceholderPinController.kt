package org.openecard.richclient.pinmanagement.controllers

import org.openecard.richclient.pinmanagement.PinManagementUI
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.pinmanagement.npa.NpaPacePinView

class PlaceholderPinController(
	private val terminal: TerminalInfo,
	private val view: NpaPacePinView,
) : PinManagementUI {
	override fun show() {
		view.showMessage(
			message = "PIN management for '${terminal.cardType}' is not supported yet.",
			after = {},
		)
	}

	override fun abortProcess() {
		view.showMessage(
			message = "PIN process aborted for '${terminal.cardType}'.",
			after = {},
		)
	}
}
