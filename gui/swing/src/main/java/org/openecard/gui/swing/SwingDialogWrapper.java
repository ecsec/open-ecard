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

package org.openecard.gui.swing;

import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import org.openecard.gui.swing.common.GUIDefaults;


/**
 *
 * @author Moritz Horsch
 */
public class SwingDialogWrapper {

    private JFrame dialog;
    private String title;

    public SwingDialogWrapper() {
	// Initialize Look and Feel
	GUIDefaults.initialize();
    }

    private SwingDialogWrapper(SwingDialogWrapper other) {
	this.title = other.title;
	getContentPane();
    }

    JFrame getDialog() {
	return dialog;
    }

    JRootPane getRootPane() {
	return dialog.getRootPane();
    }

    /**
     * Set title of the user consent dialog.
     *
     * @param title Title to set in the dialog.
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * A content panel is needed so the user consent can be embedded in the actual application.
     *
     * @return Container the GUI can draw its content on.
     */
    public final Container getContentPane() {
	dialog = new JFrame();
	dialog.setTitle(title);
	dialog.setSize(690, 500);
	dialog.setLocationRelativeTo(null);
	dialog.setIconImage(GUIDefaults.getImage("Frame.icon", 45, 45).getImage());
	dialog.setVisible(false);
	dialog.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

	return dialog.getContentPane();
    }

    public void setSize(int width, int height) {
	getDialog().setSize(width, height);
    }

    /**
     * This function is executed after the root panel has been set up with the contents of the user consent.
     */
    public void show() {
	dialog.setVisible(true);
	dialog.toFront();
	dialog.requestFocus();
	dialog.setAlwaysOnTop(true);
    }

    /**
     * This function is executed after the user consent is finished or canceled.
     */
    public void hide() {
	dialog.setVisible(false);
    }

    public SwingDialogWrapper derive() {
	return new SwingDialogWrapper(this);
    }

}
