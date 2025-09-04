package org.openecard.richclient.pinmanagement.controllers

import javafx.application.Platform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.bundled.NpaCif
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.pinmanagement.model.NpaPacePinModel
import org.openecard.richclient.pinmanagement.model.PinStatus
import org.openecard.richclient.pinmanagement.ui.NpaPacePinView
import org.openecard.richclient.pinmanagement.ui.PinManagementUI
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sal.sc.SmartcardSal
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.apdu.command.SecurityCommandFailure
import org.openecard.sc.iface.Terminals
import org.openecard.sc.iface.withContext
import org.openecard.sc.pace.PaceFeatureSoftwareFactory
import org.openecard.sc.pcsc.PcscTerminalFactory

class NpaPacePinController(
	private val terminal: TerminalInfo,
	private val view: NpaPacePinView,
) : PinManagementUI {
	override fun show() {
		CoroutineScope(Dispatchers.IO).launch {
			val terminals = PcscTerminalFactory.instance.load()
			try {
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))
					val status = model.getPinStatus()
					Platform.runLater {
						when (status) {
							PinStatus.OK -> view.showChangeFlow { old, new -> changePin(old, new) }
							PinStatus.Suspended -> view.showCanAndPinFlow { can, pin -> suspendRecovery(can, pin) }
							PinStatus.Blocked -> view.showPukFlow { puk -> unblockPin(puk) }
							PinStatus.Unknown -> view.showMessage("Unable to determine PIN status.") {}
						}
					}
				}
			} catch (e: Exception) {
				Platform.runLater { view.showMessage("Error: ${e.message}") {} }
			}
		}
	}

	override fun abortProcess() {
		view.showMessage("PIN process aborted.") {}
	}

	private fun changePin(
		old: String,
		new: String,
	) {
		CoroutineScope(Dispatchers.IO).launch {
			val terminals = PcscTerminalFactory.instance.load()
			try {
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))
					val success = model.changePin(old, new)
					val retries = (model.pacePin.passwordStatus() as? SecurityCommandFailure)?.retries

					Platform.runLater {
						if (success) {
							view.showMessage("PIN changed successfully.") {
								view.showChangeFlow { o, n -> changePin(o, n) }
							}
						} else {
							when (retries) {
								2 ->
									view.showMessage("PIN incorrect. 2 retries left.") {
										view.showChangeFlow { o, n -> changePin(o, n) }
									}

								1 ->
									view.showMessage("PIN suspended. Please enter CAN.") {
// 										view.showCanFlow { can -> suspendRecovery(can) }
										view.showCanAndPinFlow { can, pin -> suspendRecovery(can, pin) }
									}

								0 ->
									view.showMessage("PIN blocked. Please enter PUK.") {
										view.showPukFlow { puk -> unblockPin(puk) }
									}

								else -> view.showMessage("PIN change failed.") {}
							}
						}
					}
				}
			} catch (e: Exception) {
				Platform.runLater { view.showMessage("Error: ${e.message}") {} }
			}
		}
	}

	private fun suspendRecovery(
		can: String,
		pin: String,
	) {
		CoroutineScope(Dispatchers.IO).launch {
			val terminals = PcscTerminalFactory.instance.load()
			try {
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))

					if (!model.enterCan(can)) {
						Platform.runLater {
							view.showMessage("Wrong CAN. Please try again.") {
								view.showCanAndPinFlow { retryCan, retryPin -> suspendRecovery(retryCan, retryPin) }
							}
						}
						return@withContext
					}

					val success = model.enterPin(pin)
					val retries = (model.pacePin.passwordStatus() as? SecurityCommandFailure)?.retries

					Platform.runLater {
						if (success) {
							view.showMessage("PIN recovered successfully.") {
								view.showChangeFlow { old, new -> changePin(old, new) }
							}
						} else if (retries == 0) {
							view.showMessage("PIN blocked. Please enter PUK.") {
								view.showPukFlow { puk -> unblockPin(puk) }
							}
						} else {
							view.showMessage("PIN recovery failed. Please try again.") {
								view.showCanAndPinFlow { retryCan, retryPin -> suspendRecovery(retryCan, retryPin) }
							}
						}
					}
				}
			} catch (e: Exception) {
				Platform.runLater {
					view.showMessage("Error: ${e.message}") {}
				}
			}
		}
	}

	private fun unblockPin(puk: String) {
		CoroutineScope(Dispatchers.IO).launch {
			val terminals = PcscTerminalFactory.instance.load()
			try {
				terminals.withContext { ctx ->
					val model = NpaPacePinModel(connectToMf(ctx))
					if (model.enterPuk(puk)) {
						Platform.runLater {
							view.showMessage("PIN unblocked successfully.") {
								view.showChangeFlow { old, new -> changePin(old, new) }
							}
						}
					} else {
						Platform.runLater {
							view.showMessage("Wrong PUK. Please try again.") {
								view.showPukFlow { retryPuk -> unblockPin(retryPuk) }
							}
						}
					}
				}
			} catch (e: Exception) {
				Platform.runLater { view.showMessage("Error: ${e.message}") {} }
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
