package org.openecard.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState
import org.openecard.demo.viewmodel.Config
import org.openecard.demo.viewmodel.EacViewModel

@Suppress("ktlint:standard:function-naming")
@Composable
fun ConfigScreen(
	eacViewModel: EacViewModel,
	navigateUp: () -> Unit,
) {
	var showDialog by remember { mutableStateOf(false) }
	var dialogMessage by remember { mutableStateOf("") }

	val mode by eacViewModel.uiMode.collectAsState()

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Dev Options",
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
			Text("Configure your app", style = MaterialTheme.typography.headlineSmall)

			Spacer(Modifier.height(32.dp))

			Text(
				"EAC",
				style = MaterialTheme.typography.headlineSmall,
				fontSize = 18.sp,
			)

			Spacer(Modifier.height(24.dp))

			CheckboxItem(
				mode = mode,
				onToggle = { checked ->
					eacViewModel.updateConfig(
						mode.copy(
							requiredChatEnabled = checked,
						),
					)
				},
			)

			Spacer(Modifier.height(24.dp))

			Button(
				enabled = true,
				onClick = {
					eacViewModel.updateConfig(
						mode.copy(
							requiredChatEnabled = mode.requiredChatEnabled,
						),
					)
					showDialog = true
					dialogMessage = "Configuration updated successfully"
				},
			) {
				Text("Confirm")
			}

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

@Suppress("ktlint:standard:function-naming")
@Composable
fun CheckboxItem(
	mode: Config,
	onToggle: (Boolean) -> Unit,
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		shape = RoundedCornerShape(12.dp),
		colors =
			CardDefaults.cardColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant,
			),
	) {
		Row(
			modifier =
				Modifier
					.fillMaxWidth()
					.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Checkbox(
				checked = mode.requiredChatEnabled,
				onCheckedChange = onToggle,
				enabled = true,
			)

			Column(modifier = Modifier.padding(start = 8.dp)) {
				Text(
					text = "Enable required CHAT deselection",
					style = MaterialTheme.typography.titleMedium,
				)
			}
		}
	}
}
