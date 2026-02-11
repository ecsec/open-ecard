package org.openecard.demo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState
import org.openecard.demo.GovernikusTestServer
import org.openecard.demo.SkidServer

private val logger = KotlinLogging.logger { }

@Suppress("ktlint:standard:function-naming")
@Composable
fun StartScreen(
	navigateToPin: () -> Unit,
	navigateToEac: (tokenUrl: String) -> Unit,
	navigateToEgk: () -> Unit,
) {
	val scope = rememberCoroutineScope()

	var showDialog by rememberSaveable { mutableStateOf(false) }
	var dialogMessage by rememberSaveable { mutableStateOf("") }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Open eCard",
				),
			)
		},
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
			val eacErrorText = "Error occurred while loading token URL."

			EacButton(
				"EAC - SkidStaging",
				onClick =
					{
						try {
							scope.launch {
								val url = SkidServer.forStageSystem().loadTcTokenUrl()
								navigateToEac(url)
							}
						} catch (e: Exception) {
							logger.error(e) { eacErrorText }
							dialogMessage = eacErrorText
							showDialog = true
						}
					},
			)
			EacButton(
				"EAC - SkidProd",
				onClick =
					{
						try {
							scope.launch {
								val url = SkidServer.forProdSystem().loadTcTokenUrl()
								navigateToEac(url)
							}
						} catch (e: Exception) {
							logger.error(e) { eacErrorText }
							dialogMessage = eacErrorText
							showDialog = true
						}
					},
			)
			EacButton(
				"EAC - Governikus",
				onClick =
					{
						try {
							scope.launch {
								val url = GovernikusTestServer().loadTcTokenUrl()
								navigateToEac(url)
							}
						} catch (e: Exception) {
							logger.error(e) { eacErrorText }
							dialogMessage = eacErrorText
							showDialog = true
						}
					},
			)
			Button(onClick = {
				try {
					navigateToPin()
				} catch (e: Exception) {
					logger.error(e) { "Error" }
					dialogMessage = "Some error occurred."
					showDialog = true
				}
			}) {
				Text("Change PIN")
			}

			Button(onClick = {
				try {
					navigateToEgk()
				} catch (e: Exception) {
					logger.error(e) { "Error" }
					dialogMessage = "Some error occurred."
					showDialog = true
				}
			}) {
				Text("eGK")
			}

			if (showDialog) {
				AlertDialog(
					onDismissRequest = { showDialog = false },
					title = { Text("Error") },
					text = { Text(dialogMessage) },
					confirmButton = {
						TextButton(onClick = { showDialog = false }) {
							Text("OK")
						}
					},
				)
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
