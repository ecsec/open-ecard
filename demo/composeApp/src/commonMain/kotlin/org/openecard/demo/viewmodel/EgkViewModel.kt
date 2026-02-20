package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.openecard.demo.data.Session
import org.openecard.demo.domain.EgkOperations
import org.openecard.sal.sc.SmartcardDeviceConnection
import org.openecard.sc.iface.TerminalFactory
import org.openecard.sc.iface.feature.PaceError

private val logger = KotlinLogging.logger { }

class EgkViewModel(
	private val terminalFactory: TerminalFactory?,
) : ViewModel() {
	private val _egkUiState = MutableStateFlow(EgkUiState())
	val egkUiState = _egkUiState.asStateFlow()

	var egkOps: EgkOperations? = null
	var connection: SmartcardDeviceConnection? = null

	fun onCanChanged(value: String) {
		_egkUiState.update {
			it.copy(
				can = value,
				isSubmitEnabled = value.isNotBlank(),
			)
		}
	}

	fun validateCan(): String? {
		val state = egkUiState.value

		if (state.can.length != 6) {
			return "CAN must be 6 digits long."
		}
		return null
	}

	suspend fun readEgk(
		nfcDetected: () -> Unit,
		can: String,
	): String? {
		return try {
			if (egkOps == null) {
				egkOps = terminalFactory?.let { Session.createEgkSession(it) }
			}
			val ops = egkOps

			if (ops != null) {
				val result = ops.doPace(this, nfcDetected, can)
				result ?: ops.readPersonalData(this)
			} else {
				logger.error { "Could not create session" }
				return null
			}
		} catch (e: Exception) {
			logger.error(e) { "PACE operation failed" }
			e.message
		} finally {
			egkOps?.session?.shutdownStack()
			egkOps = null
		}
	}

	fun setDefaults(can: String) {
		_egkUiState.value =
			EgkUiState(
				can = can,
				isSubmitEnabled = can.isNotBlank(),
			)
	}

	fun clear() {
		_egkUiState.value = EgkUiState()
	}
}

data class EgkUiState(
	val can: String = "",
	val isSubmitEnabled: Boolean = false,
)
