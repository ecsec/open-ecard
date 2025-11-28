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
import org.openecard.richclient.res.MR

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

			if (model.pacePin.capturePasswordInHardware()) {
				msgController.showMessage(MR.strings.pinmanage_message_terminal_not_supported.localized()) {}
				return
			}

			// check pin status and decide which UI we need
			val status = model.getPinStatus()

			when (status) {
				PinStatus.OK -> stage.showChangeFlow { _, old, new -> changePin(old, new) }
				PinStatus.Suspended -> stage.showCanPinFlow { view, can, pin -> suspendRecovery(view, can, pin) }
				PinStatus.Blocked -> stage.showPukFlow { _, puk -> unblockPin(puk) }
				PinStatus.Unknown -> msgController.showMessage(MR.strings.pinmanage_message_unknown_status.localized()) {}
			}
		} catch (e: Exception) {
			closeProcess()
			msgController.showMessage("${MR.strings.pinmanage_common_error_title.localized()}: ${e.message}") {}
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
					msgController.showMessage(MR.strings.pinmanage_message_pin_changed.localized()) {
						stage.showChangeFlow { _, old, new -> changePin(old, new) }
					}
				} else {
					when (model.getPinStatus()) {
						PinStatus.OK -> {
							msgController.showMessage(MR.strings.pinmanage_message_pin_incorrect.localized()) {
								stage.showChangeFlow { _, old, new -> changePin(old, new) }
							}
						}

						PinStatus.Suspended -> {
							msgController.showMessage(MR.strings.pinmanage_message_pin_suspended.localized()) {
								stage.showCanPinFlow { view, can, pin -> suspendRecovery(view, can, pin) }
							}
						}

						PinStatus.Blocked -> {
							msgController.showMessage(MR.strings.pinmanage_message_pin_blocked.localized()) {
								stage.showPukFlow { _, puk -> unblockPin(puk) }
							}
						}

						else -> {
							msgController.showMessage(MR.strings.pinmanage_message_pin_change_failed.localized()) {}
						}
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("${MR.strings.pinmanage_common_error_title.localized()}: ${e.message}") {}
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
					view.errorLabel.text = MR.strings.pinmanage_message_wrong_can.localized()
				} else {
					val success = model.enterPin(pin)
					if (success) {
						msgController.showMessage(MR.strings.pinmanage_message_pin_recovered.localized()) {
							stage.showChangeFlow { _, old, new -> changePin(old, new) }
						}
					} else {
						when (model.getPinStatus()) {
							PinStatus.Blocked -> {
								msgController.showMessage(MR.strings.pinmanage_message_pin_blocked.localized()) {
									stage.showPukFlow { _, puk -> unblockPin(puk) }
								}
							}

							else -> {
								msgController.showMessage(MR.strings.pinmanage_message_pin_recovery_failed.localized()) {
									stage.showCanPinFlow { view, retryCan, retryPin ->
										suspendRecovery(
											view,
											retryCan,
											retryPin,
										)
									}
								}
							}
						}
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("${MR.strings.pinmanage_common_error_title.localized()}: ${e.message}") {}
			}
		}
	}

	private fun unblockPin(puk: String) {
		bgTaskScope.launch {
			try {
				val model = checkNotNull(model)

				if (model.enterPuk(puk)) {
					msgController.showMessage(MR.strings.pinmanage_message_pin_unblocked.localized()) {
						stage.showChangeFlow { _, old, new -> changePin(old, new) }
					}
				} else {
					msgController.showMessage(MR.strings.pinmanage_message_wrong_puk.localized()) {
						stage.showPukFlow { _, retryPuk -> unblockPin(retryPuk) }
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("${MR.strings.pinmanage_common_error_title.localized()}: ${e.message}") {}
			}
		}
	}
}
