/****************************************************************************
 * Copyright (C) 2012-2025 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.richclient.gui

import dev.icerock.moko.resources.format
import io.github.oshai.kotlinlogging.KotlinLogging
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.stage.Stage
import javafx.stage.WindowEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.openecard.addon.AddonManager
import org.openecard.cif.bundled.CompleteTree
import org.openecard.cif.definition.recognition.removeUnsupported
import org.openecard.common.AppVersion.name
import org.openecard.common.interfaces.Environment
import org.openecard.i18n.I18N
import org.openecard.richclient.CifDb
import org.openecard.richclient.MR
import org.openecard.richclient.PcscCardWatcher
import org.openecard.richclient.PcscCardWatcherCallbacks
import org.openecard.richclient.gui.manage.ManagementDialog
import org.openecard.richclient.gui.update.UpdateWindow
import org.openecard.richclient.updater.VersionUpdateChecker
import org.openecard.sal.sc.recognition.DirectCardRecognition
import org.openecard.sc.pcsc.PcscTerminalFactory
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.ConcurrentSkipListMap
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.Icon
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import javax.swing.border.BevelBorder

private val LOG = KotlinLogging.logger { }

/**
 * This class collects all events published by the EventManager in order to reproduce the current status including the
 * connected card terminals and available cards. It is also responsible for creating the InfoPopup which displays the
 * collected information.
 *
 * @author Johannes Schmölz
 * @author Tobias Wich
 * @author Sebastian Schuberth
 */
class Status(
	private val appTray: AppTray,
	private val manager: AddonManager,
	private val withControls: Boolean,
	private val cifDb: CifDb,
) {
	private val infoMap: MutableMap<String, JPanel> = ConcurrentSkipListMap()
	private var contentPane: JPanel? = null
	private var infoView: JPanel? = null
	private var noTerminal: JPanel? = null
	private var popup: StatusContainer? = null
	private var uw: UpdateWindow? = null
	private var gradPanel: GradientPanel? = null
	private var updateLabel: JLabel? = null

	/**
	 * Constructor of Status class.
	 *
	 * @param appTray tray icon
	 * @param env The environment object.
	 * @param manager
	 */
	init {
		setupBaseUI()
	}

	/**
	 * Shows the InfoPopup at the specified position p or at the default if no position is given.
	 */
	@JvmOverloads
	fun showInfo(p: Point? = null) {
		if (popup != null && popup is Window) {
			(popup as Window).dispose()
		}
		popup = InfoPopup(contentPane!!, p)
	}

	fun setInfoPanel(frame: StatusContainer?) {
		popup = frame
		popup?.contentPane = contentPane
	}

	private fun setupBaseUI() {
		contentPane = JPanel()
		contentPane!!.layout = BorderLayout()
		contentPane!!.background = Color.white
		contentPane!!.border =
			BorderFactory.createBevelBorder(
				BevelBorder.RAISED,
				Color.LIGHT_GRAY,
				Color.DARK_GRAY,
			)

		noTerminal = JPanel()
		noTerminal!!.layout = FlowLayout(FlowLayout.LEFT)
		noTerminal!!.background = Color.white
		noTerminal!!.add(createInfoLabel())
		infoMap[NO_TERMINAL_CONNECTED] = noTerminal!!

		infoView = JPanel()
		infoView!!.layout = BoxLayout(infoView, BoxLayout.PAGE_AXIS)
		infoView!!.background = Color.white
		infoView!!.add(Box.createRigidArea(Dimension(0, 5)))
		infoView!!.add(noTerminal)

		val label =
			JLabel(
				" ${I18N.strings.richclient_tray_title.format(name).localized()} ",
			)
		label.font = Font(Font.SANS_SERIF, Font.BOLD, 16)
		label.horizontalAlignment = SwingConstants.CENTER

		gradPanel = GradientPanel(Color(106, 163, 213), Color(80, 118, 177))
		gradPanel!!.layout = BorderLayout()

		gradPanel!!.isOpaque = false
		gradPanel!!.add(label, BorderLayout.CENTER)

		val btnPanel = JPanel()
		btnPanel.layout = FlowLayout(FlowLayout.RIGHT)
		btnPanel.background = Color.white

		val btnExit =
			JButton(
				I18N.strings.richclient_tray_exit.localized(),
			)
		btnExit.addActionListener {
			LOG.debug { "Shutdown button pressed." }
			try {
				appTray.shutdown()
			} catch (ex: Throwable) {
				LOG.error(ex) { "Exiting client threw an error." }
				throw ex
			}
		}

		val btnAbout =
			JButton(
				I18N.strings.richclient_tray_about.localized(),
			)
		btnAbout.addActionListener {
			LOG.debug { "About button pressed." }
			try {
				AboutDialog.showDialog()
			} catch (ex: Throwable) {
				LOG.error(ex) { "Show About dialog threw an error." }
				throw ex
			}
		}

		val btnSettings =
			JButton(
				I18N.strings.richclient_tray_config.localized(),
			)
		btnSettings.addActionListener {
			LOG.debug { "Settings button pressed." }
			try {
				ManagementDialog.Companion.showDialog(manager)
			} catch (ex: Throwable) {
				LOG.error(ex) { "Show Settings dialog threw an error." }
				throw ex
			}
		}

		btnPanel.add(btnSettings)
		btnPanel.add(btnAbout)
		btnPanel.add(btnExit)

		contentPane!!.add(gradPanel!!, BorderLayout.NORTH)
		contentPane!!.add(infoView!!, BorderLayout.CENTER)
		if (withControls) {
			contentPane!!.add(btnPanel, BorderLayout.SOUTH)
		}
	}

	@Synchronized
	private fun addInfo(
		ifdName: String,
		cardType: String?,
	) {
		if (infoMap.containsKey(NO_TERMINAL_CONNECTED)) {
			infoMap.remove(NO_TERMINAL_CONNECTED)
			infoView!!.removeAll()
		}

		// only add if there is no terminal with an identical name already
		if (infoMap.containsKey(ifdName)) {
			return
		}

		val panel = JPanel()
		panel.layout = FlowLayout(FlowLayout.LEFT)
		panel.add(createInfoLabel(ifdName, cardType))
		infoMap[ifdName] = panel
		infoView!!.add(panel)

		if (popup != null) {
			popup!!.updateContent(contentPane!!)
		}
	}

	@Synchronized
	private fun updateInfo(
		ifdName: String,
		cardType: String?,
	) {
		val panel = infoMap[ifdName]
		if (panel != null) {
			panel.removeAll()
			panel.add(createInfoLabel(ifdName, cardType))
			panel.repaint()

			if (popup != null) {
				popup!!.updateContent(contentPane!!)
			}
		}
	}

	@Synchronized
	private fun removeInfo(ifdName: String) {
		val panel = infoMap[ifdName]
		if (panel != null) {
			infoMap.remove(ifdName)
			infoView!!.remove(panel)

			if (infoMap.isEmpty()) {
				infoMap[NO_TERMINAL_CONNECTED] = noTerminal!!
				infoView!!.add(noTerminal)
			}

			if (popup != null) {
				popup!!.updateContent(contentPane!!)
			}
		}
	}

	private fun getCardIcon(cardType: String): ImageIcon {
		val img = cifDb.getCardImage(cardType)
		val icon = GuiUtils.getScaledCardImageIcon(img)
		return icon
	}

	private fun createInfoLabel(
		ifdName: String? = null,
		cardType: String? = null,
	): JLabel {
		val label = JLabel()

		if (ifdName != null) {
			val cardType = cardType ?: CifDb.NO_CARD
			label.icon = getCardIcon(cardType)
			label.text = "<html><b>" + cifDb.getCardType(cardType) + "</b><br><i>" + ifdName + "</i></html>"
		} else {
			// no_terminal.svg is based on klaasvangend_USB_plug.svg by klaasvangend
			// see: http://openclipart.org/detail/3705/usb-plug-by-klaasvangend
			val cardType = CifDb.NO_TERMINAL
			label.icon = getCardIcon(cardType)
			label.text = "<html><i>" + cifDb.getCardType(cardType) + "</i></html>"
		}

		label.iconTextGap = 10
		label.background = Color.white

		// on Windows the label width is too small to display all information
		val dim = label.preferredSize
		label.preferredSize = Dimension(dim.width + 10, dim.height)

		return label
	}

	lateinit var watcher: PcscCardWatcher

	fun startCardWatcher() {
		val recognizeCard =
			if (cifDb.supportedCardTypes.isNotEmpty()) {
				DirectCardRecognition(CompleteTree.calls.removeUnsupported(cifDb.supportedCardTypes))
			} else {
				DirectCardRecognition(CompleteTree.calls)
			}

		val scope = CoroutineScope(Dispatchers.Default)

		val factory = PcscTerminalFactory.instance

		val callbacks =
			object : PcscCardWatcherCallbacks {
				override fun onTerminalAdded(terminalName: String) {
					addInfo(terminalName, null)
				}

				override fun onTerminalRemoved(terminalName: String) {
					removeInfo(terminalName)
					updateInfo(terminalName, null)
				}

				override fun onCardInserted(terminalName: String) {
					addInfo(terminalName, null)
					updateInfo(terminalName, null)
				}

				override fun onCardRecognized(
					terminalName: String,
					cardType: String?,
				) {
					addInfo(terminalName, cardType)
					updateInfo(terminalName, cardType)
				}

				override fun onCardRemoved(terminalName: String) {
					updateInfo(terminalName, null)
				}
			}

		watcher = PcscCardWatcher(callbacks, scope, recognizeCard, factory)
		watcher.start()
	}

	fun stopCardWatcher() {
		watcher.stop()
	}

	fun showUpdateIcon(checker: VersionUpdateChecker) {
		if (updateLabel != null) {
			gradPanel!!.remove(updateLabel)
		}

		val img = GuiUtils.getImage("update_available.png")
		val resizedImage = img.getScaledInstance(40, 40, 0)
		val icon: Icon = ImageIcon(resizedImage)
		updateLabel = JLabel(icon)

		updateLabel!!.addMouseListener(
			object : MouseAdapter() {
				override fun mouseClicked(e: MouseEvent) {
					openUpdateWindow(checker)
				}
			},
		)

		gradPanel!!.add(updateLabel!!, BorderLayout.EAST)

		if (popup != null) {
			popup!!.updateContent(contentPane!!)
		}
	}

	@Synchronized
	private fun openUpdateWindow(checker: VersionUpdateChecker) {
		if (uw == null) {
			// no window displayed, start it up

			Platform.runLater {
				val stage = Stage()
				stage.onHidden =
					EventHandler { event: WindowEvent? ->
						synchronized(this) {
							uw = null
						}
					}
				uw = UpdateWindow(checker, stage)
				uw!!.init()
			}
		} else {
			// window is already displayed, just bring it to the front
			Platform.runLater { uw!!.toFront() }
		}
	}
}

private const val NO_TERMINAL_CONNECTED = "noTerminalConnected"
