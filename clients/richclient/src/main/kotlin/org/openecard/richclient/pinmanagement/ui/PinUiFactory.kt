package org.openecard.richclient.pinmanagement.ui

import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.layout.StackPane
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.richclient.MR.images.oec_logo
import org.openecard.richclient.pinmanagement.TerminalInfo
import org.openecard.richclient.pinmanagement.controllers.CardSelectionController
import org.openecard.richclient.pinmanagement.controllers.CardSelectionViewController
import org.openecard.richclient.pinmanagement.controllers.NpaPacePinController
import org.openecard.richclient.pinmanagement.controllers.PlaceholderPinController
import org.openecard.richclient.pinmanagement.model.CardSelectionModel
import org.openecard.richclient.pinmanagement.ui.NpaPacePinView
import org.openecard.richclient.pinmanagement.util.toFXImage
import org.openecard.richclient.sc.CardState
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith

class PinUiFactory(
	private val stage: Stage,
	private val cardWatcher: CardWatcher,
) {
	val supportedCardTypes: Set<String> = setOf(NpaDefinitions.cardType)
	val dialogStage: Stage get() = stage

	fun openSelectionUi(): CardSelectionController {
		val model = CardSelectionModel(supportedCardTypes, cardWatcher)
		val loader = FXMLLoader(javaClass.getResource("/fxml/CardSelectionView.fxml"))
		val root = loader.load<StackPane>()
		val viewController = loader.getController<CardSelectionViewController>()

		stage.icons.add(oec_logo.image.toFXImage())

		return CardSelectionController(model, viewController, this, root, cardWatcher)
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

			val callback =
				object : CardWatcherCallback {
					override fun onInitialState(cardState: CardState) {}

					override fun onCardRecognized(
						terminalName: String,
						cardType: String,
					) {
					}

					override fun onCardInserted(terminalName: String) {}

					override fun onTerminalAdded(terminalName: String) {}

					override fun onCardRemoved(terminalName: String) {
						if (terminal.terminalName == terminalName) {
							Platform.runLater {
								view.showMessage("The selected card or card terminal has been removed.") {
									val controller = openSelectionUi()
									model.selectedTerminal = null
									controller.start()
								}
							}
						}
					}

					override fun onTerminalRemoved(terminalName: String) {
						if (terminal.terminalName == terminalName) {
							Platform.runLater {
								view.showMessage("The selected card or card terminal has been removed.") {
									val controller = openSelectionUi()
									model.selectedTerminal = null
									controller.start()
								}
							}
						}
					}
				}
			callback.registerWith(cardWatcher, CoroutineScope(Dispatchers.IO))

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
