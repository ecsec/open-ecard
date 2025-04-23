/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.gui.swing

import org.openecard.common.OpenecardProperties
import org.openecard.gui.swing.common.GUIDefaults
import java.awt.Container
import java.awt.DisplayMode
import java.awt.GraphicsDevice
import java.awt.GraphicsEnvironment
import javax.swing.JFrame
import javax.swing.JRootPane

/**
 *
 * @author Moritz Horsch
 */
class SwingDialogWrapper {
	private lateinit var screenDevice: GraphicsDevice
	val dialog: JFrame by lazy {
		JFrame().apply {
			setTitle(title)
			setSize(690, 500)
			setLocationRelativeTo(null)
			iconImage = GUIDefaults.getImage("Frame.icon", 45, 45)!!.getImage()
			isVisible = false
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE)
		}
	}
	private var title: String? = null

	constructor() {
		// Initialize Look and Feel
		GUIDefaults.initialize()
	}

	private constructor(other: SwingDialogWrapper) {
		this.title = other.title
	}

	val rootPane: JRootPane
		get() = dialog.getRootPane()

	val getContentPane: Container
		get() = dialog.contentPane

	/**
	 * Set title of the user consent dialog.
	 *
	 * @param title Title to set in the dialog.
	 */
	fun setTitle(title: String?) {
		this.title = title
	}

	fun setSize(
		width: Int,
		height: Int,
	) {
		dialog.setSize(width, height)
	}

	/**
	 * This function is executed after the root panel has been set up with the contents of the user consent.
	 */
	fun show() {
		screenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
		var fsMode: DisplayMode? = null
		if (screenDevice.isFullScreenSupported && needsFullscreen()) {
			// check if resolution should be changed
			if (screenDevice.isDisplayChangeSupported && this.isChangeResolution) {
				fsMode = this.bestFullscreenMode
			}

			// change mode if it is supported
			if (fsMode != null) {
				dialog.isUndecorated = true
				screenDevice.setFullScreenWindow(dialog)
				screenDevice.displayMode = fsMode
			} else {
				dialog.isUndecorated = true
				screenDevice.setFullScreenWindow(dialog)
			}
		} else {
			dialog.isUndecorated = false
		}

		dialog.isVisible = true
		dialog.toFront()
		dialog.requestFocus()
		dialog.setAlwaysOnTop(true)
	}

	/**
	 * This function is executed after the user consent is finished or canceled.
	 */
	fun hide() {
		dialog.isVisible = false
		screenDevice.setFullScreenWindow(null)
	}

	fun derive(): SwingDialogWrapper = SwingDialogWrapper(this)

	private val isChangeResolution: Boolean
		get() = false

	private val bestFullscreenMode: DisplayMode?
		get() {
			var i = 0
			for (mode in screenDevice.displayModes) {
				System.out.printf(
					"Mode-%d: %dx%d@%d %dHz%n",
					i++,
					mode.width,
					mode.height,
					mode.bitDepth,
					mode.refreshRate,
				)
			}

			return screenDevice.displayModes[0]
		}

	companion object {
		private const val FULLSCREEN_USER_CONSENT = "display_fullscreen_uc"

		fun needsFullscreen(): Boolean {
			val fsStr = OpenecardProperties.getProperty(FULLSCREEN_USER_CONSENT)
			return fsStr.toBoolean()
		}
	}
}
