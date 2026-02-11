package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.demo.data.ConnectNpaPin
import org.openecard.demo.PinStatus
import org.openecard.demo.domain.PinOperations
import org.openecard.sc.iface.TerminalFactory

private val logger = KotlinLogging.logger { }

class PukEntryViewModel(
	private val terminalFactory: TerminalFactory?
) : ViewModel() {
	private val _pukUiState = MutableStateFlow(PukUiState())
	val pukUiState = _pukUiState.asStateFlow()

	fun onPukChanged(value: String) {
		_pukUiState.update {
			it.copy(puk = value, isSubmitEnabled = value.isNotBlank())
		}
	}

	fun validatePuk(): String? {
		val state = _pukUiState.value

		if (state.puk.length != 10) {
			return "PUK must be 10 digits long."
		}
		return null
	}

	suspend fun unblockPin(
		nfcDetected: () -> Unit,
		puk: String,
	): PinStatus {
		var model: PinOperations? = null

		return try {
			model = terminalFactory?.let { ConnectNpaPin.createPinModel(it, nfcDetected) }

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
		} finally {
			model?.shutdownStack()
		}
	}

	fun clear() {
		_pukUiState.value = PukUiState()
	}
}

data class PukUiState(
	val puk: String = "",
	val isSubmitEnabled: Boolean = false,
	val errorMessage: String? = null
)

