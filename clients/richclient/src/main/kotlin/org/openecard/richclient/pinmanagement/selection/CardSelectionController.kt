package org.openecard.richclient.pinmanagement.selection

import javafx.scene.Parent
import kotlinx.coroutines.CoroutineScope
import org.openecard.richclient.gui.JfxUtils
import org.openecard.richclient.pinmanagement.PinManagementStage
import org.openecard.richclient.pinmanagement.PinUiFactory
import org.openecard.richclient.pinmanagement.common.MessageController

class CardSelectionController(
	private val model: CardSelectionModel,
	private val uiFactory: PinUiFactory,
	private val stage: PinManagementStage,
	bgTaskScope: CoroutineScope,
) {
	private val msgCtl = MessageController(stage, bgTaskScope)
	private val view: Parent
	private val viewCtl: CardSelectionViewController

	init {
		val (view, viewCtl) = JfxUtils.loadFxml<Parent, CardSelectionViewController>("CardSelection.fxml")
		this.view = view
		this.viewCtl = viewCtl
	}

	fun start() {
		model.registerWatcher(
			onUpdate = { viewCtl.updateTerminals(model.terminals) },
			onError = { message ->
				msgCtl.showErrorDialog(message) {
					stage.replaceView(view)
				}
			},
		)

		viewCtl.setup(model.terminals) { selected ->
			uiFactory.openPinUiForType(
				terminal = selected,
				onError = { error ->
					msgCtl.showErrorDialog("Error: ${error.message}") {
						stage.replaceView(view)
					}
				},
			)
		}

		stage.showScene(PinManagementStage.makeScene(view))
	}
}
