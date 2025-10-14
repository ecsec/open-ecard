package org.openecard.richclient.pinmanagement

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.richclient.MR
import org.openecard.richclient.gui.GuiUtils.toFXImage
import org.openecard.richclient.pinmanagement.common.MessageController
import org.openecard.richclient.pinmanagement.npa.NpaPinController
import org.openecard.richclient.pinmanagement.selection.CardSelectionController
import org.openecard.richclient.pinmanagement.selection.CardSelectionModel
import org.openecard.richclient.pinmanagement.selection.CardSelectionViewController
import org.openecard.richclient.pinmanagement.unsupported.PlaceholderPinController
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith

class PinUiFactory(
	private val stage: Stage,
	private val cardWatcher: CardWatcher,
	private val bgTaskScope: CoroutineScope,
) {
	val supportedCardTypes: Set<String> = setOf(NpaDefinitions.cardType)
	val dialogStage: Stage get() = stage

	fun createSelectionUi(): CardSelectionController {
		val model = CardSelectionModel(supportedCardTypes, cardWatcher, bgTaskScope)
		val loader = FXMLLoader(javaClass.getResource("/fxml/CardSelectionView.fxml"))
		val root = loader.load<StackPane>()
		val viewController = loader.getController<CardSelectionViewController>()

		stage.icons.add(
			MR.images.oec_logo.image
				.toFXImage(),
		)

		return CardSelectionController(model, viewController, this, root, bgTaskScope)
	}

	fun openPinUiForType(
		terminal: TerminalInfo,
		onError: (Throwable) -> Unit,
	) {
		try {
			val stage = PinManagementStage(stage, bgTaskScope)
			val controller: PinManagementUI =
				when (terminal.cardType) {
					NpaDefinitions.cardType -> NpaPinController(terminal, stage, bgTaskScope)
					else -> PlaceholderPinController(terminal, stage, bgTaskScope)
				}

			// watch card for removal event
			// run callback in task scope, so it gets removed when we are finished
			object : CardWatcherCallback.CardWatcherCallbackDefault() {
				override fun onCardRemoved(terminalName: String) {
					if (terminal.terminalName == terminalName) {
						Platform.runLater {
							val msgController = MessageController(stage.rootPane, bgTaskScope)
							msgController.showMessage("The selected card or card terminal has been removed.") {
								val controller = createSelectionUi()
								controller.start()
							}
						}
					}
				}
			}.registerWith(cardWatcher, bgTaskScope)

			Platform.runLater {
				controller.show()
			}
		} catch (e: Exception) {
			Platform.runLater {
				onError(e)
			}
		}
	}

	fun closeStage() {
		stage.close()
		Platform.runLater {
			stage.scene?.root?.let {
				if (it is StackPane) it.children.clear()
			}
			stage.close()
		}
	}
}
