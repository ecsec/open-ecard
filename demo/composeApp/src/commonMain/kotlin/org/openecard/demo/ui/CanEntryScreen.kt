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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState
import org.openecard.demo.PinStatus
import org.openecard.demo.viewmodel.PinMgmtViewModel

@Composable
fun CanEntryScreen(
	pinMgmtViewModel: PinMgmtViewModel,
	navigateToNfc: () -> Unit,
	navigateToResult: (PinStatus) -> Unit,
	navigateBack: () -> Unit,
	nfcDetected: () -> Unit,
) {
	val scope = rememberCoroutineScope()

	val can = rememberSaveable { mutableStateOf("") }
	val pin = rememberSaveable { mutableStateOf("") }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Recover your PIN",
					canNavigateUp = true,
					navigateUp = navigateBack
				)
			)
		}
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center,
		) {
			Text("Recover your PIN", fontSize = 28.sp)

			Spacer(Modifier.height(32.dp))

			OutlinedTextField(
				value = can.value,
				onValueChange = {
					can.value = it
				},
				label = { Text("CAN") },
				visualTransformation = PasswordVisualTransformation(),
				modifier =
					Modifier
						.fillMaxWidth(),
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
			)

			Spacer(Modifier.height(16.dp))

			OutlinedTextField(
				value = pin.value,
				onValueChange = {
					pin.value = it
				},
				label = { Text("PIN") },
				visualTransformation = PasswordVisualTransformation(),
				modifier =
					Modifier
						.fillMaxWidth(),
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
			)

			Spacer(Modifier.height(24.dp))

			Button(
				onClick = {
					navigateToNfc()
					scope.launch {

						CoroutineScope(Dispatchers.IO).launch {
							try {
								val result =
									pinMgmtViewModel.recoverWithCan(
										nfcDetected,
										can.value,
										pin.value,
									)

								withContext(Dispatchers.Main) {
									navigateToResult(result)
								}

							} catch (e: Exception) {
								e.message
							}
						}
					}
				},
				modifier = Modifier
					.fillMaxWidth()
					.height(50.dp),
			) {
				Text("Submit", fontSize = 16.sp)
			}
		}
	}
}
