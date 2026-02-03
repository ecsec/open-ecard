package org.openecard.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.resources.StringResource
import org.openecard.demo.core.NavigationWrapper
import org.openecard.demo.viewmodel.RootViewModel
import org.openecard.sc.iface.TerminalFactory

typealias TokenUrlProvider = suspend () -> String


// @Preview
@Suppress("ktlint:standard:function-naming")
@Composable
fun App(nfcTerminalFactory: TerminalFactory? = null, tokenUrlProvider: TokenUrlProvider? = null) {
	MaterialTheme {

		NavigationWrapper(nfcTerminalFactory, tokenUrlProvider)
	}
//	}
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


@Composable
fun RootScaffold(
	viewModel: RootViewModel = viewModel(),
	content: @Composable () -> Unit
) {
	val appBarState = viewModel.appBar

	Scaffold(
		topBar = { AppBar(appBarState) }
	) { padding ->
		Box(Modifier.padding(padding)) {
			content()
		}
	}
}

@Composable
fun DemoAppScaffold(
	title: String?,
	canNavigateUp: Boolean = false,
	navigateUp: () -> Unit = {},
	includeTopBar: Boolean = true,
	rootViewModel: RootViewModel,
	content: @Composable () -> Unit
) {
	LaunchedEffect(title, canNavigateUp, includeTopBar) {
		rootViewModel.update(
			title = title,
			canNavigateUp = canNavigateUp,
			includeTopBar = includeTopBar,
			navigateUp = navigateUp
		)
	}

	content()
}
