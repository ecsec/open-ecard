package org.openecard.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openecard.demo.PinStatus
import org.openecard.sc.iface.TerminalFactory

// @Preview(showBackground = true)
@Suppress("ktlint:standard:function-naming")
@Composable
fun ResultScreen(
	nfcTerminalFactory: TerminalFactory?,
	pinStatus: PinStatus?,
	eacResult: String?,
	navigateToStart: () -> Unit,
	navigateToOperation: (PinStatus) -> Unit,
) {
	val uriHandler = LocalUriHandler.current

	Column(
		modifier =
			Modifier
				.fillMaxSize()
				.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.SpaceEvenly,
	) {
		if (pinStatus != null) {
			Spacer(modifier = Modifier.weight(1f))
			Text(
				text = "Result: $pinStatus",
				fontSize = 24.sp,
				style = MaterialTheme.typography.headlineMedium,
			)
			when (pinStatus) {
				PinStatus.OK, PinStatus.Unknown -> {
					Spacer(Modifier.weight(1f))

					Button(
						onClick = {
							navigateToStart()
						},
					) {
						Text("Back to start")
					}
				}

				PinStatus.Suspended, PinStatus.Blocked -> {
					Text(
						text = "Your PIN is in state $pinStatus. Click next if you want to solve this.",
						fontSize = 16.sp,
					)

					Spacer(Modifier.weight(1f))

					Row(
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically,
						modifier = Modifier.padding(bottom = 32.dp),
					) {
						Button(
							onClick = {
								navigateToStart()
							},
						) {
							Text("Back to start")
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
			}
		} else if (eacResult != null) {
			Spacer(modifier = Modifier.weight(1f))
			Text(
				text = "Result: $eacResult",
				fontSize = 24.sp,
				style = MaterialTheme.typography.headlineMedium,
			)

			Button(
				onClick = {
					eacResult.let {
						uriHandler.openUri(it)
					}
				},
			) {
				Text("Open Result-URL")
			}
			Spacer(Modifier.weight(1f))

			Button(
				onClick = {
					navigateToStart()
				},
			) {
				Text("Back to start")
			}
		} else {
			Spacer(modifier = Modifier.weight(1f))
			Text(
				text = "Nothing to show",
				fontSize = 24.sp,
				style = MaterialTheme.typography.headlineMedium,
			)
			Spacer(Modifier.weight(1f))

			Button(
				onClick = {
					navigateToStart()
				},
			) {
				Text("Back to start")
			}
		}
	}
}
