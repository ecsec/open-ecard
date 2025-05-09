/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

import dorkbox.systemTray.MenuItem
import dorkbox.systemTray.Separator
import dorkbox.systemTray.SystemTray
import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonManager
import org.openecard.common.AppVersion.name
import org.openecard.common.I18n
import org.openecard.common.interfaces.Environment
import org.openecard.common.util.SysUtils
import org.openecard.gui.about.AboutDialog
import org.openecard.gui.graphics.*
import org.openecard.richclient.RichClient
import org.openecard.richclient.gui.manage.ManagementDialog
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JSeparator
import kotlin.system.exitProcess

private val LOG = KotlinLogging.logger {  }

private const val ICON_LOADER: String = "loader"
private const val ICON_LOGO: String = "logo"

/**
 * This class creates a tray icon on systems that do have a system tray.
 * Otherwise a normal window will be shown.
 *
 * @author Moritz Horsch
 * @author Johannes Schmölz
 * @author Tobias Wich
 */
class AppTray
	/**
	 * Constructor of AppTray class.
	 *
	 * @param client RichClient
	 */
	(private val client: RichClient) {

    private val lang: I18n = I18n.getTranslation("richclient")

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
		tray = SystemTray.get(lang.translationForKey("tray.title", name))?.let { tray ->
			tray.setImage(tray.getTrayIconImage(ICON_LOADER))
			tray.status = lang.translationForKey("tray.message.loading", name)
			tray.setTooltip(lang.translationForKey("tray.title", name))

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
     * @param env
     * @param manager
     */
    fun endSetup(env: Environment, manager: AddonManager) {
		val statusObj = Status(this, env, manager, tray == null)
		status = statusObj

		tray?.let { tray ->
			tray.setImage(tray.getTrayIconImage(ICON_LOGO))
			tray.status = null
			tray.menu.add(MenuItem("Card Status", object : ActionListener {
				override fun actionPerformed(e: ActionEvent) {
					if (!infoPopupActive) {
						val frame = setupFrame(false)
						frame.setStatusPane(statusObj)
						frame.addWindowListener(object : WindowAdapter() {
							override fun windowClosed(e: WindowEvent) {
								infoPopupActive = false
							}
						})
						infoPopupActive = true
					}
				}
			}))
			tray.menu.add(Separator())
			tray.menu.add(MenuItem(lang.translationForKey("tray.about"), object : ActionListener {
				override fun actionPerformed(e: ActionEvent) {
					AboutDialog.showDialog()
				}
			}))
			tray.menu.add(MenuItem(lang.translationForKey("tray.config"), object : ActionListener {
				override fun actionPerformed(e: ActionEvent) {
					ManagementDialog.Companion.showDialog(manager)
				}
			}))
			tray.menu.add(Separator())
			tray.menu.add(MenuItem(lang.translationForKey("tray.exit"), object : ActionListener {
				override fun actionPerformed(e: ActionEvent) {
					shutdown()
				}
			}))

        }

		frame?.setStatusPane(statusObj)
    }

    /**
     * Removes the tray icon from the tray and terminates the application.
     */
    fun shutdown() {
		tray?.shutdown()
		tray = null
		client.teardown()
		exitProcess(0)
    }


	private fun setupFrame(standalone: Boolean): InfoFrame {
		return InfoFrame(lang.translationForKey("tray.title", name)).also { frame ->
			frame.iconImage = GraphicsUtil.createImage(OecLogo::class.java, 256, 256)

			if (standalone) {
				frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

				val logo = ImageIcon(GraphicsUtil.createImage(OecLogo::class.java, 256, 256))
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

private fun getImageLinux(name: String, dim: Dimension): Image {
	return if (name == ICON_LOADER) {
		GraphicsUtil.createImage(OecLogoLoading::class.java, dim.width, dim.height)
	} else {
		GraphicsUtil.createImage(OecLogo::class.java, dim.width, dim.height)
	}
}

private fun getImageMacOSX(name: String, dim: Dimension): Image {
	val c = if (isMacMenuBarDarkMode()) {
		OecLogoWhite::class.java
	} else {
		OecLogoBlack::class.java
	}
	return GraphicsUtil.createImage(c, dim.width - 2, dim.height - 2, dim.width, dim.height, 1, 1)
}

private fun getImageDefault(name: String, dim: Dimension): Image {
	return if (name == ICON_LOADER) {
		GraphicsUtil.createImage(OecLogoLoading::class.java, dim.width, dim.height)
	} else {
		GraphicsUtil.createImage(OecLogo::class.java, dim.width, dim.height)
	}
}

private fun isMacMenuBarDarkMode(): Boolean {
	// code inspired by https://stackoverflow.com/questions/33477294/menubar-icon-for-dark-mode-on-os-x-in-java
	val f: FutureTask<Int> =
		FutureTask {
			// check for exit status only. Once there are more modes than "dark" and "default", we might need to
			// analyze string contents.
			val proc: Process = Runtime.getRuntime()
				.exec(arrayOf("defaults", "read", "-g", "AppleInterfaceStyle"))
			proc.waitFor()
			proc.exitValue()
		}
	try {
		val t = Thread {
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
