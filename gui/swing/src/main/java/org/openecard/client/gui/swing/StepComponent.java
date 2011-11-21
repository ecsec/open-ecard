package org.openecard.client.gui.swing;

import java.awt.Component;
import org.openecard.ws.gui.v1.InfoUnitType;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public interface StepComponent {

    public Component getComponent();
    public boolean validate();
    public boolean isValueType();
    public InfoUnitType getValue();

}
