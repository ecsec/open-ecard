package org.openecard.richclient.pinmanagement.npa

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openecard.richclient.gui.JfxUtils
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
	private val resources = JfxUtils.richclientResourceBundle

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
				PinStatus.Unknown -> msgController.showMessage(resources.getString("pinmanage_message_unknown_status")) {}
			}
		} catch (e: Exception) {
			closeProcess()
			msgController.showMessage("${resources.getString("pinmanage_common_error_title")}: ${e.message}") {}
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
					msgController.showMessage(resources.getString("pinmanage_message_pin_changed")) {
						stage.showChangeFlow { _, old, new -> changePin(old, new) }
					}
				} else {
					when (model.getPinStatus()) {
						PinStatus.OK ->
							msgController.showMessage(resources.getString("pinmanage_message_pin_incorrect")) {
								stage.showChangeFlow { _, old, new -> changePin(old, new) }
							}
						PinStatus.Suspended ->
							msgController.showMessage(resources.getString("pinmanage_message_pin_suspended")) {
								stage.showCanPinFlow { view, can, pin -> suspendRecovery(view, can, pin) }
							}
						PinStatus.Blocked ->
							msgController.showMessage(resources.getString("pinmanage_message_pin_blocked")) {
								stage.showPukFlow { _, puk -> unblockPin(puk) }
							}
						else -> msgController.showMessage(resources.getString("pinmanage_message_pin_change_failed")) {}
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("${resources.getString("pinmanage_common_error_title")}: ${e.message}") {}
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
					view.errorLabel.text = resources.getString("pinmanage_message_wrong_can")
				} else {
					val success = model.enterPin(pin)
					if (success) {
						msgController.showMessage(resources.getString("pinmanage_message_pin_recovered")) {
							stage.showChangeFlow { _, old, new -> changePin(old, new) }
						}
					} else {
						when (model.getPinStatus()) {
							PinStatus.Blocked ->
								msgController.showMessage(resources.getString("pinmanage_message_pin_blocked")) {
									stage.showPukFlow { _, puk -> unblockPin(puk) }
								}
							else ->
								msgController.showMessage(resources.getString("pinmanage_message_pin_recovery_failed")) {
									stage.showCanPinFlow { view, retryCan, retryPin -> suspendRecovery(view, retryCan, retryPin) }
								}
						}
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("${resources.getString("pinmanage_common_error_title")}: ${e.message}") {}
			}
		}
	}

	private fun unblockPin(puk: String) {
		bgTaskScope.launch {
			try {
				val model = checkNotNull(model)

				if (model.enterPuk(puk)) {
					msgController.showMessage(resources.getString("pinmanage_message_pin_unblocked")) {
						stage.showChangeFlow { _, old, new -> changePin(old, new) }
					}
				} else {
					msgController.showMessage(resources.getString("pinmanage_message_wrong_puk")) {
						stage.showPukFlow { _, retryPuk -> unblockPin(retryPuk) }
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("${resources.getString("pinmanage_common_error_title")}: ${e.message}") {}
			}
		}
	}
}
