package org.openecard.richclient.pinmanagement.npa

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openecard.richclient.pinmanagement.PinManagementStage
import org.openecard.richclient.pinmanagement.PinManagementUI
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.pinmanagement.common.MessageController
import org.openecard.richclient.pinmanagement.npa.CanEntryViewController.Companion.showCanPinFlow
import org.openecard.richclient.pinmanagement.npa.PinChangeViewController.Companion.showChangeFlow
import org.openecard.richclient.pinmanagement.npa.PukEntryViewController.Companion.showPukFlow

private val log = KotlinLogging.logger { }

class NpaPinController(
	private val terminal: TerminalInfo,
	private val stage: PinManagementStage,
	private val bgTaskScope: CoroutineScope,
) : PinManagementUI {
	private val msgController = MessageController(stage, bgTaskScope)

	private var model: NpaPacePinModel? = null

	override fun show() {
		try {
			val model = NpaPacePinModel.createConnectedModel(terminal)
			this.model = model
			// check pin status and decide which UI we need
			val status = model.getPinStatus()

			when (status) {
				PinStatus.OK -> stage.showChangeFlow { _, old, new -> changePin(old, new) }
				PinStatus.Suspended -> stage.showCanPinFlow { view, can, pin -> suspendRecovery(view, can, pin) }
				PinStatus.Blocked -> stage.showPukFlow { _, puk -> unblockPin(puk) }
				PinStatus.Unknown -> msgController.showMessage("Unable to determine PIN status.") {}
			}
		} catch (e: Exception) {
			closeProcess()
			msgController.showMessage("Error: ${e.message}") {}
		}
	}

	override fun closeProcess() {
		try {
			model?.shutdownStack()
			model = null
		} catch (ex: Exception) {
			log.error(ex.takeIf { log.isDebugEnabled() }) { "Failed to shutdown smartcard stack: ${ex.message}" }
		}
	}

	private fun changePin(
		old: String,
		new: String,
	) {
		bgTaskScope.launch {
			try {
				val model = checkNotNull(model)
				val success = model.changePin(old, new)

				if (success) {
					msgController.showMessage("PIN changed successfully.") {
						stage.showChangeFlow { _, old, new -> changePin(old, new) }
					}
				} else {
					val status = model.getPinStatus()
					when (status) {
						PinStatus.OK ->
							msgController.showMessage("PIN incorrect. 2 retries left.") {
								stage.showChangeFlow { _, old, new -> changePin(old, new) }
							}
						PinStatus.Suspended ->
							msgController.showMessage("PIN suspended. Please enter CAN.") {
								stage.showCanPinFlow { view, can, pin -> suspendRecovery(view, can, pin) }
							}
						PinStatus.Blocked ->
							msgController.showMessage("PIN blocked. Please enter PUK.") {
								stage.showPukFlow { _, puk -> unblockPin(puk) }
							}
						else -> msgController.showMessage("PIN change failed.") {}
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("Error: ${e.message}") {}
			}
		}
	}

	private fun suspendRecovery(
		view: CanEntryViewController,
		can: String,
		pin: String,
	) {
		bgTaskScope.launch {
			try {
				val model = checkNotNull(model)

				if (!model.enterCan(can)) {
					view.errorLabel.text = "Wrong CAN. Please try again."
				} else {
					val success = model.enterPin(pin)
					if (success) {
						msgController.showMessage("PIN recovered successfully.") {
							stage.showChangeFlow { _, old, new -> changePin(old, new) }
						}
					} else {
						val status = model.getPinStatus()
						when (status) {
							PinStatus.Blocked ->
								msgController.showMessage("PIN blocked. Please enter PUK.") {
									stage.showPukFlow { _, puk -> unblockPin(puk) }
								}
							else ->
								msgController.showMessage("PIN recovery failed. Please try again.") {
									stage.showCanPinFlow { view, retryCan, retryPin -> suspendRecovery(view, retryCan, retryPin) }
								}
						}
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("Error: ${e.message}") {}
			}
		}
	}

	private fun unblockPin(puk: String) {
		bgTaskScope.launch {
			try {
				val model = checkNotNull(model)

				if (model.enterPuk(puk)) {
					msgController.showMessage("PIN unblocked successfully.") {
						stage.showChangeFlow { _, old, new -> changePin(old, new) }
					}
				} else {
					msgController.showMessage("Wrong PUK. Please try again.") {
						stage.showPukFlow { _, retryPuk -> unblockPin(retryPuk) }
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("Error: ${e.message}") {}
			}
		}
	}
}
