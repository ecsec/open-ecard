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
import org.openecard.demo.TokenUrlProvider
import org.openecard.demo.viewmodel.EacViewModel

@OptIn(ExperimentalUnsignedTypes::class)
@Suppress("ktlint:standard:function-naming")
@Composable
fun EacPinEntryScreen(
	eacViewModel: EacViewModel,
	navigateToResult: (String) -> Unit,
	navigateToNfc: () -> Unit,
	navigateBack: () -> Unit,
	nfcDetected: () -> Unit,
) {
	val state by eacViewModel.eacUiState.collectAsState()

	var showDialog by remember { mutableStateOf(false) }
	var dialogTitle by remember { mutableStateOf("") }
	var dialogMessage by remember { mutableStateOf("") }

	var isPinVisible by remember { mutableStateOf(false) }
	var modified by remember { mutableStateOf(false) }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Enter your PIN",
					canNavigateUp = true,
					navigateUp = navigateBack,
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
				text = "Please enter your PIN",
				fontSize = 28.sp,
				style = MaterialTheme.typography.headlineMedium,
			)

			Spacer(Modifier.height(32.dp))

			OutlinedTextField(
				value = state.pin,
				onValueChange = {
					eacViewModel.onPinChanged(it)
				},
				label = { Text("PIN") },
				visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
				trailingIcon = {
					val icon = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
					val description = if (isPinVisible) "Hide password" else "Show password"
					IconButton(onClick = { isPinVisible = !isPinVisible }) {
						Icon(imageVector = icon, contentDescription = description)
					}
				},
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								eacViewModel.clear()
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
					val error = eacViewModel.validatePin()
					if (error == null) {
						navigateToNfc()

						CoroutineScope(Dispatchers.IO).launch {
							val result =
// 								eacViewModel.doEac(
// 									state.pin,
// 									nfcDetected,
// 								)
								eacViewModel.startEacProcess(
									nfcDetected,
									state.pin,
								)

							withContext(Dispatchers.Main) {
								if (result != null) {
									navigateToResult(result)
								} else {
									dialogTitle = "Error"
									dialogMessage = "Something went wrong. Please try again."
									showDialog = true
								}
							}
						}
					} else {
						eacViewModel.clear()

						dialogTitle = "Invalid Input"
						dialogMessage = error
						showDialog = true
					}
				},
			) {
				Text(text = "Submit", fontSize = 16.sp)
			}

			if (showDialog) {
				AlertDialog(
					onDismissRequest = { showDialog = false },
					title = { Text(dialogTitle) },
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
