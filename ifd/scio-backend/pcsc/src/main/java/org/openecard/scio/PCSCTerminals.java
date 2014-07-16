package org.openecard.scio;

import java.util.ArrayList;
import java.util.List;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.SCIOTerminals.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PC/SC terminals implementation of the SCIOTerminals.
 * 
 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public class PCSCTerminals extends SCIOTerminals {

    private static final Logger logger = LoggerFactory.getLogger(PCSCTerminals.class);
    private static final long WAIT = (long) (500);
    private final CardTerminals terminals;

    public PCSCTerminals(CardTerminals terminals) {
	this.terminals = terminals;
    }

    @Override
    public List<SCIOTerminal> list() throws SCIOException {
	return list(State.ALL);
    }

    @Override
    public List<SCIOTerminal> list(State state) throws SCIOException {
	List<SCIOTerminal> list = new ArrayList<SCIOTerminal>();
	try {
	    List<CardTerminal> t = terminals.list(javax.smartcardio.CardTerminals.State.ALL);

	    for (CardTerminal ct : t) {
		SCIOTerminal st = new PCSCTerminal(ct);
		if (state.equals(State.ALL)) {
		    list.add(st);
		} else if (state.equals(State.CARD_PRESENT) || st.isCardPresent()) {
		    list.add(st);
		} else if (state.equals(State.CARD_ABSENT) || !st.isCardPresent()) {
		    list.add(st);
		} else if (state.equals(State.CARD_INSERTION)) {
		    // TODO
		    throw new UnsupportedOperationException("State CARD_INSERTION not supported yet");
		} else if (state.equals(State.CARD_REMOVAL)) {
		    // TODO
		    throw new UnsupportedOperationException("State CARD_REMOVAL not supported yet");
		}
	    }
	} catch (CardException ex) {
	    logger.warn(ex.getMessage(), ex);
	}
	return list;
    }

    @Override
    public SCIOTerminal getTerminal(String name) {
	try {
	    for (SCIOTerminal terminal : list()) {
		if (terminal.getName().equals(name)) {
		    return terminal;
		}
	    }
	    return null;
	} catch (SCIOException ex) {
	    logger.error(ex.getMessage(), ex);
	    return null;
	}
    }

    @Override
    public void waitForChange() throws SCIOException {
	waitForChange(0);
    }

    @Override
    public boolean waitForChange(long timeout) throws SCIOException {
	if (timeout < 0) {
	    throw new IllegalArgumentException("Timeout is negative.");
	} else if (timeout == 0) {
	    timeout = Long.MAX_VALUE;
	}
	long start = System.currentTimeMillis();
	while ((System.currentTimeMillis() - start) < timeout) {
	    try {
		Thread.sleep(WAIT);
	    } catch (InterruptedException ignore) {
	    }
	}
	return true;
    }
}
