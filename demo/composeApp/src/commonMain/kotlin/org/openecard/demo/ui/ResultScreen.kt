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
				PinResult(
					pinResult,
					navigateToStart,
					navigateToOperation,
				)
			} else if (eacResult != null) {
				EacResult(
					eacResult,
					navigateToStart,
				)
			} else if (egkResult != null) {
				EgkResult(
					egkResult,
					navigateToStart,
				)
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
fun EgkResult(
	egkResult: String,
	navigateToStart: () -> Unit,
) {
	Text(
		text = egkResult,
		fontSize = 24.sp,
		style = MaterialTheme.typography.headlineMedium,
	)

	Spacer(Modifier.height(24.dp))

	BackToStartButton {
		navigateToStart()
	}
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun EacResult(
	eacResult: String,
	navigateToStart: () -> Unit,
) {
	val uriHandler = LocalUriHandler.current
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
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun PinResult(
	pinResult: PinOperationResult,
	navigateToStart: () -> Unit,
	navigateToOperation: (PinStatus) -> Unit,
) {
	Text(
		text = "Result: ${pinResult.status?.label ?: pinResult.errorMessage}",
		fontSize = 24.sp,
		style = MaterialTheme.typography.headlineMedium,
	)
	when (val status = pinResult.status) {
		null -> {
			Spacer(Modifier.height(24.dp))
			BackToStartButton { navigateToStart() }
		}

		PinStatus.OK, PinStatus.Unknown -> {
			Text(status.infoText)

			Spacer(Modifier.height(24.dp))

			BackToStartButton {
				navigateToStart()
			}
		}

		PinStatus.WrongPIN, PinStatus.WrongCAN, PinStatus.WrongPUK -> {
			Text(status.infoText)

			Spacer(Modifier.height(24.dp))

			Button(
				onClick = {
					navigateToOperation(status)
				},
			) {
				Text("Try again")
			}
		}

		PinStatus.Suspended, PinStatus.Blocked -> {
			Text(status.infoText)

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
						navigateToOperation(status)
					},
				) {
					Text("Next")
				}
			}
		}

		else -> {}
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

val PinStatus.label: String
	get() =
		when (this) {
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

val PinStatus.infoText: String
	get() =
		when (this) {
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
