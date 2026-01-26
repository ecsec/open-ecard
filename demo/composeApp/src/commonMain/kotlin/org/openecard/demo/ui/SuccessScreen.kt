package org.openecard.demo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Suppress("ktlint:standard:function-naming")
@Composable
fun SuccessScreen(onClick: () -> Unit = {}) {
	Column {
		Text(text = "Success")

		Button(
			onClick = {
				onClick()
			},
		) {
			Text("OK")
		}
	}
}
