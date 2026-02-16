package org.openecard.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.openecard.demo.AppBar
import org.openecard.demo.AppBarState
import org.openecard.demo.util.ChatAttributeUi
import org.openecard.demo.viewmodel.EacViewModel

@Suppress("ktlint:standard:function-naming")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EacChatSelectionScreen(
	eacViewModel: EacViewModel,
	navigateToPinEntry: () -> Unit,
	navigateUp: () -> Unit,
) {
	val items by eacViewModel.chatItems.collectAsState()

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Select data to share",
					canNavigateUp = true,
					navigateUp = navigateUp,
				),
			)
		},
		bottomBar = {
			Surface(
				tonalElevation = 3.dp,
			) {
				Button(
					onClick = {
						// build object
						eacViewModel.confirmChatSelection()

						navigateToPinEntry()
					},
					modifier =
						Modifier
							.fillMaxWidth()
							.padding(16.dp),
				) {
					Text("Continue")
				}
			}
		},
	) { padding ->
		LazyColumn(
			modifier =
				Modifier
					.padding(padding)
					.padding(16.dp)
					.fillMaxSize(),
			verticalArrangement = Arrangement.spacedBy(12.dp),
		) {
			items(items.size) { index ->
				val item = items[index]

				ChatSelectionItem(
					item = item,
					onToggle = { checked ->
						if (!item.required) {
							val updated = items.toMutableList()
							updated[index] = item.copy(selected = checked)
							eacViewModel.updateChatSelection(updated)
						}
					},
				)
			}
		}
	}
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ChatSelectionItem(
	item: ChatAttributeUi,
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
				checked = item.selected,
				onCheckedChange = onToggle,
				enabled = !item.required,
			)

			Column(modifier = Modifier.padding(start = 8.dp)) {
				Text(
					text = item.label,
					style = MaterialTheme.typography.titleMedium,
				)
			}
		}
	}
}
