package org.openecard.demo.viewmodel

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DefaultsState(
	val npaPin: String = "",
	val npaNewPin: String = "",
	val npaCan: String = "",
	val npaPuk: String = "",
	val egkCan: String = "",
) {
	fun store() {
		val settings = Settings()
		settings.putString("npaPin", npaPin)
		settings.putString("npaNewPin", npaNewPin)
		settings.putString("npaCan", npaCan)
		settings.putString("npaPuk", npaPuk)
		settings.putString("egkCan", egkCan)
	}

	companion object {
		fun load(): DefaultsState {
			val settings = Settings()
			return DefaultsState(
				settings.getString("npaPin", ""),
				settings.getString("npaNewPin", ""),
				settings.getString("npaCan", ""),
				settings.getString("npaPuk", ""),
				settings.getString("egkCan", ""),
			)
		}
	}
}

class DefaultsViewModel : ViewModel() {
	private val _state = MutableStateFlow(DefaultsState.load())
	val state = _state.asStateFlow()

	fun update(block: (DefaultsState) -> DefaultsState) {
		_state.update {
			block(it).apply { store() }
		}
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
