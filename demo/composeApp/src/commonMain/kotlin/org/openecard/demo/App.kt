package org.openecard.demo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.openecard.demo.core.Navigation
import org.openecard.sc.iface.TerminalFactory

typealias TokenUrlProvider = suspend () -> String


// @Preview
@Suppress("ktlint:standard:function-naming")
@Composable
fun App(nfcTerminalFactory: TerminalFactory? = null) {
	MaterialTheme {
		Scaffold(
			topBar = { AppBar(AppBarState("Open eCard", true, true, {})) },
		) { innerPadding ->
			Navigation(nfcTerminalFactory)
// 			NfcScreen()
		}
	}
}

data class AppBarState(
	val title: String? = null,
	val includeTopBar: Boolean = true,
	val canNavigateUp: Boolean = false,
	val navigateUp: () -> Unit = {},
)

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
	state: AppBarState,
	modifier: Modifier = Modifier,
) {
	if (state.includeTopBar) {
		TopAppBar(
			title = {
				if (state.title != null) {
					Text(state.title)
				}
			},
			colors =
				TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.primaryContainer,
					scrolledContainerColor = Color.Unspecified,
					navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
					actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
				),
			modifier = modifier,
			navigationIcon = {
				if (state.canNavigateUp) {
					IconButton(onClick = state.navigateUp) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = "",
						)
					}
				}
			},
		)
	}
}
