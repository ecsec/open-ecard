package org.openecard.demo.viewmodel

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.demo.PinOperationResult
import org.openecard.demo.PinStatus
import org.openecard.demo.data.SalStackFactory
import org.openecard.sal.iface.dids.PinDid
import org.openecard.sal.sc.SmartcardApplication
import org.openecard.sc.iface.TerminalFactory

private val logger = KotlinLogging.logger { }

class PinChangeViewModel(
	private val terminalFactory: TerminalFactory?,
) : PinMgmtViewModel() {
	private val _pinChangeState = MutableStateFlow(PinChangeUiState())
	val pinChangeState = _pinChangeState.asStateFlow()

	var pin: PinDid? = null

	fun onOldPinChanged(value: String) {
		_pinChangeState.update {
			it.copy(
				oldPin = value,
				isSubmitEnabled = value.isNotBlank() && it.newPin.isNotBlank() && it.repeatPin.isNotBlank(),
			)
		}
	}

	fun onNewPinChanged(value: String) {
		_pinChangeState.update {
			it.copy(
				newPin = value,
				isSubmitEnabled = it.newPin.isNotBlank() && value.isNotBlank() && it.repeatPin.isNotBlank(),
			)
		}
	}

	fun onRepeatPinChanged(value: String) {
		_pinChangeState.update {
			it.copy(
				repeatPin = value,
				isSubmitEnabled = it.newPin.isNotBlank() && it.repeatPin.isNotBlank() && value.isNotBlank(),
			)
		}
	}

	fun validatePin(): String? {
		val state =
			_pinChangeState.value
		if (state.oldPin.length !in 5..6 || state.newPin.length !in 5..6 || state.repeatPin.length !in 5..6) {
			return "PIN must be 5 to 6 digits long."
		}
		if (state.newPin != state.repeatPin) {
			return "New PINs do not match."
		}
		return null
	}

	suspend fun changePin(
		nfcDetected: () -> Unit,
		oldPin: String,
		newPin: String,
	): PinOperationResult =
		try {
			if (pinOps == null) {
				pinOps = terminalFactory?.let { SalStackFactory.createPinSession(it) }
			}

			val ops = pinOps
			if (ops != null) {
				ops.connectCard(this, nfcDetected)

				when (val status = ops.getPinStatus(this.pacePin)) {
					PinStatus.OK -> {
						val success = ops.changePin(this, oldPin, newPin)
						if (success) {
							PinOperationResult(PinStatus.OK)
						} else {
							PinOperationResult(PinStatus.WrongPIN)
						}
					}

					PinStatus.Retry -> {
						val success = ops.changePin(this, oldPin, newPin)
						if (success) {
							PinOperationResult(PinStatus.OK)
						} else {
							PinOperationResult(PinStatus.Suspended)
						}
					}

					else -> {
						PinOperationResult(status)
					}
				}
			} else {
				logger.error { "Could not create session" }
				PinOperationResult(
					status = null,
					errorMessage = "Could not create session",
				)
			}
		} catch (e: Exception) {
			logger.error(e) { "PIN operation failed." }
			PinOperationResult(
				status = null,
				errorMessage = "PIN operation failed: ${e.message}",
			)
		} finally {
			pinOps?.shutdownStack()
			pinOps = null
		}

	fun setDefaults(
		oldPin: String,
		newPin: String,
	) {
		_pinChangeState.value =
			PinChangeUiState(
				oldPin = oldPin,
				newPin = newPin,
				repeatPin = newPin,
				isSubmitEnabled = oldPin.isNotBlank() && newPin.isNotBlank(),
			)
	}

	fun clear() {
		_pinChangeState.value = PinChangeUiState()
	}

	override fun setConnectionForOperation(mf: SmartcardApplication) {
		pin =
			mf.dids.filterIsInstance<PinDid>().find {
				it.name == NpaDefinitions.Apps.Mf.Dids.pin
			} ?: throw IllegalStateException("PIN DID not found")
	}
}

data class PinChangeUiState(
	val oldPin: String = "",
	val newPin: String = "",
	val repeatPin: String = "",
	val isSubmitEnabled: Boolean = false,
)
