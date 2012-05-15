package org.openecard.client.gui.swing.components;

import java.awt.Component;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JTextArea;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.swing.StepFrame;

/**
 * Implementation of a simple text component for use in a {@link StepFrame}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Text implements StepComponent {

    private JTextArea textArea;

    public Text(org.openecard.client.gui.definition.Text text) {
	textArea = new JTextArea(text.getText());
	textArea.setMargin(new Insets(0, 0, 0, 0));
	textArea.setEditable(false);
	textArea.setLineWrap(true);
	textArea.setWrapStyleWord(true);
	textArea.setFont(new JButton().getFont());
    }

    @Override
    public Component getComponent() {
	return textArea;
    }

    @Override
    public boolean validate() {
	return true;
    }

    @Override
    public boolean isValueType() {
	return false;
    }

    @Override
    public OutputInfoUnit getValue() {
	return null;
    }
}
