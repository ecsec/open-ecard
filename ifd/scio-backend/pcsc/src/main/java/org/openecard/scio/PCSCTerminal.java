package org.openecard.scio;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PC/SC terminal implementation of the SCIOTerminal.
 * 
 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public class PCSCTerminal implements SCIOTerminal {

    private static final Logger logger = LoggerFactory.getLogger(PCSCTerminal.class);
    private final CardTerminal terminal;

    public PCSCTerminal(CardTerminal terminal) {
	this.terminal = terminal;
    }

    @Override
    public String getName() {
	return terminal.getName();
    }

    @Override
    public PCSCCard connect(String protocol) throws SCIOException {
	try {
	    Card Result = terminal.connect(protocol);
	    return new PCSCCard(Result);
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }

    @Override
    public boolean isCardPresent() throws SCIOException {
	try {
	    return terminal.isCardPresent();
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }

    @Override
    public boolean waitForCardPresent(long timeout) throws SCIOException {
	try {
	    return terminal.waitForCardPresent(timeout);
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }

    @Override
    public boolean waitForCardAbsent(long timeout) throws SCIOException {
	try {
	    return terminal.waitForCardAbsent(timeout);
	} catch (CardException ex) {
	    logger.error(ex.getMessage(), ex);
	    throw new SCIOException(ex);
	}
    }
}
