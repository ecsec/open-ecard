package org.openecard.richclient.pinmanagement.selection

import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import org.openecard.i18n.I18N
import org.openecard.richclient.pinmanagement.PinUiFactory

class CardSelectionController(
	private val model: CardSelectionModel,
	private val view: CardSelectionViewController,
	private val uiFactory: PinUiFactory,
	private val root: StackPane,
	private val bgTaskScope: CoroutineScope,
) {
	fun start() {
		model.registerWatcher(
			onUpdate = { view.updateTerminals(model.terminals) },
			onError = { message ->
				view.showErrorDialog(message, bgTaskScope) {
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
					view.showErrorDialog("Error: ${error.message}", bgTaskScope) {
						model.selectedTerminal = null
						root.children.setAll(view.cardListLayout)
					}
				},
				model = model,
			)
		}

		Platform.runLater {
			uiFactory.dialogStage.scene = Scene(root, 400.0, 350.0)
			uiFactory.dialogStage.title = I18N.strings.pinplugin_name.localized()
			uiFactory.dialogStage.show()
		}
	}
}
