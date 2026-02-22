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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState
import org.openecard.demo.viewmodel.DefaultsViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun DefaultsScreen(
	navigateUp: () -> Unit,
	defaultsViewModel: DefaultsViewModel,
) {
	var modified by remember { mutableStateOf(false) }

	var showDialog by remember { mutableStateOf(false) }
	var dialogMessage by remember { mutableStateOf("") }

	val state by defaultsViewModel.state.collectAsState()

	val isSubmitEnabled =
		state.npaPin.isNotBlank() || state.npaNewPin.isNotBlank() ||
			state.npaCan.isNotBlank() || state.npaPuk.isNotBlank() || state.egkCan.isNotBlank()

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Set your default values",
					canNavigateUp = true,
					navigateUp = navigateUp,
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
			Spacer(Modifier.height(8.dp))

			Text(text = "nPA", style = MaterialTheme.typography.headlineSmall, fontSize = 18.sp)

			OutlinedTextField(
				value = state.npaPin,
				onValueChange = { newValue ->
					defaultsViewModel.update { it.copy(npaPin = newValue) }
				},
				label = { Text("PIN") },
				visualTransformation = VisualTransformation.None,
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								defaultsViewModel.update { it.copy(npaPin = "") }
							}
						},
			)

			Spacer(Modifier.height(8.dp))

			OutlinedTextField(
				value = state.npaNewPin,
				onValueChange = { newValue ->
					defaultsViewModel.update { it.copy(npaNewPin = newValue) }
				},
				label = { Text("new PIN (only required for PIN change)") },
				visualTransformation = VisualTransformation.None,
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								defaultsViewModel.update { it.copy(npaNewPin = "") }
							}
						},
			)

			Spacer(Modifier.height(8.dp))

			OutlinedTextField(
				value = state.npaCan,
				onValueChange = { newValue ->
					defaultsViewModel.update { it.copy(npaCan = newValue) }
				},
				label = { Text("CAN") },
				visualTransformation = VisualTransformation.None,
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								defaultsViewModel.update { it.copy(npaCan = "") }
							}
						},
			)

			Spacer(Modifier.height(8.dp))

			OutlinedTextField(
				value = state.npaPuk,
				onValueChange = { newValue ->
					defaultsViewModel.update { it.copy(npaPuk = newValue) }
				},
				label = { Text("PUK") },
				visualTransformation = VisualTransformation.None,
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								defaultsViewModel.update { it.copy(npaPuk = "") }
							}
						},
			)

			Spacer(Modifier.height(8.dp))

			Text("eGK", style = MaterialTheme.typography.headlineSmall, fontSize = 18.sp)

			OutlinedTextField(
				value = state.egkCan,
				onValueChange = { newValue ->
					defaultsViewModel.update { it.copy(egkCan = newValue) }
				},
				label = { Text("CAN") },
				visualTransformation = VisualTransformation.None,
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								defaultsViewModel.update { it.copy(egkCan = "") }
							}
						},
			)

			Spacer(Modifier.height(8.dp))

			Button(
				enabled = isSubmitEnabled,
				onClick = {
					defaultsViewModel.update {
						it.copy(
							npaPin = state.npaPin,
							npaNewPin = state.npaNewPin,
							npaCan = state.npaCan,
							npaPuk = state.npaPuk,
							egkCan = state.egkCan,
						)
					}

					val invalid = defaultsViewModel.validateInput()
					if (invalid) {
						dialogMessage = "Defaults updated successfully." +
							"\nKeep in mind that the some of the entered values are not valid. Valid PINs must be 5 to 6 digits, valid CANs 6 digits and valid PUKs 10 digits long."
						showDialog = true
					} else {
						dialogMessage = "Defaults updated successfully."
						showDialog = true
					}
				},
			) {
				Text("Submit")
			}

			Spacer(Modifier.height(200.dp))

			if (showDialog) {
				AlertDialog(
					onDismissRequest = { showDialog = false },
					title = { Text("Success") },
					text = { Text(dialogMessage) },
					confirmButton = {
						TextButton(onClick = { showDialog = false }) {
							Text("OK")
						}
					},
				)
			}
		}
	}
}
