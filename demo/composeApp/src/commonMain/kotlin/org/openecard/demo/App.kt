package org.openecard.demo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType.Companion.Uri
import demo.composeapp.generated.resources.Res
import demo.composeapp.generated.resources.compose_multiplatform
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.openecard.sc.iface.TerminalFactory

@Composable
@Preview
fun App(nfcTerminalFactory: TerminalFactory? = null) {
	MaterialTheme {
		var result by remember { mutableStateOf("Nothing yet.") }
		val scope = rememberCoroutineScope()
		val uriHandler = LocalUriHandler.current
		// SideEffect {
		// 	scope.launch {
		// 		doNFC(nfcTerminalFactory)
		// 	}
		// }
		Column(
			modifier =
				Modifier
					.background(MaterialTheme.colorScheme.primaryContainer)
					.safeContentPadding()
					.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Button(onClick = {
				result = "Working"
				scope.launch {
					CoroutineScope(Dispatchers.IO).launch {
						result = doNFC(nfcTerminalFactory) ?: "erorr"
					}
				}
			}) {
				Text("Click me!")
			}
			AnimatedVisibility(true) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally,
				) {
					Text("Compose: $result")
				}
			}
			Button(onClick = {
				uriHandler.openUri(result)
			}) {
				Text("Open")
			}
		}
	}
}
