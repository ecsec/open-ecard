package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import org.openecard.demo.PinStatus
import org.openecard.demo.data.logger
import org.openecard.demo.model.ConnectNpaPin
import org.openecard.sc.iface.TerminalFactory

class PinMgmtViewModel(
	private val terminalFactory: TerminalFactory?
) : ViewModel() {
	suspend fun changePin(
		nfcDetected: () -> Unit,
		oldPin: String,
		newPin: String,
	): PinStatus {
		return try {
			val model = terminalFactory?.let { ConnectNpaPin.createPinModel(it, nfcDetected) }

			if (model != null) {
				val status = model.getPinStatus()

				when (status) {
					PinStatus.OK -> {

						val success = model.changePin(oldPin, newPin)

						if (success)
							PinStatus.OK
						else {
							PinStatus.WrongPIN
						}
					}

					PinStatus.Retry -> {
						val success = model.changePin(oldPin, newPin)

						if (success)
							PinStatus.OK
						else {
							PinStatus.Suspended
						}
					}

					else -> {
						status
					}
				}

			} else {
				logger.error { "Could not connect card." }
				return PinStatus.Unknown
			}
		} catch (e: Exception) {
			logger.error(e) { "PIN operation failed." }
			e.message
			PinStatus.Unknown
		}
	}

	suspend fun recoverWithCan(
		nfcDetected: () -> Unit,
		can: String,
		pin: String,
	): PinStatus {
		return try {
			val model = terminalFactory?.let { ConnectNpaPin.createPinModel(it, nfcDetected) }

			if (model != null) {
				val status = model.getPinStatus()

				when (status) {
					PinStatus.Suspended -> {
						if (!model.enterCan(can)) {
							PinStatus.WrongCAN
						} else if (model.enterPin(pin)) {
							PinStatus.OK
						} else {
							status
						}
					}

					else -> {
						status
					}
				}
			} else {
				logger.error { "Could not connect card." }
				return PinStatus.Unknown
			}
		} catch (e: Exception) {
			logger.error(e) { "PIN operation failed." }
			e.message
			PinStatus.Unknown
		}
	}

	suspend fun unblockPin(
		nfcDetected: () -> Unit,
		puk: String,
	): PinStatus {
		return try {
			val model = terminalFactory?.let { ConnectNpaPin.createPinModel(it, nfcDetected) }
			
			if (model != null) {
				val status = model.getPinStatus()

				when (status) {
					PinStatus.Blocked -> {
						if (model.enterPuk(puk)) {
							PinStatus.OK
						} else {
							PinStatus.WrongPUK
						}
					}

					else -> {
						status
					}
				}
			} else {
				logger.error { "Could not connect card." }
				return PinStatus.Unknown
			}
		} catch (e: Exception) {
			logger.error(e) { "PIN operation failed." }
			e.message
			PinStatus.Unknown
		}
	}
}
