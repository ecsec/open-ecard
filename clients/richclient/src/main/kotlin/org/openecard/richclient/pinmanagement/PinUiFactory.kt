package org.openecard.richclient.pinmanagement

import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Platform
import javafx.stage.Stage
import kotlinx.coroutines.CoroutineScope
import org.openecard.cif.bundled.NpaDefinitions
import org.openecard.richclient.gui.JfxUtils.toJfxImage
import org.openecard.richclient.pinmanagement.common.MessageController
import org.openecard.richclient.pinmanagement.npa.NpaPinController
import org.openecard.richclient.pinmanagement.selection.CardSelectionController
import org.openecard.richclient.pinmanagement.selection.CardSelectionModel
import org.openecard.richclient.res.MR
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CardWatcherCallback
import org.openecard.richclient.sc.CardWatcherCallback.Companion.registerWith

private val log = KotlinLogging.logger { }

class PinUiFactory(
	stage: Stage,
	private val cardWatcher: CardWatcher,
	private val bgTaskScope: CoroutineScope,
) {
	private val supportedCardTypes: Set<String> = setOf(NpaDefinitions.cardType)

	private val pmStage = PinManagementStage(stage)

	private var activeController: PinManagementUI? = null

	init {
		stage.icons.add(
			MR.images.oec_logo.image
				.toJfxImage(),
		)
	}

	fun openSelectionUi() {
		if (activeController == null) {
			// when going back to the selection view, always destroy the active controller
			closeActiveController()
		}

		val model = CardSelectionModel(supportedCardTypes, cardWatcher, bgTaskScope)
		val controller = CardSelectionController(model, this, pmStage, bgTaskScope)
		controller.start()
	}

	fun openPinUiForType(terminal: TerminalInfo) {
		if (activeController == null) {
			log.warn { "There is an uncleaned active controller, removing it before creating a new one" }
			closeActiveController()
		}

		val controller: PinManagementUI =
			when (terminal.cardType) {
				NpaDefinitions.cardType -> NpaPinController(terminal, pmStage, bgTaskScope)
				else -> {
					// make sure the dialog is closed and cleanup runs
					MessageController(pmStage, bgTaskScope).showErrorDialog(
						MR.strings.pinmanage_message_unknown_status.localized(),
					) {
						pmStage.stage.close()
					}
					// then signal an error that the application is fucked
					throw IllegalArgumentException("PIN Management UI allowed to select an unsupported card type")
				}
			}

		// watch card for removal event
		// run callback in task scope, so it gets removed when we are finished
		object : CardWatcherCallback.CardWatcherCallbackDefault() {
			override fun onCardRemoved(terminalName: String) {
				if (terminal.terminalName == terminalName) {
					closeActiveController()
					Platform.runLater {
						val msgController = MessageController(pmStage, bgTaskScope)
						msgController.showMessage(
							MR.strings.pinmanage_message_card_removed.localized(),
						) {
							openSelectionUi()
						}
					}
				}
			}
		}.registerWith(cardWatcher, bgTaskScope)

		controller.show()
		activeController = controller
	}

	fun closeActiveController() {
		activeController?.closeProcess()
		activeController = null
	}
}
