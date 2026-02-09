package org.openecard.demo.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import org.openecard.demo.PinStatus
import org.openecard.demo.ui.CanEntryScreen
import org.openecard.demo.ui.EacPinEntryScreen
import org.openecard.demo.ui.EgkCanEntryScreen
import org.openecard.demo.ui.NfcScreen
import org.openecard.demo.ui.PinChangeScreen
import org.openecard.demo.ui.PukEntryScreen
import org.openecard.demo.ui.ResultScreen
import org.openecard.demo.ui.StartScreen
import org.openecard.demo.viewmodel.CanEntryViewModel
import org.openecard.demo.viewmodel.EacViewModel
import org.openecard.demo.viewmodel.EgkViewModel
import org.openecard.demo.viewmodel.PinChangeViewModel
import org.openecard.demo.viewmodel.PukEntryViewModel
import org.openecard.sc.iface.TerminalFactory

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun NavigationWrapper(
	nfcTerminalFactory: TerminalFactory?,
) {
	val navController = rememberNavController()
	val nfcDetected = rememberSaveable { mutableStateOf(false) }
	val pinChangeViewModel = remember { PinChangeViewModel(nfcTerminalFactory) }
	val canEntryViewModel = remember { CanEntryViewModel(nfcTerminalFactory) }
	val pukEntryViewModel = remember { PukEntryViewModel(nfcTerminalFactory) }
	val eacViewModel = remember { EacViewModel(nfcTerminalFactory) }
	val egkViewModel = remember { EgkViewModel(nfcTerminalFactory) }

	fun clearAll() {
		pinChangeViewModel.clear()
		canEntryViewModel.clear()
		pukEntryViewModel.clear()
		eacViewModel.clear()
		egkViewModel.clear()
	}

	NavHost(navController = navController, startDestination = Start) {
		composable<Start> {
			clearAll()

			StartScreen(
				navigateToPin = {
					navController.navigate(PIN)
				},
				navigateToEac = { url ->
					navController.navigate(EAC(url))

				},
				navigateToEgk = {
					navController.navigate(EGK)
				},
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				}
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
				}
			)
		}

		composable<NFC> {
			NfcScreen(
				nfcDetected = nfcDetected.value,
				onCancel = {
					navController.navigateUp()
				}
			)
		}

		composable<EAC> { backStackEntry ->
			val eac = backStackEntry.toRoute<EAC>()

			EacPinEntryScreen(
				tokenUrlProvider = {
					eac.tokenUrl
				},
				nfcDetected = {
					nfcDetected.value = true
				},
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { result ->
					navController.navigate(EacResult(result))
				},
				navigateBack = {
					navController.navigate(Start)
				},
				eacViewModel = eacViewModel
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
				egkViewModel = egkViewModel
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
				eacResult = eacResult.url,
				egkResult = null
			)
			BackHandler(
				navController = navController,
				onCleanup = {
					nfcDetected.value = false
				}
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
				}
			)
		}
	}
}

@Composable
fun BackHandler(
	navController: NavController,
	onCleanup: () -> Unit
) {
	NavigationBackHandler(
		state = rememberNavigationEventState(NavigationEventInfo.None),
		isBackEnabled = true,
		onBackCompleted = {
			onCleanup()
			navController.navigate(Start)
		}
	)
}



