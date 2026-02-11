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

class CanEntryViewModel(
	private val terminalFactory: TerminalFactory?,
) : ViewModel() {
	private val _canPinUiState = MutableStateFlow(CanPinUiState())

	val canPinUiState = _canPinUiState.asStateFlow()

	fun onCanChanged(value: String) {
		_canPinUiState.update {
			it.copy(can = value, isSubmitEnabled = value.isNotBlank() && it.pin.isNotBlank())
		}
	}

	fun onPinChanged(value: String) {
		_canPinUiState.update {
			it.copy(pin = value, isSubmitEnabled = it.can.isNotBlank() && value.isNotBlank())
		}
	}

	fun validateCanPin(): String? {
		val state = _canPinUiState.value

		if (state.can.length != 6) {
			return "CAN must be 6 digits long."
		}
		if (state.pin.length !in 5..6) {
			return "PIN must be 5 to 6 digits long."
		}
		return null
	}

	suspend fun recoverWithCan(
		nfcDetected: () -> Unit,
		can: String,
		pin: String,
	): PinStatus {
		var model: PinOperations? = null

		return try {
			model = terminalFactory?.let { ConnectNpaPin.createPinModel(it, nfcDetected) }

			if (model != null) {
				when (val status = model.getPinStatus()) {
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
		} finally {
			model?.shutdownStack()
		}
	}

	fun setDefaults() {
		_canPinUiState.value =
			CanPinUiState(
				can = "123123",
				pin = "123123",
				isSubmitEnabled = true,
			)
	}

	fun clear() {
		_canPinUiState.value = CanPinUiState()
	}
}

data class CanPinUiState(
	val can: String = "",
	val pin: String = "",
	val isSubmitEnabled: Boolean = false,
)
