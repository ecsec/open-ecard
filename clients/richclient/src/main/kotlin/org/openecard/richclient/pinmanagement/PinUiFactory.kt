package org.openecard.richclient.pinmanagement

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.richclient.MR
import org.openecard.richclient.gui.GuiUtils.toFXImage
import org.openecard.richclient.pinmanagement.controllers.PlaceholderPinController
import org.openecard.richclient.pinmanagement.npa.NpaPacePinController
import org.openecard.richclient.pinmanagement.npa.NpaPacePinView
import org.openecard.richclient.pinmanagement.selection.CardSelectionController
import org.openecard.richclient.pinmanagement.selection.CardSelectionModel
import org.openecard.richclient.pinmanagement.selection.CardSelectionViewController
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith

class PinUiFactory(
	private val stage: Stage,
	private val cardWatcher: CardWatcher,
) {
	val supportedCardTypes: Set<String> = setOf(NpaDefinitions.cardType)
	val dialogStage: Stage get() = stage

	fun createSelectionUi(): CardSelectionController {
		val model = CardSelectionModel(supportedCardTypes, cardWatcher)
		val loader = FXMLLoader(javaClass.getResource("/fxml/CardSelectionView.fxml"))
		val root = loader.load<StackPane>()
		val viewController = loader.getController<CardSelectionViewController>()

		stage.icons.add(
			MR.images.oec_logo.image
				.toFXImage(),
		)

		return CardSelectionController(model, viewController, this, root)
	}

	fun openPinUiForType(
		cardType: String,
		terminal: TerminalInfo,
		onError: (Throwable) -> Unit,
		model: CardSelectionModel,
	) {
		try {
			val view = createPinView()
			val controller: PinManagementUI =
				when (cardType) {
					NpaDefinitions.cardType -> NpaPacePinController(terminal, view)
					else -> PlaceholderPinController(terminal, view)
				}

			// watch card for removal event
			// TODO: remove callback from watcher, when selection UI is finished (e.g. cancel job)
			val terminalWatchJob =
				object : CardWatcherCallback.CardWatcherCallbackDefault() {
					override fun onCardRemoved(terminalName: String) {
						if (terminal.terminalName == terminalName) {
							Platform.runLater {
								view.showMessage("The selected card or card terminal has been removed.") {
									val controller = createSelectionUi()
									model.selectedTerminal = null
									controller.start()
								}
							}
						}
					}
				}.registerWith(cardWatcher)

			Platform.runLater {
				controller.show()
			}
		} catch (e: Exception) {
			Platform.runLater {
				onError(e)
			}
		}
	}

	fun createPinView(): NpaPacePinView = NpaPacePinView(stage)

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
