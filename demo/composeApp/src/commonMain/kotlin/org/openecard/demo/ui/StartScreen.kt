package org.openecard.demo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.launch
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState
import org.openecard.demo.GovernikusTestServer
import org.openecard.demo.SkidServer

private val logger = KotlinLogging.logger { }

enum class DetailType {
	EAC,
	PIN,
	EGK,
}

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun StartScreen(
	navigateToPin: () -> Unit,
	navigateToChatSelection: (String) -> Unit,
	navigateToEgk: () -> Unit,
	navigateUp: () -> Unit,
	navigateToSettings: () -> Unit,
) {
	var selectedDetail by remember { mutableStateOf<DetailType?>(null) }

	val navigator = rememberListDetailPaneScaffoldNavigator<Nothing>()
	val scope = rememberCoroutineScope()

	ListDetailPaneScaffold(
		modifier =
			Modifier
				.fillMaxSize(),
		directive = navigator.scaffoldDirective,
		value = navigator.scaffoldValue,
		listPane = {
			StartListPane(
				navigateToEac = {
					selectedDetail = DetailType.EAC
					scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
				},
				navigateToPin = {
					selectedDetail = DetailType.PIN
					scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
				},
				navigateToEgk = {
					selectedDetail = DetailType.EGK
					scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail) }
				},
				navigateToSettings = {
					navigateToSettings()
				},
			)
		},
		detailPane = {
			when (selectedDetail) {
				DetailType.EAC -> {
					EacDetailPane(navigateToChatSelection, navigateUp)
				}

				DetailType.PIN -> {
					PinDetailPane(navigateToPin, navigateUp)
				}

				DetailType.EGK -> {
					EgkDetailPane(navigateToEgk, navigateUp)
				}

				null -> {}
			}
		},
	)
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun StartListPane(
	navigateToEac: () -> Unit,
	navigateToPin: () -> Unit,
	navigateToEgk: () -> Unit,
	navigateToSettings: () -> Unit,
) {
	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Open eCard",
					canNavigateUp = false,
					settingsEnabled = true,
					navigateToSettings = navigateToSettings,
				),
			)
		},
	) {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(24.dp),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			FeatureCard("EAC with nPA", onClick = navigateToEac)

			Spacer(Modifier.height(16.dp))

			FeatureCard("PIN Management", onClick = navigateToPin)

			Spacer(Modifier.height(16.dp))
			
			FeatureCard("PACE with eGK", onClick = navigateToEgk)
		}
	}
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun EacDetailPane(
	navigateToChatSelection: (String) -> Unit,
	navigateUp: () -> Unit,
) {
	val scope = rememberCoroutineScope()

	var showDialog by rememberSaveable { mutableStateOf(false) }
	var dialogMessage by rememberSaveable { mutableStateOf("") }

	val eacErrorText = "Error occurred while loading token URL."

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "EAC with nPA",
					canNavigateUp = true,
					navigateUp = navigateUp,
				),
			)
		},
	) {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			Text(
				"Select a service",
				style = MaterialTheme.typography.headlineSmall,
			)

			Spacer(Modifier.height(32.dp))

			FeatureCard("EAC – SkidStaging") {
				scope.launch {
					try {
						val url = SkidServer.forStageSystem().loadTcTokenUrl()
						navigateToChatSelection(url)
					} catch (e: Exception) {
						logger.error(e) { eacErrorText }
						dialogMessage = eacErrorText
						showDialog = true
					}
				}
			}

			Spacer(Modifier.height(16.dp))

			FeatureCard("EAC – SkidProd") {
				scope.launch {
					try {
						val url = SkidServer.forProdSystem().loadTcTokenUrl()
						navigateToChatSelection(url)
					} catch (e: Exception) {
						logger.error(e) { eacErrorText }
						dialogMessage = eacErrorText
						showDialog = true
					}
				}
			}

			Spacer(Modifier.height(16.dp))

			FeatureCard("EAC – Governikus") {
				scope.launch {
					try {
						val url = GovernikusTestServer().loadTcTokenUrl()
						navigateToChatSelection(url)
					} catch (e: Exception) {
						logger.error(e) { eacErrorText }
						dialogMessage = eacErrorText
						showDialog = true
					}
				}
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
fun PinDetailPane(
	navigateToPin: () -> Unit,
	navigateUp: () -> Unit,
) {
	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "PIN Management",
					canNavigateUp = true,
					navigateUp = navigateUp,
				),
			)
		},
	) {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			Text("Select a card type", style = MaterialTheme.typography.headlineSmall)

			Spacer(Modifier.height(32.dp))

			FeatureCard("nPA", onClick = navigateToPin)
		}
	}
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun EgkDetailPane(
	navigateToEgk: () -> Unit,
	navigateUp: () -> Unit,
) {
	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "PACE with eGK",
					canNavigateUp = true,
					navigateUp = navigateUp,
				),
			)
		},
	) {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(24.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			Text("Select an operation", style = MaterialTheme.typography.headlineSmall)

			Spacer(Modifier.height(32.dp))

			FeatureCard("Read personal data from data set", onClick = navigateToEgk)
		}
	}
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun FeatureCard(
	title: String,
	onClick: () -> Unit,
) {
	Card(
		modifier =
			Modifier
				.fillMaxWidth()
				.heightIn(min = 64.dp)
				.clickable(onClick = onClick),
		shape = MaterialTheme.shapes.medium,
		colors =
			CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant,
			),
		elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
	) {
		Box(
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(vertical = 20.dp, horizontal = 16.dp),
			contentAlignment = Alignment.CenterStart,
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
			)
		}
	}
}
