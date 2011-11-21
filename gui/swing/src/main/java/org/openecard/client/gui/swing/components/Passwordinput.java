package org.openecard.client.gui.swing.components;

import javax.swing.JPasswordField;
import org.openecard.ws.gui.v1.PasswordInput;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Passwordinput extends AbstractInput {

    public Passwordinput(PasswordInput input) {
        super(input, new JPasswordField());
    }

}
