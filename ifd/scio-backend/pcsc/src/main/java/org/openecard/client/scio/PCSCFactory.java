package org.openecard.client.scio;

import java.security.NoSuchAlgorithmException;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;


/**
 * Proxy and abstracted Factory for SCIO PC/SC driver.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class PCSCFactory implements org.openecard.client.common.ifd.TerminalFactory {

    private final TerminalFactory factory;

    public PCSCFactory() throws NoSuchAlgorithmException {
        factory = TerminalFactory.getInstance("PC/SC", null);
    }


    @Override
    public String getType() {
	return factory.getType();
    }

    @Override
    public CardTerminals terminals() {
	return factory.terminals();
    }

}
