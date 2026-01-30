package org.openecard.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.openecard.sc.iface.TerminalFactory

typealias TokenUrlProvider = suspend () -> String

@Suppress("ktlint:standard:function-naming")
@Composable
fun EacButton(
	text: String,
	nfcTerminalFactory: TerminalFactory? = null,
	scope: CoroutineScope,
	tokenUrlProvider: TokenUrlProvider,
	onClick: () -> Unit,
	result: (r: String?) -> Unit,
) {
	Button(onClick = {
		onClick()
		scope.launch {
			CoroutineScope(Dispatchers.IO).launch {
				result(
					doEAC(nfcTerminalFactory, tokenUrlProvider()),
				)
			}
		}
	}) {
		Text(text)
	}
}

@Suppress("ktlint:standard:function-naming")
@Composable
@Preview
fun App(nfcTerminalFactory: TerminalFactory? = null) {
	MaterialTheme {
		var status: String? by remember { mutableStateOf(null) }
		var result: String? by remember { mutableStateOf(null) }
		val scope = rememberCoroutineScope()
		val uriHandler = LocalUriHandler.current

		Column(
			modifier =
				Modifier
					.background(MaterialTheme.colorScheme.primaryContainer)
					.safeContentPadding()
					.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			EacButton(
				"EAC - SkidStaging",
				nfcTerminalFactory,
				scope,
				{
					SkidServer.forStageSystem().loadTcTokenUrl()
				},
				{
					status = "Bring card"
					result = null
				},
				{ result = it },
			)
			EacButton(
				"EAC - SkidProd",
				nfcTerminalFactory,
				scope,
				{
					SkidServer.forProdSystem().loadTcTokenUrl()
				},
				{
					status = "Bring card"
					result = null
				},
				{ result = it },
			)
			EacButton(
				"EAC - Governikus",
				nfcTerminalFactory,
				scope,
				{
					GovernikusTestServer().loadTcTokenUrl()
				},
				{
					status = "Bring card"
					result = null
				},
				{ result = it },
			)
			Column(
				modifier = Modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text("Status: $status")
			}

			Column(
				modifier = Modifier.fillMaxWidth(),
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Text("Result-URL: $result")
			}

			Button(
				enabled = result != null,
				onClick = {
					result?.let {
						uriHandler.openUri(it)
					}
				},
			) {
				Text("Open Result-URL")
			}
		}
	}
}
