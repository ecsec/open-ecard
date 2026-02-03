package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.openecard.demo.PinStatus

class PinMgmtViewModel : ViewModel() {
	private val _pinMgmtUiState = MutableStateFlow(PinMgmtUiState())
	val pinMgmtUiState: StateFlow<PinMgmtUiState> = _pinMgmtUiState
}

data class PinMgmtUiState(
	val oldPin: String = "",
	val newPin: String = "",
	val pinStatus: PinStatus = PinStatus.Unknown,
	val isLoading: Boolean = false,
	val buttonEnabled: Boolean = false,
	val isError: Boolean = false
)
