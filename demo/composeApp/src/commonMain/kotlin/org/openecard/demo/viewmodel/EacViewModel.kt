package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import org.openecard.demo.domain.logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.demo.data.ConnectNpaEac
import org.openecard.sc.iface.TerminalFactory

class EacViewModel(
	private val terminalFactory: TerminalFactory?
) : ViewModel() {

	private val _eacUiState = MutableStateFlow(EacUiState())
	val eacUiState = _eacUiState.asStateFlow()

	fun onPinChanged(value: String) {
		_eacUiState.update {
			it.copy(
				pin = value,
				isSubmitEnabled = value.isNotBlank()
			)
		}
	}

	fun validatePin(): String? {
		val s = eacUiState.value

		if (s.pin.length !in 5..6) {
			return "PIN must be 5 to 6 digits long."
		}
		return null
	}

	suspend fun doEac(
		nfcDetected: () -> Unit,
		tokenUrl: String,
		pin: String
	): String? {
		return try {
			val model = terminalFactory?.let { ConnectNpaEac.createEacModel(it, nfcDetected) }

			if (model != null) {
				model.doEac(tokenUrl, pin)
			} else {
				logger.error { "Could not connect card." }
				return null
			}
		} catch (e: Exception) {
			logger.error(e) { "EAC operation failed." }
			e.message
		}
	}

	fun clear() {
		_eacUiState.value = EacUiState()
	}
}

data class EacUiState(
	val pin: String = "",
	val isSubmitEnabled: Boolean = false,
	val errorMessage: String? = null
)

