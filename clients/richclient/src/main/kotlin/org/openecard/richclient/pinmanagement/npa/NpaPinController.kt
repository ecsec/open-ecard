package org.openecard.richclient.pinmanagement.npa

import javafx.application.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.richclient.pinmanagement.PinManagementStage
import org.openecard.richclient.pinmanagement.PinManagementUI
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.pinmanagement.common.MessageController
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.iface.Terminals
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.PcscTerminalFactory

class NpaPinController(
	private val terminal: TerminalInfo,
	private val stage: PinManagementStage,
	private val bgTaskScope: CoroutineScope,
) : PinManagementUI {
	private val npaViews = NpaPacePinViews(stage)
	private val msgController = MessageController(stage.rootPane, bgTaskScope)

	override fun show() {
		CoroutineScope(Dispatchers.IO).launch {
			val terminals = PcscTerminalFactory.Companion.instance.load()
			try {
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))
					val status = model.getPinStatus()
					Platform.runLater {
						when (status) {
							PinStatus.OK -> npaViews.showChangeFlow { old, new -> changePin(old, new) }
							PinStatus.Suspended -> npaViews.showCanAndPinFlow { can, pin -> suspendRecovery(can, pin) }
							PinStatus.Blocked -> npaViews.showPukFlow { puk -> unblockPin(puk) }
							PinStatus.Unknown -> msgController.showMessage("Unable to determine PIN status.") {}
						}
					}
				}
			} catch (e: Exception) {
				Platform.runLater { msgController.showMessage("Error: ${e.message}") {} }
			}
		}
	}

	override fun abortProcess() {
		msgController.showMessage("PIN process aborted.") {}
	}

	private fun changePin(
		old: String,
		new: String,
	) {
		CoroutineScope(Dispatchers.IO).launch {
			val terminals = PcscTerminalFactory.Companion.instance.load()
			try {
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))
					val success = model.changePin(old, new)
					val retries = (model.pacePin.passwordStatus() as? SecurityCommandFailure)?.retries

					Platform.runLater {
						if (success) {
							msgController.showMessage("PIN changed successfully.") {
								npaViews.showChangeFlow { o, n -> changePin(o, n) }
							}
						} else {
							when (retries) {
								2 ->
									msgController.showMessage("PIN incorrect. 2 retries left.") {
										npaViews.showChangeFlow { o, n -> changePin(o, n) }
									}

								1 ->
									msgController.showMessage("PIN suspended. Please enter CAN.") {
// 										view.showCanFlow { can -> suspendRecovery(can) }
										npaViews.showCanAndPinFlow { can, pin -> suspendRecovery(can, pin) }
									}

								0 ->
									msgController.showMessage("PIN blocked. Please enter PUK.") {
										npaViews.showPukFlow { puk -> unblockPin(puk) }
									}

								else -> msgController.showMessage("PIN change failed.") {}
							}
						}
					}
				}
			} catch (e: Exception) {
				Platform.runLater { msgController.showMessage("Error: ${e.message}") {} }
			}
		}
	}

	private fun suspendRecovery(
		can: String,
		pin: String,
	) {
		CoroutineScope(Dispatchers.IO).launch {
			val terminals = PcscTerminalFactory.Companion.instance.load()
			try {
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))

					if (!model.enterCan(can)) {
						Platform.runLater {
							msgController.showMessage("Wrong CAN. Please try again.") {
								npaViews.showCanAndPinFlow { retryCan, retryPin -> suspendRecovery(retryCan, retryPin) }
							}
						}
					} else {
						val success = model.enterPin(pin)
						val retries = (model.pacePin.passwordStatus() as? SecurityCommandFailure)?.retries

						Platform.runLater {
							if (success) {
								msgController.showMessage("PIN recovered successfully.") {
									npaViews.showChangeFlow { old, new -> changePin(old, new) }
								}
							} else if (retries == 0) {
								msgController.showMessage("PIN blocked. Please enter PUK.") {
									npaViews.showPukFlow { puk -> unblockPin(puk) }
								}
							} else {
								msgController.showMessage("PIN recovery failed. Please try again.") {
									npaViews.showCanAndPinFlow { retryCan, retryPin -> suspendRecovery(retryCan, retryPin) }
								}
							}
						}
					}
				}
			} catch (e: Exception) {
				Platform.runLater {
					msgController.showMessage("Error: ${e.message}") {}
				}
			}
		}
	}

	private fun unblockPin(puk: String) {
		CoroutineScope(Dispatchers.IO).launch {
			val terminals = PcscTerminalFactory.Companion.instance.load()
			try {
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))
					if (model.enterPuk(puk)) {
						Platform.runLater {
							msgController.showMessage("PIN unblocked successfully.") {
								npaViews.showChangeFlow { old, new -> changePin(old, new) }
							}
						}
					} else {
						Platform.runLater {
							msgController.showMessage("Wrong PUK. Please try again.") {
								npaViews.showPukFlow { retryPuk -> unblockPin(retryPuk) }
							}
						}
					}
				}
			} catch (e: Exception) {
				Platform.runLater { msgController.showMessage("Error: ${e.message}") {} }
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
