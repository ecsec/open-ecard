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

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;


/**
 * This class creats a InfoPopup showing different information about connected terminals and inserted cards.
 * It also contains the different controls of the application, e.g. the exit button.
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class InfoPopup extends JDialog {

    private static final long serialVersionUID = 1L;

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
	int x;
	int y;
	Dimension d = c.getPreferredSize();

	// calculate X coordinate
	if (p.x - d.width > 0) {
	    x = p.x - d.width;
	} else {
	    x = p.x;
	}
	// calculate Y coordinate
	if (p.y - d.height > 0) {
	    y = p.y - d.height;
	} else {
	    y = p.y;
	}

	return new Point(x, y);
    }

    private void setupUI(Container c) {
	setAlwaysOnTop(true);
	setUndecorated(true);
	Color blue = new Color(121, 170, 215);
	setBackground(blue);
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
