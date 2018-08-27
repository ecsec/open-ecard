/****************************************************************************
 * Copyright (C) 2012-2017 HS Coburg.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import javax.annotation.Nonnull;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOErrorCode;
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
public class NFCCardTerminals implements SCIOTerminals {

    private static final Logger LOG = LoggerFactory.getLogger(NFCCardTerminals.class);

    private final List<SCIOTerminal> nfcTerminals = new ArrayList<>();

    private final NfcAdapter adapter;

    public NFCCardTerminals(NfcAdapter adapter) {
	String nameOfIntegratedNfc = NFCCardTerminal.STD_TERMINAL_NAME;

	this.adapter = adapter;
	this.nfcTerminals.add(new NFCCardTerminal(nameOfIntegratedNfc));
    }

    @Override
    public List<SCIOTerminal> list(State arg0) throws SCIOException {
	switch (arg0) {
	    case ALL:
		return this.nfcTerminals;
	    case CARD_ABSENT:
		if (! getIntegratedNfcTerminal().isCardPresent()) {
		    return this.nfcTerminals;
		}
		break;
	    case CARD_PRESENT:
		if (getIntegratedNfcTerminal().isCardPresent()) {
		    return this.nfcTerminals;
		}
		break;
	}
	return Collections.emptyList();
    }

    @Override
    public List<SCIOTerminal> list() throws SCIOException {
        return list(State.ALL);
    }

    public NFCCardTerminal getIntegratedNfcTerminal() {
	return (NFCCardTerminal) nfcTerminals.get(0);
    }

    @Override
    public SCIOTerminal getTerminal(@Nonnull String name) throws NoSuchTerminal {
        if (getIntegratedNfcTerminal().getName().equals(name)) {
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

	private Queue<StateChangeEvent> pendingEvents;
	private Collection<String> cardPresent;
	private boolean initialized = false;

	public NFCCardWatcher(NFCCardTerminals terminals) {
	    this.nfcTerminals = terminals;
	    this.nfcIntegratedTerminal = nfcTerminals.getIntegratedNfcTerminal();
	}

	@Override
	public SCIOTerminals getTerminals() {
	    return nfcTerminals;
	}

	@Override
	public List<TerminalState> start() throws SCIOException {
	    LOG.debug("Entering start of nfc card watcher.");

	    ArrayList<TerminalState> result = new ArrayList<>();

	    // check if start is called the second time
	    if (pendingEvents != null) {
		throw new IllegalStateException("Trying to initialize already initialized watcher instance.");
	    }

	    // initialize
	    initialized = true;
	    pendingEvents = new LinkedList<>();
	    cardPresent = new HashSet<>();

	    // Check if NFC Adapter is present and enabled
	    boolean isEnabled = nfcTerminals.adapter.isEnabled();

	    // check if nfc adapter is null
	    if (nfcTerminals.adapter == null) {
		String msg = "No nfc Adapter on this Android Device.";
		throw new SCIOException(msg, SCIOErrorCode.SCARD_E_NO_READERS_AVAILABLE);
	    }

	    // check if nfc is enabled
	    if (! isEnabled) {
		throw new SCIOException("Nfc Adapter not enabled.", SCIOErrorCode.SCARD_E_NO_SERVICE);
	    }

	    if (nfcTerminals.adapter != null && isEnabled) {
		String name = nfcIntegratedTerminal.getName();

		// check if card present at integrated terminal
		if (nfcIntegratedTerminal.isCardPresent()) {
		    LOG.debug("Card is present.");
		    cardPresent.add(name);
		    result.add(new TerminalState(name, true));
		// otherwise card is not present at integrated terminal
		} else {
		    LOG.debug("No card is present.");
		    result.add(new TerminalState(name, false));
		}
	    }

	    LOG.trace("Leaving start() with {} states.", result.size());
	    return result;
	}

	@Override
	public StateChangeEvent waitForChange() throws SCIOException {
	    return waitForChange(0);
	}

	@Override
	public StateChangeEvent waitForChange(long timeout) throws SCIOException {
	    LOG.debug("NFCCardWatcher wait for change...");

	    // check if watcher is initialized
	    if (! initialized) {
		throw new IllegalStateException("Calling wait on uninitialized watcher instance.");
	    }

	    // set timeout to maximum when value says wait indefinitely
	    if (timeout == 0) {
		timeout = Long.MAX_VALUE;
	    }

	    while (timeout > 0) {
		long startTime = System.nanoTime();

		// try to return any present events first
		StateChangeEvent nextEvent = pendingEvents.poll();

		if (nextEvent != null) {
		    LOG.trace("Leaving wait for change with queued event.");
		    return nextEvent;
		} else {
		    Collection<String> newCardPresent = new HashSet<>();

		    // check if card is present to the present time
		    if (nfcIntegratedTerminal.isCardPresent()) {
			LOG.debug("New card is present.");
			newCardPresent.add(nfcIntegratedTerminal.getName());
		    }

		    // check if card is removed
		    Collection<String> cardRemoved = subtract(cardPresent, newCardPresent);
		    Collection<StateChangeEvent> crEvents = createEvents(EventType.CARD_REMOVED, cardRemoved);

		    // check if card is added
		    Collection<String> cardAdded = subtract(newCardPresent, cardPresent);
		    Collection<StateChangeEvent> caEvents = createEvents(EventType.CARD_INSERTED, cardAdded);

		    // update internal status with the calculated state
		    cardPresent = newCardPresent;

		    pendingEvents.addAll(crEvents);
		    pendingEvents.addAll(caEvents);

		    try {
			StateChangeEvent event = pendingEvents.remove();
			LOG.info("StateChangeEvent: " + event.getState() + " " + event.getTerminal());
			return event;
		    } catch (NoSuchElementException ex) {
			LOG.debug("No card state changes.");
		    }
		}

		long finishTime = System.nanoTime();
		long delta = finishTime - startTime;
		timeout = timeout - (delta / 1000_000);
	    }

	    return new StateChangeEvent();
        }

	private static <T> Collection<T> subtract(Collection<T> a, Collection<T> b) {
	    HashSet<T> result = new HashSet<>(a);
	    result.removeAll(b);
	    return result;
	}

	private static Collection<StateChangeEvent> createEvents(EventType type, Collection<String> list) {
	    Collection<StateChangeEvent> result = new ArrayList<>(list.size());
	    for (String next : list) {
		result.add(new StateChangeEvent(type, next));
	    }
	    return result;
	}
    }
}
