package org.openecard.client.gui.swing.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigInteger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import org.openecard.client.gui.swing.StepComponent;
import org.openecard.ws.gui.v1.InfoUnitType;
import org.openecard.ws.gui.v1.PasswordInput;
import org.openecard.ws.gui.v1.TextInput;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class AbstractInput implements StepComponent {

    private final String name;
    private final BigInteger minLength;
    private final BigInteger maxLength;

    private final JPanel panel;
    private final JLabel label;
    private final JTextComponent textField;

    private final Object result;

    public AbstractInput(Object input, JTextComponent textFieldImpl) {
        BigInteger min = null;
        BigInteger max = null;
        String value = null;
        String labelText = null;

        // extract values from input and write to output
        if (input instanceof TextInput) {
            TextInput tmp = (TextInput) input;
            this.name = tmp.getName();
            min = tmp.getMinlength();
            max = tmp.getMaxlength();
            value = tmp.getValue();
            labelText = tmp.getText();
            // create result element
            TextInput resultImpl = new TextInput();
            resultImpl.setMinlength(min);
            resultImpl.setMaxlength(max);
            resultImpl.setName(this.name);
            resultImpl.setText(labelText);
            result = resultImpl;
        } else if (input instanceof PasswordInput) {
            PasswordInput tmp = (PasswordInput) input;
            this.name = tmp.getName();
            min = tmp.getMinlength();
            max = tmp.getMaxlength();
            value = tmp.getValue();
            labelText = tmp.getText();
            // create result element
            PasswordInput resultImpl = new PasswordInput();
            resultImpl.setMinlength(min);
            resultImpl.setMaxlength(max);
            resultImpl.setName(this.name);
            resultImpl.setText(labelText);
            result = resultImpl;
        } else {
            throw new RuntimeException("Invalid class.");
        }

        // correct values
        if (min != null) {
            this.minLength = min;
        } else {
            this.minLength = BigInteger.ZERO;
        }
        if (max != null) {
            this.maxLength = max;
        } else {
            this.maxLength = BigInteger.valueOf(Long.MAX_VALUE);
        }
        this.textField = textFieldImpl;
        if (value != null) {
            this.textField.setText(value);
            this.textField.selectAll();
        }

        this.label = new JLabel();
        this.label.setMinimumSize(new Dimension(100, 0));
        this.label.setMaximumSize(new Dimension(100, 50));
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
        BigInteger textSize = BigInteger.valueOf(textValue.length());
        if (minLength.compareTo(textSize) != 1 && maxLength.compareTo(textSize) != 1) {
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
    public InfoUnitType getValue() {
        String textValue = this.textField.getText();
        if (textValue == null) {
            textValue = "";
        }

        InfoUnitType unit = new InfoUnitType();
        if (result instanceof TextInput) {
            ((TextInput)result).setValue(textValue);
            unit.setTextInput((TextInput)result);
        } else if (result instanceof PasswordInput) {
            ((PasswordInput)result).setValue(textValue);
            unit.setPasswordInput((PasswordInput)result);
        } else {
            throw new RuntimeException("Invalid class.");
        }
        
        return unit;
    }

}
