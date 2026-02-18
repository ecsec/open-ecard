package org.openecard.demo.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import demo.composeapp.generated.resources.Res
import demo.composeapp.generated.resources.contactless
import org.jetbrains.compose.resources.painterResource

@Suppress("ktlint:standard:function-naming")
@Composable
fun NfcScreen(
	nfcDetected: Boolean,
	onCancel: () -> Unit,
) {
	val progress by animateFloatAsState(
		targetValue = if (nfcDetected) 1f else 0f,
		animationSpec = tween(durationMillis = 1500, easing = LinearEasing),
		label = "",
	)

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
			Canvas(modifier = Modifier.fillMaxSize()) {
				val strokeWidth = 12.dp.toPx()
				drawArc(
					// secondary
					color = Color(0xFF006E24),
					// tertiary
// 					color = Color(0xFF7B4983),
					startAngle = -90f,
					sweepAngle = 360f * progress,
					useCenter = false,
					style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
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
