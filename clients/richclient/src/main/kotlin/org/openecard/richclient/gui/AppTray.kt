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
import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.Separator
import dorkbox.systemTray.SystemTray
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonManager
import org.openecard.common.AppVersion.name
import org.openecard.common.util.SysUtils
import org.openecard.i18n.I18N
import org.openecard.richclient.RichClient
import org.openecard.richclient.gui.graphics.OecIconType
import org.openecard.richclient.gui.graphics.oecImage
import org.openecard.richclient.gui.manage.ManagementDialog
import org.openecard.richclient.sc.CardWatcher
import org.openecard.richclient.sc.CifDb
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.Frame
import java.awt.Image
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import kotlin.system.exitProcess

private val LOG = KotlinLogging.logger { }

private const val ICON_LOADER: String = "loader"
private const val ICON_LOGO: String = "logo"

/**
 * This class creates a tray icon on systems that do have a system tray.
 * Otherwise a normal window will be shown.
 *
 * @param client RichClient
 *
 * @author Moritz Horsch
 * @author Johannes Schmölz
 * @author Tobias Wich
 */
class AppTray(
	private val client: RichClient,
) {
	private var tray: SystemTray? = null
	var status: Status? = null
		private set
	private var frame: InfoFrame? = null
	private var infoPopupActive = false

	/**
	 * Starts the setup process.
	 * A loading icon is displayed.
	 */
	fun beginSetup() {
		tray =
			SystemTray
				.get(
					I18N.strings.richclient_tray_title
						.format(name)
						.localized(),
				)?.let { tray ->
					tray.setImage(tray.getTrayIconImage(ICON_LOADER))
					tray.status =
						I18N.strings.richclient_tray_message_loading
							.format(name)
							.localized()
					tray.setTooltip(
						I18N.strings.richclient_tray_title
							.format(name)
							.localized(),
					)

					tray
				}

		// no tray, set up frame
		if (tray == null) {
			frame = setupFrame(true)
		}
	}

	/**
	 * Finishes the setup process.
	 * The loading icon is replaced with the eCard logo.
	 *
	 * @param manager
	 */
	fun endSetup(
		cifDb: CifDb,
		manager: AddonManager,
		cardWatcher: CardWatcher,
	) {
		val statusObj =
			Status(
				this,
				manager,
				tray == null,
				cifDb,
			)

		status = statusObj
		statusObj.startCardWatcher(cardWatcher)

		tray?.let { tray ->
			tray.setImage(tray.getTrayIconImage(ICON_LOGO))
			tray.status = null
			tray.menu.add(
				MenuItem(
					I18N.strings.richclient_tray_card_status.localized(),
					object : ActionListener {
						override fun actionPerformed(e: ActionEvent) {
							if (!infoPopupActive) {
								val frame = setupFrame(false)
								frame.setStatusPane(statusObj)
								frame.addWindowListener(
									object : WindowAdapter() {
										override fun windowClosed(e: WindowEvent) {
											infoPopupActive = false
										}
									},
								)
								infoPopupActive = true
							}
						}
					},
				),
			)
			tray.menu.add(Separator())
			tray.menu.add(
				MenuItem(
					I18N.strings.richclient_tray_about.localized(),
					object : ActionListener {
						override fun actionPerformed(e: ActionEvent) {
							AboutDialog.showDialog()
						}
					},
				),
			)
			tray.menu.add(
				MenuItem(
					I18N.strings.richclient_tray_config.localized(),
					object : ActionListener {
						override fun actionPerformed(e: ActionEvent) {
							ManagementDialog.Companion.showDialog(manager)
						}
					},
				),
			)
			tray.menu.add(Separator())
			tray.menu.add(
				MenuItem(
					I18N.strings.richclient_tray_exit.localized(),
					object : ActionListener {
						override fun actionPerformed(e: ActionEvent) {
							shutdown()
						}
					},
				),
			)
		}

		frame?.setStatusPane(statusObj)
	}

	/**
	 * Removes the tray icon from the tray and terminates the application.
	 */
	fun shutdown() {
		status?.stopCardWatcher()
		tray?.shutdown()
		tray = null
		client.teardown()
		exitProcess(0)
	}

	private fun setupFrame(standalone: Boolean): InfoFrame =
		InfoFrame(
			I18N.strings.richclient_tray_title
				.format(name)
				.localized(),
		).also { frame ->
			frame.iconImage = oecImage(OecIconType.COLORED, 256, 256)

			if (standalone) {
				frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

				val logo = ImageIcon(oecImage(OecIconType.COLORED, 256, 256))
				val label = JLabel(logo)
				val c: Container = frame.contentPane
				c.preferredSize = Dimension(logo.iconWidth, logo.iconHeight)
				c.background = Color.white
				c.add(label)

				frame.pack()
				frame.isResizable = false
				frame.setLocationRelativeTo(null)
				frame.isVisible = true
			} else {
				frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
				frame.isVisible = false
			}
		}
}

private fun InfoFrame.setStatusPane(statusObj: Status) {
	this.isVisible = false
	statusObj.setInfoPanel(this)

	this.isResizable = false
	this.setLocationRelativeTo(null)
	this.state = Frame.ICONIFIED
	this.isVisible = true
	this.pack()
}

private fun SystemTray.getTrayIconImage(name: String): Image {
	val dim: Dimension = trayImageSize.let { Dimension(it, it) }

	return if (SysUtils.isUnix) {
		getImageLinux(name, dim)
	} else if (SysUtils.isMacOSX) {
		getImageMacOSX(name, dim)
	} else {
		getImageDefault(name, dim)
	}
}

private fun getImageLinux(
	name: String,
	dim: Dimension,
): Image =
	if (name == ICON_LOADER) {
		oecImage(OecIconType.GRAY, dim.width, dim.height)
	} else {
		oecImage(OecIconType.COLORED, dim.width, dim.height)
	}

private fun getImageMacOSX(
	name: String,
	dim: Dimension,
): Image {
	val kind =
		if (isMacMenuBarDarkMode()) {
			OecIconType.WHITE
		} else {
			OecIconType.BLACK
		}
	return oecImage(kind, dim.width, dim.height)
}

private fun getImageDefault(
	name: String,
	dim: Dimension,
): Image =
	if (name == ICON_LOADER) {
		oecImage(OecIconType.GRAY, dim.width, dim.height)
	} else {
		oecImage(OecIconType.COLORED, dim.width, dim.height)
	}

private fun isMacMenuBarDarkMode(): Boolean {
	// code inspired by https://stackoverflow.com/questions/33477294/menubar-icon-for-dark-mode-on-os-x-in-java
	val f: FutureTask<Int> =
		FutureTask {
			// check for exit status only. Once there are more modes than "dark" and "default", we might need to
			// analyze string contents.
			val proc: Process =
				Runtime
					.getRuntime()
					.exec(arrayOf("defaults", "read", "-g", "AppleInterfaceStyle"))
			proc.waitFor()
			proc.exitValue()
		}
	try {
		val t =
			Thread {
				f.run()
			}
		t.isDaemon = true
		t.start()
		val result: Int = f.get(100, TimeUnit.MILLISECONDS)
		return result == 0
	} catch (ex: InterruptedException) {
		// TimeoutException thrown if process didn't terminate
		LOG.warn(ex) { "Could not determine, whether 'dark mode' is being used. Falling back to default (light) mode." }
		f.cancel(true) // make sure the thread is dead
		return false
	} catch (ex: TimeoutException) {
		LOG.warn(ex) { "Could not determine, whether 'dark mode' is being used. Falling back to default (light) mode." }
		f.cancel(true)
		return false
	} catch (ex: ExecutionException) {
		LOG.warn(ex) { "Could not determine, whether 'dark mode' is being used. Falling back to default (light) mode." }
		f.cancel(true)
		return false
	}
}
