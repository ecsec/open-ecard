package org.openecard.demo.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import org.openecard.demo.AppBarState

class RootViewModel : ViewModel() {

	var appBar by mutableStateOf(
		AppBarState()
	)
		private set

	fun update(
		includeTopBar: Boolean = true,
		title: String? = null,
		canNavigateUp: Boolean = false,
		navigateUp: () -> Unit = {},
	) {
		val state = AppBarState(
			includeTopBar = includeTopBar,
			title = title,
			canNavigateUp = canNavigateUp,
			navigateUp = navigateUp
		)
		appBar = state
	}
}
