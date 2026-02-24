package org.openecard.demo.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.composeapp.generated.resources.Res
import demo.composeapp.generated.resources.contactless
import org.jetbrains.compose.resources.painterResource
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState

@Suppress("ktlint:standard:function-naming")
@Composable
fun NfcScreen(
	nfcDetected: Boolean,
	onCancel: () -> Unit,
) {
	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Waiting for card...",
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
			Text("Please bring card", fontSize = 24.sp)

			Spacer(Modifier.height(32.dp))

			Box(
				modifier = Modifier.size(220.dp),
				contentAlignment = Alignment.Center,
			) {
				if (nfcDetected) {
					CircularProgressIndicator(
						modifier = Modifier.fillMaxSize(),
						color = Color(0xFF006E24),
						strokeWidth = 12.dp,
					)
				}

				Image(
					painter = painterResource(Res.drawable.contactless),
					contentDescription = "",
					modifier =
						Modifier
							.size(200.dp)
							.clip(CircleShape),
					contentScale = ContentScale.Fit,
				)
			}

			Spacer(Modifier.height(32.dp))

			Button(onClick = onCancel) {
				Text("Cancel")
			}
		}
	}
}
