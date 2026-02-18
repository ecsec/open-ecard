package org.openecard.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.openecard.demo.core.NavigationWrapper
import org.openecard.demo.ui.theme.AppTheme
import org.openecard.sc.iface.TerminalFactory

@Suppress("ktlint:standard:function-naming")
@Composable
fun App(nfcTerminalFactory: TerminalFactory? = null) {
	AppTheme {
		NavigationWrapper(nfcTerminalFactory)
	}
}

data class AppBarState(
	val title: String? = null,
	val includeTopBar: Boolean = true,
	val canNavigateUp: Boolean = false,
	val navigateUp: () -> Unit = {},
	val settingsEnabled: Boolean = false,
	val navigateToDefaults: () -> Unit = {},
	val navigateToConfig: () -> Unit = {},
)

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
	state: AppBarState,
	modifier: Modifier = Modifier,
) {
	if (state.includeTopBar) {
		var menuExpanded by remember { mutableStateOf(false) }

		TopAppBar(
			title = {
				if (state.title != null) {
					Text(state.title)
				}
			},
			colors =
				TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.secondaryContainer,
					scrolledContainerColor = Color.Unspecified,
					navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
					titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
					actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
				),
			modifier = modifier,
			navigationIcon = {
				if (state.canNavigateUp) {
					IconButton(onClick = state.navigateUp) {
						Icon(
							imageVector = Icons.AutoMirrored.Filled.ArrowBack,
							contentDescription = null,
						)
					}
				}
			},
			actions = {
				if (state.settingsEnabled) {
					Box {
						IconButton(onClick = { menuExpanded = true }) {
							Icon(
								imageVector = Icons.Default.Settings,
								contentDescription = null,
							)
						}

						DropdownMenu(
							expanded = menuExpanded,
							onDismissRequest = { menuExpanded = false },
						) {
							DropdownMenuItem(
								text = { Text("Defaults") },
								onClick = {
									menuExpanded = false
									state.navigateToDefaults()
								},
							)
							DropdownMenuItem(
								text = { Text("Dev Options") },
								onClick = {
									menuExpanded = false
									state.navigateToConfig()
								},
							)
						}
					}
				}
			},
		)
	}
}
