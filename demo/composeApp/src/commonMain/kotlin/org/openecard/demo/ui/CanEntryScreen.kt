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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import org.openecard.demo.viewmodel.CanEntryViewModel

@Composable
fun CanEntryScreen(
	canEntryViewModel: CanEntryViewModel,
	navigateToNfc: () -> Unit,
	navigateToResult: (PinStatus) -> Unit,
	navigateBack: () -> Unit,
	nfcDetected: () -> Unit,
) {
	val state by canEntryViewModel.canPinUiState.collectAsState()

	var showDialog by rememberSaveable { mutableStateOf(false) }
	var dialogMessage by rememberSaveable { mutableStateOf("") }

	var isCanVisible by remember { mutableStateOf(false) }
	var isPinVisible by remember { mutableStateOf(false) }
	var modified by remember { mutableStateOf(false) }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Recover your PIN",
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
			Text("Recover your PIN", fontSize = 28.sp)

			Spacer(Modifier.height(32.dp))

			OutlinedTextField(
				value = state.can,
				onValueChange = {
					canEntryViewModel.onCanChanged(it)
				},
				label = { Text("CAN") },
				visualTransformation = if (isCanVisible) VisualTransformation.None else PasswordVisualTransformation(),
				trailingIcon = {
					val icon = if (isCanVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
					val description = if (isCanVisible) "Hide password" else "Show password"
					IconButton(onClick = { isCanVisible = !isCanVisible }) {
						Icon(imageVector = icon, contentDescription = description)
					}
				},
				modifier =
					Modifier
						.fillMaxWidth()
						.onFocusChanged { focusState ->
							if (focusState.isFocused && !modified) {
								modified = true
								canEntryViewModel.clear()
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
				value = state.pin,
				onValueChange = {
					canEntryViewModel.onPinChanged(it)
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
								canEntryViewModel.clear()
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
					val error = canEntryViewModel.validateCanPin()
					if (error == null) {
						navigateToNfc()

						CoroutineScope(Dispatchers.IO).launch {
							val result =
								canEntryViewModel.recoverWithCan(
									nfcDetected,
									state.can,
									state.pin,
								)
							withContext(Dispatchers.Main) {
								navigateToResult(result)
							}
						}
					} else {
						canEntryViewModel.clear()

						dialogMessage = error
						showDialog = true
					}
				},
			) {
				Text("Submit", fontSize = 16.sp)
			}

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
