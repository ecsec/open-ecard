package org.openecard.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.launch
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState
import org.openecard.demo.GovernikusTestServer
import org.openecard.demo.SkidServer
import org.openecard.demo.TokenUrlProvider
import org.openecard.sc.iface.TerminalFactory


@Suppress("ktlint:standard:function-naming")
@Composable
fun StartScreen(
	navigateToPin: () -> Unit,
	navigateToEac: (tokenUrl: String) -> Unit,
	navigateToEgk: () -> Unit,
	nfcTerminalFactory: TerminalFactory?,
	tokenUrlProvider: TokenUrlProvider?,
) {

	var status: String? by remember { mutableStateOf(null) }
	var result: String? by remember { mutableStateOf(null) }
	val scope = rememberCoroutineScope()
	val uriHandler = LocalUriHandler.current

	var pinChanged by remember { mutableStateOf(false) }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Open eCard"
				)
			)
		}
	) {
		Column(
			modifier =
				Modifier
					.background(MaterialTheme.colorScheme.primaryContainer)
					.safeContentPadding()
					.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.SpaceEvenly,
		) {

			EacButton(
				"EAC - SkidStaging",
				onClick =
					{
						scope.launch {
							val url = SkidServer.forStageSystem().loadTcTokenUrl()
							navigateToEac(url)
						}
					},
			)
			EacButton(
				"EAC - SkidProd",

				onClick =
					{
						scope.launch {
							val url = SkidServer.forProdSystem().loadTcTokenUrl()
							navigateToEac(url)
						}
					},
			)
			EacButton(
				"EAC - Governikus",
				onClick =
					{
						scope.launch {
							val url = GovernikusTestServer().loadTcTokenUrl()
							navigateToEac(url)
						}
					},
			)

			Button(onClick = {
				navigateToPin()
			}) {
				Text("Change PIN")
			}

			Button(onClick = {
				navigateToEgk()
			}) {
				Text("eGK")
			}
		}
	}
}

	@Suppress("ktlint:standard:function-naming")
	@Composable
	fun EacButton(
		text: String,
		onClick: () -> Unit,
	) {
		Button(
			onClick = {
				onClick()
			},
		)
		{
			Text(text)
		}
	}

