package org.openecard.richclient.pinmanagement.unsupported

import kotlinx.coroutines.CoroutineScope
import org.openecard.richclient.pinmanagement.PinManagementStage
import org.openecard.richclient.pinmanagement.PinManagementUI
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.pinmanagement.common.MessageController

class PlaceholderPinController(
	private val terminal: TerminalInfo,
	private val stage: PinManagementStage,
	private val bgTaskScope: CoroutineScope,
) : PinManagementUI {
	private val msgController by lazy { MessageController(stage, bgTaskScope) }

	override fun show() {
		msgController.showMessage(
			message = "PIN management for '${terminal.cardType}' is not supported yet.",
			after = {},
		)
	}

	override fun abortProcess() {
		msgController.showMessage(
			message = "PIN process aborted for '${terminal.cardType}'.",
			after = {},
		)
	}
}
