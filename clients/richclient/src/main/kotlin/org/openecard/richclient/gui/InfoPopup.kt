/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

import java.awt.Container
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog

/**
 * This class creates a InfoPopup showing different information about connected terminals and available cards.
 * It also contains the different controls of the application, e.g. the exit button.
 *
 * @author Johannes Schmölz
 */
class InfoPopup @JvmOverloads constructor(c: Container, private val point: Point? = null) : JDialog(),
    StatusContainer {
    /**
     * Constructor of InfoPopup class.
     *
     * @param c Container which will be set as ContentPane
     * @param point position
     */
    /**
     * Constructor of InfoPopup class.
     *
     * @param c Container which will be set as ContentPane
     */
    init {
        setupUI(c)
    }

    /**
     * Updates the content of the InfoPopup by setting a new ContentPane.
     *
     * @param c Container which will be set as ContentPane
     */
    override fun updateContent(c: Container) {
        contentPane = c
        pack()
		repaint()
		point?.let {
			location = calculatePosition(c, point)
		}
    }

    private fun calculatePosition(c: Container, p: Point): Point {
        val gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val scrnSize = gEnv.defaultScreenDevice.defaultConfiguration.bounds
        val winSize = gEnv.maximumWindowBounds
        val popupSize = c.preferredSize
        val x: Int
        val y: Int

        if (winSize.x > 5) { // taskbar left
            x = winSize.x + DISTANCE_TO_TASKBAR
            y = if (p.y > (winSize.height / 2)) p.y - popupSize.height else p.y
        } else if (winSize.y > 5) { // taskbar top
            x = if (p.x > (winSize.width / 2)) p.x - popupSize.width else p.x
            y = winSize.y + DISTANCE_TO_TASKBAR
        } else if (scrnSize.width > winSize.width) { // taskbar right
            x = winSize.width - popupSize.width - DISTANCE_TO_TASKBAR
            y = if (p.y > (winSize.height / 2)) p.y - popupSize.height else p.y
        } else { // taskbar bottom
            x = if (p.x > (winSize.width / 2)) p.x - popupSize.width else p.x
            y = winSize.height - popupSize.height - DISTANCE_TO_TASKBAR
        }

        return Point(x, y)
    }

    private fun setupUI(c: Container) {
        isAlwaysOnTop = true
        isUndecorated = true
        contentPane = c
        pack()

		point?.let {
            location = calculatePosition(c, point)
        }

        addWindowFocusListener(object : WindowAdapter() {
            override fun windowLostFocus(e: WindowEvent) {
                //dispose()
            }
        })

        isVisible = true
    }

    companion object {
        private const val serialVersionUID = 1L

        private const val DISTANCE_TO_TASKBAR = 2 // in px
    }
}
