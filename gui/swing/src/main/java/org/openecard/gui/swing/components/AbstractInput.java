/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import org.openecard.common.util.FileUtils;
import org.openecard.gui.definition.AbstractTextField;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.TextField;
import static org.openecard.gui.swing.SwingDialogWrapper.needsFullscreen;
import sun.security.util.SecurityConstants;


/**
 * Common base for {@link TextField} and {@link PasswordField}. <br>
 * The casting is ugly, but in the short time no better solution occured to me.
 * Remind, the problem is that TextInput and PasswordInput are identical but
 * have no base class. C++ templates would help, but Java generics don't.
 * Feel free to get rid of this mess.
 *
 * @author Tobias Wich
 */
public class AbstractInput implements StepComponent, Focusable {

    private final String name;
    private final int minLength;
    private final int maxLength;

    private final JPanel panel;
    private final JLabel label;
    private final JTextField textField;

    private final AbstractTextField result;
    private int numCharsEntered = 0;

    public AbstractInput(TextField input) {
	this(input, new TextField(input.getID()), new JTextField(20));
    }
    public AbstractInput(PasswordField input) {
	this(input, new PasswordField(input.getID()), new JPasswordField(12));
	if (needsFullscreen()) {
	    JPanel buttons = new JPanel(new GridLayout(4, 3, 4, 4));
	    this.panel.add(buttons, BorderLayout.CENTER);
	    for (int i = 1; i <= 9; i++) {
		buttons.add(createButton(i));
	    }

	    buttons.add(createRemoveSingleElementButton());
	    buttons.add(createButton(0));
	} else {
	    this.panel.add(new VirtualPinPadButton(textField, input), 1);
	}
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
    
    private class NumberProcessingListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    JButton b = (JButton) e.getSource();
	    String data = textField.getText();
	    data += b.getText();
	    textField.setText(data);

	    numCharsEntered++;
//	    if (pwInput.getMaxLength() > 0 && numCharsEntered >= pwInput.getMaxLength()) {
//		setVisible(false);
//	    }
	}
    }
    
    private class RemoveSingleElementListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    String data = textField.getText();
	    if (! data.isEmpty()) {
		data = data.substring(0, data.length() - 1);
		textField.setText(data);
		numCharsEntered--;
	    }
	}
    }

    private AbstractInput(AbstractTextField input, AbstractTextField output, JTextField textFieldImpl) {
	char[] value;
	String labelText;

	// extract values from input and write to output (depending on actual type)
	this.name = input.getID();
	minLength = input.getMinLength();
	maxLength = input.getMaxLength();
	value = input.getValue();
	labelText = input.getDescription();
	// create result element
	result = output;
	result.setMinLength(minLength);
	result.setMaxLength(maxLength);
	result.setDescription(labelText);

	// correct values
	this.textField = textFieldImpl;
	if (this.textField instanceof JPasswordField) {

	} else {
	    this.textField.setText(new String(value));
	    this.textField.selectAll();
	}

	this.label = new JLabel();
	this.label.setMinimumSize(new Dimension(100, 0));
	this.label.setMaximumSize(new Dimension(100, 50));
	this.label.setSize(100, this.label.getSize().height);
	if (labelText != null) {
	    this.label.setText(labelText);
	}

	// create panel for display
	this.panel = new JPanel();
	FlowLayout panelLayout = new FlowLayout(FlowLayout.LEFT);
	this.panel.setLayout(panelLayout);
	this.panel.add(this.textField);
	this.panel.add(this.label);
    }
    
    @Nonnull
    private char[] getFieldValue() {
        Document doc = textField.getDocument();
        Segment txt = new Segment();
        try {
            doc.getText(0, doc.getLength(), txt); // use the non-String API
        } catch (BadLocationException e) {
            return new char[0];
        }
        char[] retValue = new char[txt.count];
        System.arraycopy(txt.array, txt.offset, retValue, 0, txt.count);
        return retValue;
    }

    @Override
    public Component getComponent() {
	return this.panel;
    }

    @Override
    public boolean validate() {
	String textValue = this.textField.getText();
	if (textValue == null) {
	    textValue = "";
	}
	int textSize = textValue.length();
	// min <= text && text <= max
	if (minLength <= textSize && textSize <= maxLength) {
	    return true;
	} else {
	    return false;
	}
    }

    @Override
    public boolean isValueType() {
	return true;
    }

    @Override
    public OutputInfoUnit getValue() {
	char[] textValue = getFieldValue();
	result.setValue(textValue);
	Arrays.fill(textValue, ' ');
	return result;
    }

    @Override
    public void setFocus() {
	textField.requestFocusInWindow();
    }

}
