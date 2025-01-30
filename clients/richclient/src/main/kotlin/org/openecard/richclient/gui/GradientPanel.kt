/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

import java.awt.*
import javax.swing.JPanel

/**
 * This class creates a Panel having a gradient as background. The gradient runs form left to right, with color1
 * being [LIGHT_GRAY][java.awt.Color.LIGHT_GRAY] and color2 being [DARK_GRAY][java.awt.Color.DARK_GRAY].
 *
 * @author Johannes Schmölz
 */
class GradientPanel : JPanel {
    var color1: Color? = null
    var color2: Color? = null

    /**
     * Constructor of GradientPanel class.
     */
    constructor() : super()

    /**
     * Constructor of GradientPanel class.
     *
     * @param color1 color on the left
     * @param color2 color on the right
     */
    constructor(color1: Color?, color2: Color?) {
        this.color1 = color1
        this.color2 = color2
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param layout layout of the panel
     */
    constructor(layout: LayoutManager?) : super(layout)

    /**
     * Constructor of GradientPanel class.
     *
     * @param layout layout of the panel
     * @param color1 color on the left
     * @param color2 color on the right
     */
    constructor(layout: LayoutManager?, color1: Color?, color2: Color?) : super(layout) {
        this.color1 = color1
        this.color2 = color2
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param isDoubleBuffered set to true to enable double-buffering
     */
    constructor(isDoubleBuffered: Boolean) : super(isDoubleBuffered)

    /**
     * Constructor of GradientPanel class.
     *
     * @param isDoubleBuffered set to true to enable double-buffering
     * @param color1 color on the left
     * @param color2 color on the right
     */
    constructor(isDoubleBuffered: Boolean, color1: Color?, color2: Color?) : super(isDoubleBuffered) {
        this.color1 = color1
        this.color2 = color2
    }

    /**
     * Constructor of GradientPanel class.
     *
     * @param layout layout of the panel
     * @param isDoubleBuffered set to true to enable double-buffering
     */
    constructor(layout: LayoutManager?, isDoubleBuffered: Boolean) : super(layout, isDoubleBuffered)

    /**
     * Constructor of GradientPanel class.
     *
     * @param layout layout of the panel
     * @param isDoubleBuffered set to true to enable double-buffering
     * @param color1 color on the left
     * @param color2 color on the right
     */
    constructor(layout: LayoutManager?, isDoubleBuffered: Boolean, color1: Color?, color2: Color?) : super(
        layout,
        isDoubleBuffered
    ) {
        this.color1 = color1
        this.color2 = color2
    }

    override fun paintComponent(g: Graphics) {
        val g2d = g.create() as Graphics2D

        if (color1 == null) {
            color1 = Color.LIGHT_GRAY
        }

        if (color2 == null) {
            color2 = Color.DARK_GRAY
        }

        val w = width
        val h = height

        // gradient from left to right
        val gp = GradientPaint(0f, 0f, color1, w.toFloat(), 0f, color2)

        g2d.paint = gp
        g2d.fillRect(0, 0, w, h)
        g2d.dispose()

        // The gradient will be painted on top of the panel so that all components added to it will be hidden.
        // In order to make them visible again paintComponent(g) must be called on the superclass.
        super.paintComponent(g)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
