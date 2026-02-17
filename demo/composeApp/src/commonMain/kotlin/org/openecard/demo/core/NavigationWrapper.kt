package org.openecard.demo.core

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.coroutines.launch
import org.openecard.demo.PinStatus
import org.openecard.demo.ui.CanEntryScreen
import org.openecard.demo.ui.EacChatSelectionScreen
import org.openecard.demo.ui.EacPinEntryScreen
import org.openecard.demo.ui.EgkCanEntryScreen
import org.openecard.demo.ui.NfcScreen
import org.openecard.demo.ui.PinChangeScreen
import org.openecard.demo.ui.PukEntryScreen
import org.openecard.demo.ui.ResultScreen
import org.openecard.demo.ui.SettingsScreen
import org.openecard.demo.ui.StartScreen
import org.openecard.demo.viewmodel.CanEntryViewModel
import org.openecard.demo.viewmodel.EacViewModel
import org.openecard.demo.viewmodel.EgkViewModel
import org.openecard.demo.viewmodel.PinChangeViewModel
import org.openecard.demo.viewmodel.PukEntryViewModel
import org.openecard.demo.viewmodel.SettingsViewModel
import org.openecard.sc.iface.TerminalFactory

@OptIn(ExperimentalComposeUiApi::class, ExperimentalUnsignedTypes::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun NavigationWrapper(nfcTerminalFactory: TerminalFactory?) {
	val navController = rememberNavController()
	val nfcDetected = rememberSaveable { mutableStateOf(false) }
	val pinChangeViewModel = remember { PinChangeViewModel(nfcTerminalFactory) }
	val canEntryViewModel = remember { CanEntryViewModel(nfcTerminalFactory) }
	val pukEntryViewModel = remember { PukEntryViewModel(nfcTerminalFactory) }
	val eacViewModel = remember { EacViewModel(nfcTerminalFactory) }
	val egkViewModel = remember { EgkViewModel(nfcTerminalFactory) }
	val settingsViewModel = remember { SettingsViewModel() }

	val scope = rememberCoroutineScope()
	val defaultState by settingsViewModel.state.collectAsState()

	var dialogMessage by rememberSaveable { mutableStateOf("") }
	var showDialog by rememberSaveable { mutableStateOf(false) }

	fun setDefaults() {
		pinChangeViewModel.setDefaults(defaultState.npaPin, defaultState.npaNewPin)
		canEntryViewModel.setDefaults(defaultState.npaCan, defaultState.npaPin)
		pukEntryViewModel.setDefaults(defaultState.npaPuk)
		eacViewModel.setDefaults(defaultState.npaPin)
		egkViewModel.setDefaults(defaultState.egkCan)
	}

	NavHost(navController = navController, startDestination = Start) {
		composable<Start> {
			setDefaults()

			StartScreen(
				navigateToPin = {
					navController.navigate(PIN)
				},
				navigateToChatSelection = { url ->
					scope.launch {
						try {
							val ok = eacViewModel.setChatItems(url)
							if (ok) {
								navController.navigate(EacChat)
							} else {
								dialogMessage = "Could not load chat items"
								showDialog = true
							}
						} catch (e: Exception) {
							dialogMessage = "${e.message}"
							showDialog = true
						}
					}
				},
				navigateToEgk = {
					navController.navigate(EGK)
				},
				navigateUp = {
					navController.navigate(Start)
				},
				navigateToSettings = {
					navController.navigate(Settings)
				},
			)

			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<EacChat> {
			EacChatSelectionScreen(
				eacViewModel = eacViewModel,
				navigateToPinEntry = {
					navController.navigate(EacPin)
				},
				navigateUp = {
					navController.navigate(Start)
				},
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<EacPin> {
			EacPinEntryScreen(
				nfcDetected = {
					nfcDetected.value = true
				},
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { resultUrl ->
					navController.navigate(EacResult(resultUrl))
				},
				navigateBack = {
					navController.navigate(Start)
				},
				eacViewModel = eacViewModel,
			)
		}

		composable<PIN> {
			PinChangeScreen(
				pinChangeViewModel = pinChangeViewModel,
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { result ->
					navController.navigate(PinResult(result))
				},
				navigateBack = {
					navController.navigate(Start)
				},
				nfcDetected = {
					nfcDetected.value = true
				},
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<CAN> {
			CanEntryScreen(
				canEntryViewModel = canEntryViewModel,
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { result ->
					navController.navigate(PinResult(result))
				},
				navigateBack = {
					navController.navigate(Start)
				},
				nfcDetected = {
					nfcDetected.value = true
				},
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<PUK> {
			PukEntryScreen(
				pukEntryViewModel = pukEntryViewModel,
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { result ->
					navController.navigate(PinResult(result))
				},
				navigateBack = {
					navController.navigate(Start)
				},
				nfcDetected = {
					nfcDetected.value = true
				},
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<PinResult> { backStackEntry ->
			val pinResult = backStackEntry.toRoute<PinResult>()
			ResultScreen(
				pinResult.pinStatus,
				navigateToStart = {
					nfcDetected.value = false

					navController.navigate(Start) {
						popUpTo<Start>()
					}
				},
				navigateToOperation = {
					nfcDetected.value = false

					when (pinResult.pinStatus) {
						PinStatus.WrongPIN -> {
							pinChangeViewModel.clear()

							navController.navigate(PIN)
						}

						PinStatus.Suspended, PinStatus.WrongCAN -> {
							navController.navigate(CAN)
						}

						PinStatus.Blocked, PinStatus.WrongPUK -> {
							navController.navigate(PUK)
						}

						else -> {
							navController.navigate(Start)
						}
					}
				},
				eacResult = null,
				egkResult = null,
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<NFC> {
			NfcScreen(
				nfcDetected = nfcDetected.value,
				onCancel = {
					navController.navigateUp()
				},
			)
		}

		composable<EGK> {
			EgkCanEntryScreen(
				nfcDetected = {
					nfcDetected.value = true
				},
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { result ->
					navController.navigate(EgkResult(result))
				},
				navigateBack = {
					navController.navigate(Start)
				},
				egkViewModel = egkViewModel,
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<EacResult> { backStackEntry ->
			val eacResult = backStackEntry.toRoute<EacResult>()
			ResultScreen(
				null,
				navigateToStart = {
					nfcDetected.value = false

					navController.navigate(Start) {
						popUpTo<Start>()
					}
				},
				navigateToOperation = {},
				eacResult = eacResult.resultUrl,
				egkResult = null,
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<EgkResult> { backStackEntry ->
			val egkResult = backStackEntry.toRoute<EgkResult>()
			ResultScreen(
				null,
				navigateToStart = {
					nfcDetected.value = false

					navController.navigate(Start) {
						popUpTo<Start>()
					}
				},
				navigateToOperation = {},
				eacResult = null,
				egkResult = egkResult.result,
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
		}

		composable<Settings> {
			SettingsScreen(
				navigateToStart = {
					navController.navigate(Start)
				},
				settingsViewModel = settingsViewModel,
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				},
			)
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

@Suppress("ktlint:standard:function-naming")
@Composable
fun BackHandler(
	navController: NavController,
	onCleanup: () -> Unit,
) {
	NavigationBackHandler(
		state = rememberNavigationEventState(NavigationEventInfo.None),
		isBackEnabled = true,
		onBackCompleted = {
			onCleanup()
			navController.navigate(Start)
		},
	)
}
