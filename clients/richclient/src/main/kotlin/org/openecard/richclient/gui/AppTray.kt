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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonManager
import org.openecard.common.AppVersion.name
import org.openecard.common.I18n
import org.openecard.common.interfaces.Environment
import org.openecard.common.util.SysUtils
import org.openecard.gui.graphics.*
import org.openecard.richclient.RichClient
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import kotlin.system.exitProcess

private val LOG = KotlinLogging.logger {  }

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
 */(private val client: RichClient) {

    private val lang: I18n = I18n.getTranslation("richclient")

    private var tray: SystemTray? = null
    private var trayIcon: TrayIcon? = null
    private var status: Status? = null
    private var frame: InfoFrame? = null
    private val isLinux: Boolean? = null
    private val isKde: Boolean by lazy {
		// KDE_FULL_SESSION contains true when KDE is running
		// The spec says (http://techbase.kde.org/KDE_System_Administration/Environment_Variables#KDE_FULL_SESSION)
		// If you plan on using this variable to detect a running KDE session, check if the value is not empty
		// instead of seeing if it equals true. The value might be changed in the future to include KDE version.
		val kdeSession: String? = System.getenv("KDE_FULL_SESSION")
		kdeSession != null && !kdeSession.isEmpty()
	}
    private var trayAvailable: Boolean = false

    /**
     * Starts the setup process.
     * A loading icon is displayed.
     */
    fun beginSetup() {
        if (isTraySupported) {
            setupTrayIcon()
        } else {
            setupFrame()
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
        status = Status(this, env, manager)

        if (trayAvailable) {
            trayIcon!!.image = getTrayIconImage(ICON_LOGO)
            trayIcon!!.toolTip = lang.translationForKey("tray.title", name)
        } else {
            frame!!.isVisible = false
            status!!.setInfoPanel(frame)

            frame!!.isResizable = false
            frame!!.setLocationRelativeTo(null)
            frame!!.state = Frame.ICONIFIED
            frame!!.isVisible = true
            frame!!.pack()
        }
    }

    /**
     * Returns the current status.
     *
     * @return current status
     */
    fun status(): Status? {
        return status
    }

    /**
     * Removes the tray icon from the tray and terminates the application.
     */
    fun shutdown() {
        if (trayAvailable) {
            if (!SysUtils.isMacOSX()) {
                val desc: String = lang.translationForKey("tray.message.shutdown", name)
                trayIcon!!.displayMessage(name, desc, TrayIcon.MessageType.INFO)
            }
            client.teardown()
            tray!!.remove(trayIcon)
        } else {
            client.teardown()
        }
		exitProcess(0)
    }


    private fun setupTrayIcon() {
        trayAvailable = true

        tray = SystemTray.getSystemTray()

        trayIcon = TrayIcon(
            getTrayIconImage(ICON_LOADER),
            lang.translationForKey("tray.message.loading", name), null
        )
        trayIcon!!.isImageAutoSize = true
        trayIcon!!.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (status != null) {
                    status!!.showInfo(Point(e.x, e.y))
                }
            }
        })

        try {
            tray?.add(trayIcon)
        } catch (ex: AWTException) {
			LOG.error(ex) { "TrayIcon could not be added to the system tray." }

            // tray and trayIcon are not needed anymore
            tray = null
            trayIcon = null
            setupFrame()
        }
    }


    private fun getTrayIconImage(name: String): Image {
        val dim: Dimension = tray!!.trayIconSize

        if (SysUtils.isUnix()) {
            return getImageLinux(name, dim)
        } else if (SysUtils.isMacOSX()) {
            return getImageMacOSX(name, dim)
        } else {
            return getImageDefault(name, dim)
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
		val c = if (isMacMenuBarDarkMode) {
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

    private val isMacMenuBarDarkMode: Boolean
        get() {
            // code inspired by https://stackoverflow.com/questions/33477294/menubar-icon-for-dark-mode-on-os-x-in-java
            val f: FutureTask<Int> =
                FutureTask({
                    // check for exit status only. Once there are more modes than "dark" and "default", we might need to
                    // analyze string contents..
                    val proc: Process = Runtime.getRuntime()
                        .exec(arrayOf("defaults", "read", "-g", "AppleInterfaceStyle"))
                    proc.waitFor()
                    proc.exitValue()
                })
            try {
                val t: Thread = Thread({
                    f.run()
                })
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

    private val isGnome: Boolean by lazy {
		"GNOME" == System.getenv("XDM_CURRENT_DESKTOP")
			|| "GNOME" == System.getenv("XDG_CURRENT_DESKTOP")
	}

    private val isPlasma: Boolean by lazy {
		if (isKde) {
			"5" == System.getenv("KDE_SESSION_VERSION")
		} else {
			false
		}
	}

    private val isTraySupported: Boolean
        get() {
            return SystemTray.isSupported()
                    && !isPlasma
                    && !isGnome
                    && !SysUtils.isMacOSX()
        }

    private fun setupFrame() {
        trayAvailable = false

        frame = InfoFrame(lang.translationForKey("tray.title", name))
        frame!!.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame!!.iconImage = GraphicsUtil.createImage(OecLogo::class.java, 256, 256)

        val logo: ImageIcon = ImageIcon(GraphicsUtil.createImage(OecLogo::class.java, 256, 256))
        val label: JLabel = JLabel(logo)
        val c: Container = frame!!.contentPane
        c.preferredSize = Dimension(logo.iconWidth, logo.iconHeight)
        c.background = Color.white
        c.add(label)

        frame!!.pack()
        frame!!.isResizable = false
        frame!!.setLocationRelativeTo(null)
        frame!!.isVisible = true
    }

    companion object {
        private const val ICON_LOADER: String = "loader"
        private const val ICON_LOGO: String = "logo"
    }
}
