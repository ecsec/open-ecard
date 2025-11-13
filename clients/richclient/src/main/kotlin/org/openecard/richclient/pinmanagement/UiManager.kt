package org.openecard.richclient.pinmanagement

import javafx.application.Platform
import org.openecard.richclient.sc.CardWatcher

class UiManager(
	val cardWatcher: CardWatcher,
) {
	private var pinManager: PinManager? = null

	fun showDialog() {
		when (val pm = pinManager) {
			null -> {
				Platform.runLater {
					pinManager =
						PinManager.create(cardWatcher).also { manager ->
							manager.addOnCloseHandler {
								pinManager = null
							}
							manager.openManagerDialog()
						}
				}
			}

			else -> {
				pm.toFront()
			}
		}
	}

	fun closeDialog() {
		Platform.runLater {
			pinManager?.closeManagementDialog()
			pinManager = null
		}
	}
}
