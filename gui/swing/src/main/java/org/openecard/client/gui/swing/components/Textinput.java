package org.openecard.client.gui.swing.components;

import javax.swing.JTextField;
import org.openecard.ws.gui.v1.TextInput;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Textinput extends AbstractInput {

    public Textinput(TextInput input) {
        super(input, new JTextField());
   }

}
