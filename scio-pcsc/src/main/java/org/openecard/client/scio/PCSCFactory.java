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

    public static org.openecard.client.common.ifd.TerminalFactory getInstance() throws NoSuchAlgorithmException {
	return new PCSCFactory(TerminalFactory.getInstance("PC/SC", null));
    }

    private final TerminalFactory factory;

    private PCSCFactory(TerminalFactory factory) {
	this.factory = factory;
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
