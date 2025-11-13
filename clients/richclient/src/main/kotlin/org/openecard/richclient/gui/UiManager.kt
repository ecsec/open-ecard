package org.openecard.richclient.gui

import javafx.application.Platform
import org.openecard.richclient.gui.manage.ManagementDialog
import org.openecard.richclient.pinmanagement.PinManager
import org.openecard.richclient.sc.CardWatcher
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent

class UiManager(
	val cardWatcher: CardWatcher,
) {
	private var pinManager: PinManager? = null
	private var about: AboutDialog? = null
	private var settings: ManagementDialog? = null

	@Synchronized
	fun showPinManager() {
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

	@Synchronized
	fun closePinManager() {
		pinManager?.let {
			Platform.runLater {
				it.closeManagementDialog()
			}
			pinManager = null
		}
	}

	@Synchronized
	fun showAboutDialog() {
		when (val dialog = about) {
			null -> {
				about =
					AboutDialog.createDialog().apply {
						addWindowListener(
							object : WindowAdapter() {
								override fun windowClosed(e: WindowEvent?) {
									about = null
								}
							},
						)
						isVisible = true
					}
			}
			else -> {
				dialog.toFront()
			}
		}
	}

	@Synchronized
	fun closeAboutDialog() {
		about?.let {
			it.isVisible = false
			about = null
		}
	}

	@Synchronized
	fun showSettingsDialog() {
		when (val dialog = settings) {
			null -> {
				settings =
					ManagementDialog.createDialog().apply {
						addWindowListener(
							object : WindowAdapter() {
								override fun windowClosed(e: WindowEvent?) {
									about = null
								}
							},
						)
						isVisible = true
					}
			}
			else -> {
				dialog.toFront()
			}
		}
	}

	@Synchronized
	fun closeSettingsDialog() {
		settings?.let {
			it.isVisible = false
			settings = null
		}
	}

	fun shutdown() {
		closePinManager()
		closeAboutDialog()
		closeSettingsDialog()
	}
}
