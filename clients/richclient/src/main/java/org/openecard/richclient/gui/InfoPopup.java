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

package org.openecard.richclient.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;


/**
 * This class creats a InfoPopup showing different information about connected terminals and available cards.
 * It also contains the different controls of the application, e.g. the exit button.
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class InfoPopup extends JDialog {

    private static final long serialVersionUID = 1L;

    private static final int DISTANCE_TO_TASKBAR = 2; // in px

    private Point point;

    /**
     * Constructor of InfoPopup class.
     *
     * @param c Container which will be set as ContentPane
     */
    public InfoPopup(Container c) {
	this(c, null);
    }

    /**
     * Constructor of InfoPopup class.
     *
     * @param c Container which will be set as ContentPane
     * @param p position
     */
    public InfoPopup(Container c, Point p) {
	super();
	point = p;
	setupUI(c);
    }

    /**
     * Updates the content of the InfoPopup by setting a new ContentPane.
     *
     * @param c Container which will be set as ContentPane
     */
    public void updateContent(Container c) {
	setContentPane(c);
	pack();
	setLocation(calculatePosition(c, point));
    }

    private Point calculatePosition(Container c, Point p) {
	GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
	Rectangle scrnSize = gEnv.getDefaultScreenDevice().getDefaultConfiguration().getBounds();
	Rectangle winSize = gEnv.getMaximumWindowBounds();
	Dimension popupSize = c.getPreferredSize();
	int x;
	int y;

	if (winSize.x > 5) { // taskbar left
	    x = winSize.x + DISTANCE_TO_TASKBAR;
	    y = p.y > (winSize.height / 2) ? p.y - popupSize.height : p.y;
	} else if (winSize.y > 5) { // taskbar top
	    x = p.x > (winSize.width / 2) ? p.x - popupSize.width : p.x;
	    y = winSize.y + DISTANCE_TO_TASKBAR;
	} else if (scrnSize.width > winSize.width) { // taskbar right
	    x = winSize.width - popupSize.width - DISTANCE_TO_TASKBAR;
	    y = p.y > (winSize.height / 2) ? p.y - popupSize.height : p.y;
	} else { // taskbar bottom
	    x = p.x > (winSize.width / 2) ? p.x - popupSize.width : p.x;
	    y = winSize.height - popupSize.height - DISTANCE_TO_TASKBAR;
	}

	return new Point(x, y);
    }

    private void setupUI(Container c) {
	setAlwaysOnTop(true);
	setUndecorated(true);
	setContentPane(c);
	pack();

	if (point != null) {
	    setLocation(calculatePosition(c, point));
	}

	addWindowFocusListener(new WindowAdapter() {

	    @Override
	    public void windowLostFocus(WindowEvent e) {
		dispose();
	    }
	});

	setVisible(true);
    }

}
