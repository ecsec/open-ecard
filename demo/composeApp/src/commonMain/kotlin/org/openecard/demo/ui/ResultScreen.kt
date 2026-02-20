package org.openecard.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState
import org.openecard.demo.PinOperationResult
import org.openecard.demo.PinStatus

@Suppress("ktlint:standard:function-naming")
@Composable
fun ResultScreen(
	pinResult: PinOperationResult?,
	eacResult: String?,
	egkResult: String?,
	navigateToStart: () -> Unit,
	navigateToOperation: (PinStatus) -> Unit,
) {
	val uriHandler = LocalUriHandler.current

	val pinStatus = pinResult?.status
	val pinError = pinResult?.errorMessage

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Process done",
				),
			)
		},
	) {
		Column(
			modifier =
				Modifier
					.fillMaxSize()
					.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			if (pinResult != null) {
				if (pinStatus != null) {
					Text(
						text = "Result: ${label(pinStatus)}",
						fontSize = 24.sp,
						style = MaterialTheme.typography.headlineMedium,
					)
					when (pinResult.status) {
						PinStatus.OK, PinStatus.Unknown -> {
							val infoText = infoText(pinStatus)

							Text(infoText)

							Spacer(Modifier.height(24.dp))

							BackToStartButton {
								navigateToStart()
							}
						}

						PinStatus.WrongPIN, PinStatus.WrongCAN, PinStatus.WrongPUK -> {
							val infoText = infoText(pinStatus)

							Text(infoText)

							Spacer(Modifier.height(24.dp))

							Button(
								onClick = {
									navigateToOperation(pinStatus)
								},
							) {
								Text("Try again")
							}
						}

						PinStatus.Suspended, PinStatus.Blocked -> {
							val infoText = infoText(pinStatus)

							Text(infoText)

							Spacer(Modifier.height(24.dp))

							Row(
								horizontalArrangement = Arrangement.Center,
								verticalAlignment = Alignment.CenterVertically,
							) {
								BackToStartButton {
									navigateToStart()
								}
								Spacer(Modifier.width(8.dp))

								Button(
									onClick = {
										navigateToOperation(pinStatus)
									},
								) {
									Text("Next")
								}
							}
						}

						else -> {}
					}
				} else {
					Text(
						text = "$pinError",
						fontSize = 24.sp,
						style = MaterialTheme.typography.headlineMedium,
					)

					Spacer(Modifier.height(24.dp))

					BackToStartButton { navigateToStart() }
				}
			} else if (eacResult != null) {
				Text(
					modifier = Modifier.padding(16.dp),
					text = "Result: $eacResult",
					fontSize = 24.sp,
					style = MaterialTheme.typography.headlineMedium,
				)

				val isResultUrl = eacResult.startsWith("https")

				if (isResultUrl) {
					Spacer(Modifier.height(24.dp))

					Button(
						onClick = {
							try {
								uriHandler.openUri(eacResult)
							} catch (e: Exception) {
								e.message
							}
						},
					) {
						Text("Open Result-URL")
					}
				}
				Spacer(Modifier.height(24.dp))

				BackToStartButton {
					navigateToStart()
				}
			} else if (egkResult != null) {
				Text(
					text = egkResult,
					fontSize = 24.sp,
					style = MaterialTheme.typography.headlineMedium,
				)

				Spacer(Modifier.height(24.dp))

				BackToStartButton {
					navigateToStart()
				}
			} else {
				Text(
					text = "Nothing to show",
					fontSize = 24.sp,
					style = MaterialTheme.typography.headlineMedium,
				)

				Spacer(Modifier.height(24.dp))

				BackToStartButton {
					navigateToStart()
				}
			}
		}
	}
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun BackToStartButton(navigateToStart: () -> Unit) {
	Button(
		onClick = {
			navigateToStart()
		},
	) {
		Text("Back to start")
	}
}

fun label(pinStatus: PinStatus): String =
	when (pinStatus) {
		PinStatus.OK -> {
			"Success"
		}

		PinStatus.WrongPIN -> {
			"Wrong PIN"
		}

		PinStatus.Retry -> {
			""
		}

		PinStatus.Suspended -> {
			"PIN is suspended"
		}

		PinStatus.WrongCAN -> {
			"CAN was wrong"
		}

		PinStatus.Blocked -> {
			"PIN is blocked"
		}

		PinStatus.WrongPUK -> {
			"PUK was wrong"
		}

		PinStatus.Unknown -> {
			"PIN state unknown"
		}
	}

fun infoText(pinStatus: PinStatus): String =
	when (pinStatus) {
		PinStatus.OK -> {
			"Pin operation was successful"
		}

		PinStatus.WrongPIN -> {
			"Please try again."
		}

		PinStatus.Retry -> {
			""
		}

		PinStatus.Suspended -> {
			"Please click next to resolve this."
		}

		PinStatus.WrongCAN -> {
			"Please try again."
		}

		PinStatus.Blocked -> {
			"Please click next to resolve this."
		}

		PinStatus.WrongPUK -> {
			"Please try again."
		}

		PinStatus.Unknown -> {
			"Something went wrong. Please try again."
		}
	}
