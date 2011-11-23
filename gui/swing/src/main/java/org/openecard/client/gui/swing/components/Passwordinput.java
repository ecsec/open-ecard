package org.openecard.client.gui.swing.components;

import javax.swing.JPasswordField;
import org.openecard.ws.gui.v1.PasswordInput;


/**
 * Implementation of a text input component. Only supply proper values to the superclass.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Passwordinput extends AbstractInput {

    public Passwordinput(PasswordInput input) {
        super(input, new JPasswordField(12));
    }

}
