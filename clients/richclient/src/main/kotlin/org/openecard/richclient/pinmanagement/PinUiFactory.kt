package org.openecard.richclient.pinmanagement

import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.richclient.MR
import org.openecard.richclient.gui.JfxUtils.toJfxImage
import org.openecard.richclient.pinmanagement.common.MessageController
import org.openecard.richclient.pinmanagement.npa.NpaPinController
import org.openecard.richclient.pinmanagement.selection.CardSelectionController
import org.openecard.richclient.pinmanagement.selection.CardSelectionModel
import org.openecard.richclient.pinmanagement.unsupported.PlaceholderPinController
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith

class PinUiFactory(
	stage: Stage,
	private val cardWatcher: CardWatcher,
	private val bgTaskScope: CoroutineScope,
) {
	private val supportedCardTypes: Set<String> = setOf(NpaDefinitions.cardType)

	private val pmStage = PinManagementStage(stage)

	init {
		stage.icons.add(
			MR.images.oec_logo.image
				.toJfxImage(),
		)
	}

	fun createSelectionUi(): CardSelectionController {
		val model = CardSelectionModel(supportedCardTypes, cardWatcher, bgTaskScope)
		return CardSelectionController(model, this, pmStage, bgTaskScope)
	}

	fun openPinUiForType(terminal: TerminalInfo) {
		val controller: PinManagementUI =
			when (terminal.cardType) {
				NpaDefinitions.cardType -> NpaPinController(terminal, pmStage, bgTaskScope)
				else -> PlaceholderPinController(terminal, pmStage, bgTaskScope)
			}

		// watch card for removal event
		// run callback in task scope, so it gets removed when we are finished
		object : CardWatcherCallback.CardWatcherCallbackDefault() {
			override fun onCardRemoved(terminalName: String) {
				if (terminal.terminalName == terminalName) {
					Platform.runLater {
						val msgController = MessageController(pmStage, bgTaskScope)
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
	}
}
