package org.openecard.demo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
	navigateToDefaults: () -> Unit,
	navigateToConfig: () -> Unit,
) {
	val items by eacViewModel.chatItems.collectAsState()
	val mode by eacViewModel.uiMode.collectAsState()

	val (readAccessItems, specialFunctionItems) = items.partition { it.id.startsWith("DG") }

	Scaffold(
		topBar = {
			AppBar(
				AppBarState(
					title = "Select data to share",
					canNavigateUp = true,
					navigateUp = navigateUp,
					settingsEnabled = true,
					navigateToDefaults = navigateToDefaults,
					navigateToConfig = navigateToConfig,
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
							.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 52.dp),
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
			if (readAccessItems.isNotEmpty()) {
				item {
					Text("Read access", style = MaterialTheme.typography.headlineSmall)
				}

				items(readAccessItems) { item ->
					ChatSelectionItem(
						item = item,
						enabled = !item.required || mode.requiredChatEnabled,
						onToggle = { checked ->
							eacViewModel.updateChatSelection(
								items.map {
									if (it.id == item.id) {
										it.copy(selected = checked)
									} else {
										it
									}
								},
							)
						},
					)
				}
			}

			if (readAccessItems.isNotEmpty() && specialFunctionItems.isNotEmpty()) {
				item { HorizontalDivider() }
			}

			if (specialFunctionItems.isNotEmpty()) {
				item {
					Text("Special functions", style = MaterialTheme.typography.headlineSmall)
				}

				items(specialFunctionItems) { item ->
					ChatSelectionItem(
						item = item,
						enabled = !item.required || mode.requiredChatEnabled,
						onToggle = { checked ->
							eacViewModel.updateChatSelection(
								items.map {
									if (it.id == item.id) {
										it.copy(selected = checked)
									} else {
										it
									}
								},
							)
						},
					)
				}
			}
		}
	}
}

@Suppress("ktlint:standard:function-naming")
@Composable
fun ChatSelectionItem(
	item: ChatAttributeUi,
	onToggle: (Boolean) -> Unit,
	enabled: Boolean,
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
				enabled = enabled,
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
