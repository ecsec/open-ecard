package org.openecard.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
fun PinChangeScreen(
	pinMgmtViewModel: PinMgmtViewModel,
	navigateToResult: (PinStatus) -> Unit,
	navigateToNfc: () -> Unit,
	navigateBack: () -> Unit,
	nfcDetected: () -> Unit,
) {
	val scope = rememberCoroutineScope()

	val oldPin = rememberSaveable { mutableStateOf("") }
	val newPin = rememberSaveable { mutableStateOf("") }
	val repeat = rememberSaveable { mutableStateOf("") }

	val allFilled = oldPin.value.isNotBlank() &&
		newPin.value.isNotBlank() &&
		repeat.value.isNotBlank()

	val lengthValid = oldPin.value.length in 5..6 &&
		newPin.value.length in 5..6 &&
		repeat.value.length in 5..6

	val pinsMatch = newPin.value == repeat.value
	val validInput = lengthValid && pinsMatch

	var showDialog by remember { mutableStateOf(false) }
	var dialogMessage by remember { mutableStateOf("") }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Change your PIN",
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
			Text(
				text = "Change your PIN",
				fontSize = 28.sp,
				style = MaterialTheme.typography.headlineMedium,
			)


			Spacer(Modifier.height(32.dp))

			OutlinedTextField(
				value = oldPin.value,
				onValueChange = {
					oldPin.value = it
				},
				label = { Text("old PIN") },
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
				value = newPin.value,
				onValueChange = {
					newPin.value = it
				},
				label = { Text("new PIN") },
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
				value = repeat.value,
				onValueChange = {
					repeat.value = it
				},
				label = { Text("repeat new PIN") },
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
				enabled = allFilled,
				onClick = {
					if (validInput) {
						navigateToNfc()

						scope.launch {

							CoroutineScope(Dispatchers.IO).launch {
								try {
									val result =
										pinMgmtViewModel.changePin(
											nfcDetected,
											oldPin.value,
											newPin.value,
										)

									withContext(Dispatchers.Main) {
										navigateToResult(result)
									}
								} catch (e: Exception) {
									e.message
								}
							}
						}
					} else {
						showDialog = true
						dialogMessage =
							when {
								!lengthValid -> "PIN must be 5 to 6 digits long."
								!pinsMatch -> "New PINs do not match."
								else -> "Invalid input."
							}
					}
				},
				modifier = Modifier
					.fillMaxWidth()
					.height(50.dp),
			) {
				Text("Submit", fontSize = 16.sp)
			}

			Spacer(Modifier.height(120.dp))

			if (showDialog) {
				AlertDialog(
					onDismissRequest = { showDialog = false },
					title = { Text("Invalid Input") },
					text = { Text(dialogMessage) },
					confirmButton = {
						TextButton(onClick = { showDialog = false }) {
							Text("OK")
						}
					}
				)
			}
		}
	}
}
