package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.demo.PinStatus
import org.openecard.demo.data.ConnectNpaPin
import org.openecard.demo.domain.PinOperations
import org.openecard.sc.iface.TerminalFactory

private val logger = KotlinLogging.logger { }

class PinChangeViewModel(
	private val terminalFactory: TerminalFactory?,
) : ViewModel() {
	private val _pinChangeState = MutableStateFlow(PinChangeUiState())
	val pinChangeState = _pinChangeState.asStateFlow()

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
	): PinStatus {
		var model: PinOperations? = null

		return try {
			model = terminalFactory?.let { ConnectNpaPin.createPinModel(it, nfcDetected) }

			if (model != null) {
				when (val status = model.getPinStatus()) {
					PinStatus.OK -> {
						val success = model.changePin(oldPin, newPin)

						if (success) {
							PinStatus.OK
						} else {
							PinStatus.WrongPIN
						}
					}

					PinStatus.Retry -> {
						val success = model.changePin(oldPin, newPin)

						if (success) {
							PinStatus.OK
						} else {
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
		} finally {
			model?.shutdownStack()
		}
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
}

data class PinChangeUiState(
	val oldPin: String = "",
	val newPin: String = "",
	val repeatPin: String = "",
	val isSubmitEnabled: Boolean = false,
)
