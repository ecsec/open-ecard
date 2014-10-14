/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.gui.swing.components;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import org.openecard.common.util.FileUtils;
import org.openecard.gui.definition.PasswordField;


/**
 * Button launching a pin pad dialog when pressed.
 * The button needs access to the password input field, and the definition to create a suitable dialog.
 *
 * @author Tobias Wich
 */
public class VirtualPinPadButton extends JLabel {

    private final JTextComponent inputField;
    private final PasswordField passDef;

    /**
     * Creates an instance of the button.
     *
     * @param inputField The component capturing the PIN.
     * @param passDef The definition of the password field.
     */
    public VirtualPinPadButton(JTextComponent inputField, PasswordField passDef) {
	super(getPinPadIcon());
	this.inputField = inputField;
	this.passDef = passDef;

	setBorder(BorderFactory.createEmptyBorder());
	addMouseListener(new ButtonStyleHandler());
	addMouseListener(new VirtualPinPadDialogHandler());
    }

    private static ImageIcon getPinPadIcon() {
	URL imgUrl = FileUtils.resolveResourceAsURL(VirtualPinPadButton.class, "virtual-pinpad-button.png");
	ImageIcon img = new ImageIcon(imgUrl);
	return img;
    }

    private class ButtonStyleHandler implements MouseListener {
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	@Override
	public void mousePressed(MouseEvent e) {
	    setBorder(BorderFactory.createLoweredBevelBorder());
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	    setBorder(BorderFactory.createEmptyBorder());
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
    }

    private class VirtualPinPadDialogHandler implements MouseListener {
	@Override
	public void mouseClicked(MouseEvent e) {
	    VirtualPinPadDialog dialog = new VirtualPinPadDialog(inputField, passDef);
	    dialog.setVisible(true);
	}
	@Override
	public void mousePressed(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	}
	@Override
	public void mouseEntered(MouseEvent e) {
	}
	@Override
	public void mouseExited(MouseEvent e) {
	}
    }

}
