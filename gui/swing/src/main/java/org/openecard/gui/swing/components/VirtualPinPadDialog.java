/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import org.openecard.common.util.FileUtils;
import org.openecard.gui.definition.PasswordField;


/**
 * Dialog displaying a virtual pin pad.
 * The dialog is modal and bound to the window displaying the input field. If a maximum length is given in the
 * definition of the PIN element, then the dialog closes itself automatically after entering the respective number of
 * characters.
 *
 * @author Tobias Wich
 */
public class VirtualPinPadDialog extends JDialog {

    private final JTextComponent inputField;
    private final PasswordField passDef;
    private int numCharsEntered = 0;

    /**
     * Creates a new instance of the dialog.
     *
     * @param pinButton
     * @param inputField The component capturing the PIN.
     * @param passDef The definition of the password field.
     */
    public VirtualPinPadDialog(VirtualPinPadButton pinButton, JTextComponent inputField, PasswordField passDef) {
	super(getOwningWindow(inputField), "PIN-Pad", ModalityType.DOCUMENT_MODAL);
	this.inputField = inputField;
	this.passDef = passDef;
	this.inputField.setText("");

	setSize(200, 200);
	setResizable(false);
	setLayout(new BorderLayout(3, 3));

	Point dialogLocation = pinButton.getLocationOnScreen();
	dialogLocation.translate(0, pinButton.getHeight());
	setLocation(dialogLocation);

	JPanel buttons = new JPanel(new GridLayout(4, 3, 4, 4));
	add(buttons, BorderLayout.CENTER);
	for (int i = 1; i <= 9; i++) {
	    buttons.add(createButton(i));
	}
	// last row
	buttons.add(createRemoveSingleElementButton());
	buttons.add(createButton(0));
	buttons.add(createCloseButton());
    }

    private JButton createButton(int num) {
	JButton button = new JButton(Integer.toString(num));
	button.addActionListener(new NumberProcessingListener());
	return button;
    }

    private JButton createRemoveSingleElementButton() {
	JButton button = new JButton();
	//setButtonFont(button);
	button.addActionListener(new RemoveSingleElementListener());
	Icon ico = new ImageIcon(FileUtils.resolveResourceAsURL(VirtualPinPadDialog.class, "arrow.png"));
	button.setIcon(ico);
	Insets marginInset = button.getMargin();
	button.setMargin(new Insets(marginInset.top, 5, marginInset.bottom, 5));
	return button;
    }

    private JButton createCloseButton() {
	JButton button = new JButton("OK");
	//setButtonFont(button);
	button.addActionListener(new CloseInputListener());
	Insets marginInset = button.getMargin();
	button.setMargin(new Insets(marginInset.top, 5, marginInset.bottom, 5));
	return button;
    }

    private void setButtonFont(JButton button) {
	Font f = button.getFont();
	f = f.deriveFont(f.getSize2D() + 10);
	button.setFont(f);
    }


    private static Window getOwningWindow(JTextComponent inputField) {
	return SwingUtilities.getWindowAncestor(inputField);
    }

    /**
     * Listener handling button presses of the PIN number buttons.
     */
    private class NumberProcessingListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    JButton b = (JButton) e.getSource();
	    String data = inputField.getText();
	    data += b.getText();
	    inputField.setText(data);

	    numCharsEntered++;
	    if (passDef.getMaxLength() > 0 && numCharsEntered >= passDef.getMaxLength()) {
		setVisible(false);
	    }
	}
    }

    private class RemoveSingleElementListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    String data = inputField.getText();
	    if (! data.isEmpty()) {
		data = data.substring(0, data.length() - 1);
		inputField.setText(data);
		numCharsEntered--;
	    }
	}
    }

    private class CloseInputListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    dispatchEvent(new WindowEvent(VirtualPinPadDialog.this, WindowEvent.WINDOW_CLOSING));
	}
    }

}
