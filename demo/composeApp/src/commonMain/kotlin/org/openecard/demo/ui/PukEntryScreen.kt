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
fun PukEntryScreen(
	pinMgmtViewModel: PinMgmtViewModel,
	navigateToNfc: () -> Unit,
	navigateToResult: (PinStatus) -> Unit,
	navigateBack: () -> Unit,
	nfcDetected: () -> Unit,
) {
	val scope = rememberCoroutineScope()

	val puk = rememberSaveable { mutableStateOf("") }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Unblock your PIN",
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
			Text("Unblock your PIN", fontSize = 28.sp)

			Spacer(Modifier.height(32.dp))

			OutlinedTextField(
				value = puk.value,
				onValueChange = {
					puk.value = it
				},
				label = { Text("PUK") },
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
									pinMgmtViewModel.unblockPin(
										nfcDetected,
										puk.value,
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
