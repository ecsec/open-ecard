package org.openecard.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.openecard.demo.PinStatus
import org.openecard.demo.suspendRecovery
import org.openecard.sc.iface.TerminalFactory

@Suppress("ktlint:standard:function-naming")
@Composable
fun CanEntryScreen(
	nfcTerminalFactory: TerminalFactory?,
	navigateToNfc: () -> Unit,
	navigateToResult: (PinStatus) -> Unit,
) {
	val scope = rememberCoroutineScope()
	val can = rememberSaveable { mutableStateOf("") }
	val pin = rememberSaveable { mutableStateOf("") }

	Column(
		modifier =
			Modifier
				.fillMaxSize()
				.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Text(
			text = "Recover your PIN",
			fontSize = 28.sp,
			style = MaterialTheme.typography.headlineMedium,
			modifier = Modifier.padding(bottom = 32.dp),
		)

		OutlinedTextField(
			value = can.value,
			onValueChange = {
				can.value = it
			},
			label = { Text("CAN") },
			visualTransformation = PasswordVisualTransformation(),
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(bottom = 24.dp),
			singleLine = true,
			keyboardOptions =
				KeyboardOptions(
					keyboardType = KeyboardType.NumberPassword,
					imeAction = ImeAction.Done,
				),
		)

		OutlinedTextField(
			value = pin.value,
			onValueChange = {
				pin.value = it
			},
			label = { Text("PIN") },
			visualTransformation = PasswordVisualTransformation(),
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(bottom = 24.dp),
			singleLine = true,
			keyboardOptions =
				KeyboardOptions(
					keyboardType = KeyboardType.NumberPassword,
					imeAction = ImeAction.Done,
				),
		)

		Spacer(Modifier.height(8.dp))

		Button(
			onClick = {
				navigateToNfc()

				scope.launch {
					CoroutineScope(Dispatchers.IO).launch {
						try {
							val result = suspendRecovery(nfcTerminalFactory, can = can.value, pin = pin.value)
							withContext(Dispatchers.Main) {
								navigateToResult(result)
							}
						} catch (e: Exception) {
							e.message
						}
					}
				}
			},
			modifier =
				Modifier
					.fillMaxWidth()
					.height(50.dp),
		) {
			Text(text = "Submit", fontSize = 16.sp)
		}
	}
}
