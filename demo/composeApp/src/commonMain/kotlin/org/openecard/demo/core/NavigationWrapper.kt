package org.openecard.demo.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.openecard.demo.PinStatus
import org.openecard.demo.TokenUrlProvider
import org.openecard.demo.ui.CanEntryScreen
import org.openecard.demo.ui.EacPinEntryScreen
import org.openecard.demo.ui.EgkCanEntryScreen
import org.openecard.demo.ui.NfcScreen
import org.openecard.demo.ui.PinChangeScreen
import org.openecard.demo.ui.PukEntryScreen
import org.openecard.demo.ui.ResultScreen
import org.openecard.demo.ui.StartScreen
import org.openecard.sc.iface.TerminalFactory

@Suppress("ktlint:standard:function-naming")
@Composable
fun NavigationWrapper(
	nfcTerminalFactory: TerminalFactory?,
	tokenUrlProvider: TokenUrlProvider?
) {


	val navController = rememberNavController()

	val nfcDetected = rememberSaveable { mutableStateOf(false) }

	val tokenUrl = rememberSaveable { mutableStateOf("") }



	val result = rememberSaveable { mutableStateOf("") }

	val scope = rememberCoroutineScope()

	NavHost(navController = navController, startDestination = Start) {

		composable<Start> {
			StartScreen(
				nfcTerminalFactory = nfcTerminalFactory,
				tokenUrlProvider = tokenUrlProvider,
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
		}

//		composable<EAC> {
//			if (tokenUrlProvider != null) {
//				EacPinEntryScreen(
//					nfcTerminalFactory = nfcTerminalFactory,
//					tokenUrlProvider = {
//						tokenUrlProvider()
//					},
//					tokenUrl = tokenUrl.value,
//					nfcDetected = {
//						nfcDetected.value = true
//					},
//					navigateToNfc = {
//						navController.navigate(NFC)
//					},
//					navigateToResult = { result ->
//						navController.navigate(EacResult(result))
//					},
//					// 				result = { result.value },
//				)
//			}
//		}

		composable<EAC> { backStackEntry ->
			val eac = backStackEntry.toRoute<EAC>()

				EacPinEntryScreen(
					nfcTerminalFactory = nfcTerminalFactory,
					tokenUrlProvider = {
						eac.tokenUrl
					},
					tokenUrl = tokenUrl.value,
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
						navController.navigateUp()
					}
				)
		}

		composable<PIN> {
			PinChangeScreen(
				nfcTerminalFactory = nfcTerminalFactory,
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { result ->
					navController.navigate(PinResult(result))
				},
				nfcDetected = {
					nfcDetected.value = true
				},
				navigateBack = {
					navController.navigateUp()
				}
			)
		}

		composable<CAN> {
			CanEntryScreen(
				nfcTerminalFactory = nfcTerminalFactory,
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { result ->
					navController.navigate(PinResult(result))
				},
				navigateBack = {
					navController.navigateUp()
				}
			)
		}

		composable<PUK> {
			PukEntryScreen(
				nfcTerminalFactory = nfcTerminalFactory,
				navigateToNfc = {
					navController.navigate(NFC)
				},
				navigateToResult = { result ->
					navController.navigate(PinResult(result))
				},
				navigateBack = {
					navController.navigateUp()
				}
			)
		}



		composable<EGK> {
			EgkCanEntryScreen(
				nfcTerminalFactory = nfcTerminalFactory,
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
					navController.navigateUp()
				}
// 				result = { result.value },
			)
		}

		composable<NFC> {
			NfcScreen(
				nfcDetected.value,
				onCancel = {
					navController.navigate(Start)
				}
			)
		}

		composable<PinResult> { backStackEntry ->
			val pinResult = backStackEntry.toRoute<PinResult>()
			// 			val result: PinStatus = backStackEntry.toRoute()
			ResultScreen(
				nfcTerminalFactory = nfcTerminalFactory,
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
						PinStatus.Suspended -> {
							navController.navigate(CAN)
						}

						PinStatus.Blocked -> {
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
			// 			ResultScreen(result) {
		}

		composable<EacResult> { backStackEntry ->
			val eacResult = backStackEntry.toRoute<EacResult>()
			// 			val result: PinStatus = backStackEntry.toRoute()
			ResultScreen(
				nfcTerminalFactory = nfcTerminalFactory,
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
			// 			ResultScreen(result) {
		}

		composable<EgkResult> { backStackEntry ->
			val egkResult = backStackEntry.toRoute<EgkResult>()
			// 			val result: PinStatus = backStackEntry.toRoute()
			ResultScreen(
				nfcTerminalFactory = nfcTerminalFactory,
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
			// 			ResultScreen(result) {
		}
	}
}

// 	NavHost(navController = navController, startDestination = Start) {
// 		composable<Start> {
// 			StartScreen {
// 				navController.navigate(PIN)
// 			}
// 		}
//
// 		composable<PIN> {
// 			PinChangeScreen { result ->
// 				navController.navigate(Result(result))
// 			}
// 		}
//
// 		composable<Result> { backStackEntry ->
// 			val result = backStackEntry.toRoute<Result>()
// 			// 			val result: PinStatus = backStackEntry.toRoute()
// // 			ResultScreen(result.pinChanged) {
// 				 			ResultScreen(result.pinStatus) {
// 				navController.navigate(Start) {
// 					// 					popUpTo(Start)
// 					popUpTo<Start>()
// 				}
// 			}
// 		}
// 	}

// 	NavHost(navController = navController, startDestination = "start") {
// 		composable("start") {
// 			StartScreen {
// 				navController.navigate("pin")
// 			}
// 		}
//
// 		composable("pin") {
// 			PinChangeScreen { result ->
// 				navController.navigate("result/$result")
// 			}
// 		}
//
// 		composable("result") { backStackEntry ->
// 			val result = backStackEntry.toRoute<Result>()
// 			ResultScreen(result)
// 		}
// 	}
