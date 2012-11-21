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

package org.openecard.gui.swing;

import java.awt.Container;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.openecard.gui.swing.common.GUIDefaults;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class SwingDialogWrapper implements DialogWrapper {

    private JFrame dialog;

    public SwingDialogWrapper() {
	// Initialize Look and Feel
	GUIDefaults.initialize();

	dialog = new JFrame();
	dialog.setSize(640, 480);
	dialog.setLocationRelativeTo(null);
	dialog.setIconImage(GUIDefaults.getImage("Frame.icon", 45, 45).getImage());
	dialog.setVisible(false);
	dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    @Override
    public void setTitle(String title) {
	dialog.setTitle(title);
    }

    @Override
    public Container getContentPane() {
	return dialog.getContentPane();
    }

    @Override
    public void show() {
	this.dialog.setVisible(true);
    }

    @Override
    public void hide() {
	this.dialog.setVisible(false);
    }

}
