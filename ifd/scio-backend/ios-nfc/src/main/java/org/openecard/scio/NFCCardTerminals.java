/****************************************************************************
 * Copyright (C) 2012-2018 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.scio;

import android.nfc.NfcAdapter;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.SCIOTerminals.State;
import org.openecard.common.ifd.scio.TerminalState;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * NFC implementation of smartcardio's CardTerminals interface.
 *
 * @author Dirk Petrautzki
 * @author Daniel Nemmert
 * @author Mike Prechtl
 */
public final class NFCCardTerminals implements SCIOTerminals {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardTerminals.class);

    private final NFCCardTerminal nfcTerminal;
    private final NfcAdapter adapter;

    public NFCCardTerminals(NfcAdapter adapter) {
	this.adapter = adapter;
	this.nfcTerminal = new NFCCardTerminal();
    }

    @Override
    public List<SCIOTerminal> list(State state) throws SCIOException {
	switch (state) {
	    case ALL:
		return Collections.singletonList(this.nfcTerminal);
	    case CARD_ABSENT:
		if (! nfcTerminal.isCardPresent()) {
		    return Collections.singletonList(this.nfcTerminal);
		}
		break;
	    case CARD_PRESENT:
		if (nfcTerminal.isCardPresent()) {
		    return Collections.singletonList(this.nfcTerminal);
		}
		break;
	}
	return Collections.emptyList();
    }

    @Override
    public List<SCIOTerminal> list() throws SCIOException {
        return list(State.ALL);
    }

    NFCCardTerminal getIntegratedNfcTerminal() {
	return nfcTerminal;
    }

    @Override
    public SCIOTerminal getTerminal(@Nonnull String name) throws NoSuchTerminal {
        if (nfcTerminal.getName().equals(name)) {
	    return getIntegratedNfcTerminal();
	}
	String errorMsg = String.format("There is no terminal with the name '%s' available.", name);
	throw new NoSuchTerminal(errorMsg);
    }

    @Override
    public TerminalWatcher getWatcher() throws SCIOException {
	return new NFCCardWatcher(this);
    }


    private static class NFCCardWatcher implements TerminalWatcher {

	private final NFCCardTerminals nfcTerminals;
	private final NFCCardTerminal nfcIntegratedTerminal;

	private boolean initialized = false;
	private volatile boolean isCardPresent = false;

	public NFCCardWatcher(NFCCardTerminals terminals) {
	    this.nfcTerminals = terminals;
	    this.nfcIntegratedTerminal = terminals.nfcTerminal;
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
	public StateChangeEvent waitForChange() throws SCIOException {
	    return waitForChange(0);
	}

	@Override
	public StateChangeEvent waitForChange(long timeout) throws SCIOException {
	    LOG.debug("NFCCardWatcher wait for change ...");

	    // check if watcher is initialized
	    if (! initialized) {
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
		    return new StateChangeEvent(EventType.CARD_REMOVED, terminalName);
		}
	    } else {
		LOG.debug("Waiting for card to become present.");
		boolean result = nfcIntegratedTerminal.waitForCardPresent(timeout);
		LOG.debug("Function waitForCardPresent()={}.", result);
		if (result) {
		    isCardPresent = true;
		    return new StateChangeEvent(EventType.CARD_INSERTED, terminalName);
		}
	    }

	    return new StateChangeEvent();
        }

    }

}
