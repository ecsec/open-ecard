/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.scio;

import java.util.Collections;
import java.util.List;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.TerminalState;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Neil Crossley
 */
public class NFCCardWatcher implements TerminalWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardTerminals.class);

    private final SCIOTerminals nfcTerminals;
    private final SCIOTerminal nfcIntegratedTerminal;

    private boolean initialized = false;
    private volatile boolean isCardPresent = false;

    public NFCCardWatcher(SCIOTerminals terminals, SCIOTerminal terminal) {
	this.nfcTerminals = terminals;
	this.nfcIntegratedTerminal = terminal;
    }

    @Override
    public SCIOTerminals getTerminals() {
	return nfcTerminals;
    }

    @Override
    public List<TerminalState> start() throws SCIOException {
	LOG.debug("Entering start of nfc card watcher.");

	// allow this method to be called only once
	if (initialized) {
	    throw new IllegalStateException("Trying to initialize already initialized watcher instance.");
	}
	initialized = true;

	String name = nfcIntegratedTerminal.getName();
	// check if card present at integrated terminal
	if (nfcIntegratedTerminal.isCardPresent()) {
	    LOG.debug("Card is present.");
	    isCardPresent = true;
	    return Collections.singletonList(new TerminalState(name, true));
	    // otherwise card is not present at integrated terminal
	} else {
	    LOG.debug("No card is present.");
	    isCardPresent = false;
	    return Collections.singletonList(new TerminalState(name, false));
	}
    }

    @Override
    public TerminalWatcher.StateChangeEvent waitForChange() throws SCIOException {
	return waitForChange(0);
    }

    @Override
    public TerminalWatcher.StateChangeEvent waitForChange(long timeout) throws SCIOException {
	LOG.debug("NFCCardWatcher wait for change ...");

	// check if watcher is initialized
	if (!initialized) {
	    throw new IllegalStateException("Calling wait on uninitialized watcher instance.");
	}

	// set timeout to maximum when value says wait indefinitely
	if (timeout == 0) {
	    timeout = Long.MAX_VALUE;
	}

	// terminal name
	String terminalName = nfcIntegratedTerminal.getName();

	if (isCardPresent) {
	    LOG.debug("Waiting for card to become absent.");
	    boolean result = nfcIntegratedTerminal.waitForCardAbsent(timeout);
	    LOG.debug("Function waitForCardPresent()={}.", result);
	    if (result) {
		isCardPresent = false;
		return new TerminalWatcher.StateChangeEvent(TerminalWatcher.EventType.CARD_REMOVED, terminalName);
	    }
	} else {
	    LOG.debug("Waiting for card to become present.");
	    boolean result = nfcIntegratedTerminal.waitForCardPresent(timeout);
	    LOG.debug("Function waitForCardPresent()={}.", result);
	    if (result) {
		isCardPresent = true;
		return new TerminalWatcher.StateChangeEvent(TerminalWatcher.EventType.CARD_INSERTED, terminalName);
	    }
	}

	return new TerminalWatcher.StateChangeEvent();
    }

}
