/*
 * Copyright (C) 2015 ecsec GmbH.
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
 */

package org.openecard.richclient.gui

import dev.icerock.moko.resources.format
import org.openecard.build.BuildInfo
import org.openecard.i18n.I18N
import org.openecard.richclient.RichClient
import org.openecard.richclient.gui.graphics.OecIconType
import org.openecard.richclient.gui.graphics.oecImage
import java.awt.Color
import java.awt.Container
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

/**
 * Frame class with the necessary interface for status element updates.
 *
 * @author Tobias Wich
 */
class InfoFrame(
	title: String?,
) : JFrame(title),
	StatusContainer {
	private var isShown = false

	override fun updateContent(status: Container) {
		pack()
	}

	override fun setVisible(b: Boolean) {
		if (isShown) {
			state =
				if (b) {
					NORMAL
				} else {
					ICONIFIED
				}
		} else {
			super.setVisible(b)

			// set after first setVisible(true) call
			if (b) {
				isShown = true
			}
		}
	}

	companion object {
		fun setupStandaloneFrame(client: RichClient): InfoFrame =
			InfoFrame(
				I18N.strings.richclient_tray_title
					.format(BuildInfo.appName)
					.localized(),
			).also { frame ->
				frame.iconImage = oecImage(OecIconType.COLORED, 256, 256)

				frame.defaultCloseOperation = DISPOSE_ON_CLOSE
				frame.addWindowListener(
					object : WindowAdapter() {
						override fun windowClosed(e: WindowEvent?) {
							client.teardown()
						}
					},
				)

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
			}

		fun setupPopupFrame(): InfoFrame =
			InfoFrame(
				I18N.strings.richclient_tray_title
					.format(BuildInfo.appName)
					.localized(),
			).also { frame ->
				frame.iconImage = oecImage(OecIconType.COLORED, 256, 256)

				frame.defaultCloseOperation = DISPOSE_ON_CLOSE
				frame.isVisible = false
			}
	}
}
