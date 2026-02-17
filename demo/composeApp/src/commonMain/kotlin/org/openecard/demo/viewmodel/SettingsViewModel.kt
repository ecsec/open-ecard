package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class SettingsState(
	val npaPin: String = "",
	val npaNewPin: String = "",
	val npaCan: String = "",
	val npaPuk: String = "",
	val egkCan: String = "",
)

class SettingsViewModel : ViewModel() {
	private val _state = MutableStateFlow(SettingsState())
	val state = _state.asStateFlow()

	fun update(block: (SettingsState) -> SettingsState) {
		_state.update(block)
	}

	fun validateInput(): Boolean {
		val state =
			_state.value
		return (state.npaPin.length !in 5..6 && state.npaPin.isNotBlank()) ||
			(state.npaNewPin.length !in 5..6 && state.npaNewPin.isNotBlank()) ||
			(state.npaCan.length != 6 && state.npaCan.isNotBlank()) ||
			(state.npaPuk.length != 10 && state.npaPuk.isNotBlank()) ||
			(state.egkCan.length != 6 && state.egkCan.isNotBlank())
	}
}
