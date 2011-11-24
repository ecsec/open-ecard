package org.openecard.client.gui.swing.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigInteger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;
import org.openecard.ws.gui.v1.AbstractInputType;
import org.openecard.ws.gui.v1.OutputInfoUnitType;
import org.openecard.ws.gui.v1.PasswordInput;
import org.openecard.ws.gui.v1.TextInput;


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
    private final BigInteger minLength;
    private final BigInteger maxLength;

    private final JPanel panel;
    private final JLabel label;
    private final JTextComponent textField;

    private final Object result;

    public AbstractInput(TextInput input) {
        this(input, new JTextField(20));
    }
    public AbstractInput(PasswordInput input) {
        this(input, new JPasswordField(12));
    }

    private AbstractInput(AbstractInputType input, JTextComponent textFieldImpl) {
        BigInteger min = null;
        BigInteger max = null;
        String value = null;
        String labelText = null;

        // extract values from input and write to output (depending on actual type)
        this.name = input.getName();
        min = input.getMinlength();
        max = input.getMaxlength();
        value = input.getValue();
        labelText = input.getText();
        // create result element
        TextInput resultImpl = new TextInput();
        resultImpl.setMinlength(min);
        resultImpl.setMaxlength(max);
        resultImpl.setName(this.name);
        resultImpl.setText(labelText);
        result = resultImpl;

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
        BigInteger textSize = BigInteger.valueOf(textValue.length());
        // min <= text && text <= max
        if (minLength.compareTo(textSize) != 1 && textSize.compareTo(maxLength) != 1) {
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
    public OutputInfoUnitType getValue() {
        String textValue = this.textField.getText();
        if (textValue == null) {
            textValue = "";
        }

        OutputInfoUnitType unit = new OutputInfoUnitType();
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
