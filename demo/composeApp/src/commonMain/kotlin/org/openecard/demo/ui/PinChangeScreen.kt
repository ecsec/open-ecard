package org.openecard.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
import org.openecard.demo.viewmodel.PinChangeViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun PinChangeScreen(
	pinChangeViewModel: PinChangeViewModel,
	navigateToResult: (PinStatus) -> Unit,
	navigateToNfc: () -> Unit,
	navigateBack: () -> Unit,
	navigateToDefaults: () -> Unit,
	navigateToConfig: () -> Unit,
	nfcDetected: () -> Unit,
) {
	val state by pinChangeViewModel.pinChangeState.collectAsState()

	var showDialog by remember { mutableStateOf(false) }
	var dialogMessage by remember { mutableStateOf("") }

	var isOldPinVisible by remember { mutableStateOf(false) }
	var isNewPinVisible by remember { mutableStateOf(false) }
	var isRepeatPinVisible by remember { mutableStateOf(false) }
	var modified by remember { mutableStateOf(false) }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Change your PIN",
					canNavigateUp = true,
					navigateUp = navigateBack,
					settingsEnabled = true,
					navigateToDefaults = navigateToDefaults,
					navigateToConfig = navigateToConfig,
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
			Text(
				text = "Change your PIN",
				fontSize = 28.sp,
				style = MaterialTheme.typography.headlineMedium,
			)

			Spacer(Modifier.height(32.dp))

			OutlinedTextField(
				value = state.oldPin,
				onValueChange = {
					pinChangeViewModel.onOldPinChanged(it)
				},
				label = { Text("old PIN") },
				visualTransformation = if (isOldPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
				trailingIcon = {
					val icon = if (isOldPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
					val description = if (isOldPinVisible) "Hide password" else "Show password"
					IconButton(onClick = { isOldPinVisible = !isOldPinVisible }) {
						Icon(imageVector = icon, contentDescription = description)
					}
				},
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								pinChangeViewModel.clear()
							}
						},
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
			)

			Spacer(Modifier.height(16.dp))

			OutlinedTextField(
				value = state.newPin,
				onValueChange = {
					pinChangeViewModel.onNewPinChanged(it)
				},
				label = { Text("new PIN") },
				visualTransformation = if (isNewPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
				trailingIcon = {
					val icon = if (isNewPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
					val description = if (isNewPinVisible) "Hide password" else "Show password"
					IconButton(onClick = { isNewPinVisible = !isNewPinVisible }) {
						Icon(imageVector = icon, contentDescription = description)
					}
				},
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								pinChangeViewModel.clear()
							}
						},
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
			)

			Spacer(Modifier.height(16.dp))

			OutlinedTextField(
				value = state.repeatPin,
				onValueChange = {
					pinChangeViewModel.onRepeatPinChanged(it)
				},
				label = { Text("repeat new PIN") },
				visualTransformation = if (isRepeatPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
				trailingIcon = {
					val icon = if (isRepeatPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
					val description = if (isRepeatPinVisible) "Hide password" else "Show password"
					IconButton(onClick = { isRepeatPinVisible = !isRepeatPinVisible }) {
						Icon(imageVector = icon, contentDescription = description)
					}
				},
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								pinChangeViewModel.clear()
							}
						},
				singleLine = true,
				keyboardOptions =
					KeyboardOptions(
						keyboardType = KeyboardType.NumberPassword,
						imeAction = ImeAction.Done,
					),
			)

			Spacer(Modifier.height(24.dp))

			Button(
				enabled = state.isSubmitEnabled,
				onClick = {
					val error = pinChangeViewModel.validatePin()
					if (error == null) {
						navigateToNfc()

						CoroutineScope(Dispatchers.IO).launch {
							val result =
								pinChangeViewModel.changePin(
									nfcDetected,
									state.oldPin,
									state.newPin,
								)
							withContext(Dispatchers.Main) {
								navigateToResult(result)
							}
						}
					} else {
						pinChangeViewModel.clear()

						dialogMessage = error
						showDialog = true
					}
				},
			) {
				Text("Submit")
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
					},
				)
			}
		}
	}
}
