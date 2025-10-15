package org.openecard.richclient.pinmanagement.npa

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.richclient.pinmanagement.PinManagementStage
import org.openecard.richclient.pinmanagement.PinManagementUI
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.pinmanagement.common.MessageController
import org.openecard.richclient.pinmanagement.npa.CanEntryViewController.Companion.showCanPinFlow
import org.openecard.richclient.pinmanagement.npa.PinChangeViewController.Companion.showChangeFlow
import org.openecard.richclient.pinmanagement.npa.PukEntryViewController.Companion.showPukFlow
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.iface.Terminals
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.PcscTerminalFactory

class NpaPinController(
	private val terminal: TerminalInfo,
	private val stage: PinManagementStage,
	private val bgTaskScope: CoroutineScope,
) : PinManagementUI {
	private val msgController = MessageController(stage, bgTaskScope)

	override fun show() {
		val terminals = PcscTerminalFactory.instance.load()
		try {
			terminals.withContext { ctx ->
				val model = NpaPacePinModel(connectToMf(ctx))
				// check pin status and decide which UI we need
				val status = model.getPinStatus()

				when (status) {
					PinStatus.OK -> stage.showChangeFlow { _, old, new -> changePin(old, new) }
					PinStatus.Suspended -> stage.showCanPinFlow { view, can, pin -> suspendRecovery(view, can, pin) }
					PinStatus.Blocked -> stage.showPukFlow { _, puk -> unblockPin(puk) }
					PinStatus.Unknown -> msgController.showMessage("Unable to determine PIN status.") {}
				}
			}
		} catch (e: Exception) {
			msgController.showMessage("Error: ${e.message}") {}
		}
	}

	override fun abortProcess() {
		// TODO: call this function, do cleanup here and close stage
		msgController.showMessage("PIN process aborted.") {}
	}

	private fun changePin(
		old: String,
		new: String,
	) {
		bgTaskScope.launch {
			try {
				val terminals = PcscTerminalFactory.instance.load()
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))
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
				val terminals = PcscTerminalFactory.instance.load()
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))

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
				}
			} catch (e: Exception) {
				msgController.showMessage("Error: ${e.message}") {}
			}
		}
	}

	private fun unblockPin(puk: String) {
		bgTaskScope.launch {
			try {
				val terminals = PcscTerminalFactory.instance.load()
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))
					if (model.enterPuk(puk)) {
						msgController.showMessage("PIN unblocked successfully.") {
							stage.showChangeFlow { _, old, new -> changePin(old, new) }
						}
					} else {
						msgController.showMessage("Wrong PUK. Please try again.") {
							stage.showPukFlow { _, retryPuk -> unblockPin(retryPuk) }
						}
					}
				}
			} catch (e: Exception) {
				msgController.showMessage("Error: ${e.message}") {}
			}
		}
	}

	private fun connectToMf(ctx: Terminals): SmartcardApplication {
		val sal =
			SmartcardSal(
				ctx,
				setOf(NpaCif),
				DirectCardRecognition(CompleteTree.calls.removeUnsupported(setOf(NpaDefinitions.cardType))),
				PaceFeatureSoftwareFactory(),
			)
		val session = sal.startSession()
		val connection = session.connect(terminal.terminalName)

		if (connection.deviceType != NpaCif.metadata.id) {
			throw IllegalStateException("Card is not an nPA")
		}

		return connection.applications.find { it.name == NpaDefinitions.Apps.Mf.name }
			?: throw IllegalStateException("MF application not found")
	}
}
