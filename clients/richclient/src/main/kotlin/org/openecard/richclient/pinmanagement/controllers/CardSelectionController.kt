package org.openecard.richclient.pinmanagement.controllers

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openecard.richclient.pinmanagement.model.CardSelectionModel
import org.openecard.richclient.pinmanagement.ui.PinUiFactory
import org.openecard.richclient.sc.CardWatcher

class CardSelectionController(
	private val model: CardSelectionModel,
	private val view: CardSelectionViewController,
	private val uiFactory: PinUiFactory,
	private val root: StackPane,
) {
	fun start() {
		model.registerWatcher(
			onUpdate = { view.updateTerminals(model.terminals) },
			onError = { message ->
				view.showErrorDialog(message) {
					model.selectedTerminal = null
					root.children.setAll(view.cardListLayout)
				}
			},
		)

		view.setup(model.terminals) { selected ->
			model.selectTerminal(selected)

			uiFactory.openPinUiForType(
				cardType = selected.cardType,
				terminal = selected,
				onError = { error ->
					view.showErrorDialog("Error: ${error.message}") {
						model.selectedTerminal = null
						root.children.setAll(view.cardListLayout)
					}
				},
				model = model,
			)
		}

		Platform.runLater {
			uiFactory.dialogStage.scene = Scene(root, 400.0, 350.0)
			uiFactory.dialogStage.title = "PIN Management"
			uiFactory.dialogStage.show()
		}
	}
}
