package org.openecard.common.ifd.scio;

import java.util.List;

/**
 *
 * @author Wael Alkhatib <walkhatib@cdc.informatik.tu-darmstadt.de>
 */
public abstract class SCIOTerminals {

    public static enum State {

	ALL,
	CARD_PRESENT,
	CARD_ABSENT,
	CARD_INSERTION,
	CARD_REMOVAL,
    }

    public abstract List<SCIOTerminal> list(State state) throws SCIOException;

    public List<SCIOTerminal> list() throws SCIOException {
	return list(State.ALL);
    }

    public SCIOTerminal getTerminal(String name) {
	try {
	    for (SCIOTerminal terminal : list()) {
		if (terminal.getName().equals(name)) {
		    return terminal;
		}
	    }
	    return null;
	} catch (SCIOException ignore) {
	    return null;
	}
    }

    public void waitForChange() throws SCIOException {
	waitForChange(0);
    }

    public abstract boolean waitForChange(long timeout) throws SCIOException;

}
