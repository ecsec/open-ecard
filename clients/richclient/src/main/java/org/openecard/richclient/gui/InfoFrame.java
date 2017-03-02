/****************************************************************************
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
 ***************************************************************************/

package org.openecard.richclient.gui;

import java.awt.Container;
import java.awt.Frame;
import java.awt.HeadlessException;
import javax.swing.JFrame;


/**
 * Frame class with the necessary interface for status element updates.
 *
 * @author Tobias Wich
 */
public class InfoFrame extends JFrame implements StatusContainer {

    private boolean isShown = false;

    public InfoFrame(String title) throws HeadlessException {
	super(title);
    }

    @Override
    public void updateContent(Container c) {
	pack();
    }

    @Override
    public void setVisible(boolean b) {
	if (isShown) {
	    if (b) {
		setState(Frame.NORMAL);
	    } else {
		setState(Frame.ICONIFIED);
	    }
	} else {
	    super.setVisible(b);

	    // set after first setVisable(true) call
	    if (b) {
		isShown = true;
	    }
	}
    }

}
