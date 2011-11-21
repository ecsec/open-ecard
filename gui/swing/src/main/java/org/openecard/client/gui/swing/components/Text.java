package org.openecard.client.gui.swing.components;

import java.awt.Component;
import javax.swing.JLabel;
import org.openecard.client.gui.swing.StepComponent;
import org.openecard.ws.gui.v1.InfoUnitType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Text implements StepComponent {

    private final JLabel label;

    public Text(String text) {
        String s = "<html>" + text + "</html>";
        s = s.replace("\n", "<br/>");
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
    public InfoUnitType getValue() {
        return null;
    }

}
