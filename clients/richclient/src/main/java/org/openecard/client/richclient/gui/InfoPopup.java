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

package org.openecard.client.richclient.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class InfoPopup extends JDialog {

    private Point location;

    public InfoPopup(Container c) {
	this(c, null);
    }

    public InfoPopup(Container c, Point p) {
	super();
	location = p;
	setupUI(c);
    }

    public void updateContent(Container c) {
	setContentPane(c);
	pack();
	Dimension d = c.getPreferredSize();
	setLocation(location.x - d.width, location.y - d.height);
    }

    private void setupUI(Container c) {
	setUndecorated(true);
	Color blue = new Color(121, 170, 215);
	setBackground(blue);
	setContentPane(c);

	if (location != null) {
	    Dimension d = c.getPreferredSize();
	    setLocation(location.x - d.width, location.y - d.height);
	}

	addWindowFocusListener(new WindowAdapter() {

	    @Override
	    public void windowLostFocus(WindowEvent e) {
		dispose();
	    }
	});

	pack();
	setVisible(true);
    }

}
