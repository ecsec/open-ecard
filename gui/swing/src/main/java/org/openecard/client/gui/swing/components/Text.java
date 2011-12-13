package org.openecard.client.gui.swing.components;

import java.awt.Component;
import javax.swing.JLabel;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 * Implementation of a simple text component for use in a {@link StepFrame}.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Text implements StepComponent {

    private final JLabel label;

    public Text(org.openecard.client.gui.definition.Text text) {
	String s = text.getText();
	// replace newlines with appropriate html code
	if (s.contains("\n")) {
	    s = "<html>" + s + "</html>";
	    s = s.replace("\n", "<br/>");
	}
	this.label = new JLabel(s);
    }


    @Override
    public Component getComponent() {
	return label;
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
