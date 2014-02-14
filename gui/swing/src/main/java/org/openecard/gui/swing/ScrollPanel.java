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
 ***************************************************************************/

package org.openecard.gui.swing;

import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JPanel;
import javax.swing.Scrollable;


/**
 * JPanel which only scrolls vertically.
 * See this Stack Overflow
 * <a href="http://stackoverflow.com/questions/15783014/jtextarea-on-jpanel-inside-jscrollpane-does-not-resize-properly"
 * >article</a>. This is needed because The content produces odd resizing behaviour.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ScrollPanel extends JPanel implements Scrollable {

    private static final long serialVersionUID = 1L;

    @Override
    public Dimension getPreferredScrollableViewportSize() {
	 //tell the JScrollPane that we want to be our 'preferredSize'
	// but later, we'll say that vertically, it should scroll.
        return super.getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
	// 16 seems reasonable
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
	// 16 seems reasonable
        return 16;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
	// track the width, and re-size as needed
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
	// we don't want to track the height, because we want to scroll vertically
        return false;
    }

}
