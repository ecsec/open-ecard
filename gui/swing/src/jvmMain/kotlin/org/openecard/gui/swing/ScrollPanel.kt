/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of SkIDentity.
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 */
package org.openecard.gui.swing

import java.awt.Dimension
import java.awt.Rectangle
import javax.swing.JPanel
import javax.swing.Scrollable

/**
 * JPanel which only scrolls vertically.
 * See this Stack Overflow
 * [article](http://stackoverflow.com/questions/15783014/jtextarea-on-jpanel-inside-jscrollpane-does-not-resize-properly). This is needed because The content produces odd resizing behaviour.
 *
 * @author Tobias Wich
 */
class ScrollPanel : JPanel(), Scrollable {
    override fun getPreferredScrollableViewportSize(): Dimension? {
        //tell the JScrollPane that we want to be our 'preferredSize'
        // but later, we'll say that vertically, it should scroll.
        return super.getPreferredSize()
    }

    override fun getScrollableUnitIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
        // 16 seems reasonable
        return 16
    }

    override fun getScrollableBlockIncrement(visibleRect: Rectangle?, orientation: Int, direction: Int): Int {
        // 16 seems reasonable
        return 16
    }

    override fun getScrollableTracksViewportWidth(): Boolean {
        // track the width, and re-size as needed
        return true
    }

    override fun getScrollableTracksViewportHeight(): Boolean {
        // we don't want to track the height, because we want to scroll vertically
        return false
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
