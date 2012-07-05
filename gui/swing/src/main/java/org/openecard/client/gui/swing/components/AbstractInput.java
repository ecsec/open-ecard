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

package org.openecard.client.gui.swing.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.openecard.client.gui.definition.AbstractTextField;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.PasswordField;
import org.openecard.client.gui.definition.TextField;


/**
 * <p>Common base for {@link Textinput} and {@link Passwordinput}.<p/>
 * The casting is ugly, but in the short time no better solution occured to me.
 * Remind, the problem is that TextInput and PasswordInput are identical but
 * have no base class. C++ templates would help, but Java generics don't.
 * Feel free to get rid of this mess.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class AbstractInput implements StepComponent {

    private final String name;
    private final int minLength;
    private final int maxLength;

    private final JPanel panel;
    private final JLabel label;
    private final JTextComponent textField;

    private final AbstractTextField result;

    public AbstractInput(TextField input) {
	this(input, new TextField(), new JTextField(20));
    }
    public AbstractInput(PasswordField input) {
	this(input, new PasswordField(), new JPasswordField(12));
    }

    private AbstractInput(AbstractTextField input, AbstractTextField output, JTextComponent textFieldImpl) {
	String value = null;
	String labelText = null;

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
	result.setID(this.name);
	result.setDescription(labelText);

	// correct values
	this.textField = textFieldImpl;
	if (value != null) {
	    this.textField.setText(value);
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
	this.panel.add(this.label);
	this.panel.add(this.textField);
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
	String textValue = this.textField.getText();
	if (textValue == null) {
	    textValue = "";
	}

	result.setValue(textValue);
	return result;
    }

}
