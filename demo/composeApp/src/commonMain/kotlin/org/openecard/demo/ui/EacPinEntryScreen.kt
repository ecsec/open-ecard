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
import org.openecard.demo.TokenUrlProvider
import org.openecard.demo.viewmodel.EacViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun EacPinEntryScreen(
	eacViewModel: EacViewModel,
	tokenUrlProvider: TokenUrlProvider,
	navigateToResult: (String) -> Unit,
	navigateToNfc: () -> Unit,
	navigateBack: () -> Unit,
	nfcDetected: () -> Unit,
) {
	val state by eacViewModel.eacUiState.collectAsState()

	var showDialog by remember { mutableStateOf(false) }
	var dialogTitle by remember { mutableStateOf("") }
	var dialogMessage by remember { mutableStateOf("") }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Enter your PIN",
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
				visualTransformation = PasswordVisualTransformation(),
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				keyboardOptions = KeyboardOptions(
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
							val result = eacViewModel.doEac(
								nfcDetected,
								tokenUrlProvider(),
								state.pin
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
				modifier = Modifier
					.fillMaxWidth()
					.height(50.dp),
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
