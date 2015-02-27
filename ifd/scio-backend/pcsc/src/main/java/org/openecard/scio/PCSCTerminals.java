/****************************************************************************
 * Copyright (C) 2014-2015 TU Darmstadt.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.annotation.Nonnull;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
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
 * PC/SC terminals implementation of the SCIOTerminals.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
public class PCSCTerminals implements SCIOTerminals {

    private static final Logger logger = LoggerFactory.getLogger(PCSCTerminals.class);

    private final TerminalFactory terminalFactory;
    private final CardTerminals terminals;

    PCSCTerminals(@Nonnull TerminalFactory terminalFactory) {
	this.terminalFactory = terminalFactory;
	this.terminals = terminalFactory.terminals();
    }

    @Override
    public List<SCIOTerminal> list() throws SCIOException {
	return list(State.ALL);
    }

    @Override
    public List<SCIOTerminal> list(State state) throws SCIOException {
	try {
	    CardTerminals.State scState = convertState(state);
	    // get terminals with the specified state from the SmartcardIO
	    List<CardTerminal> scList = terminals.list(scState);
	    ArrayList<SCIOTerminal> list = convertTerminals(scList);
	    return Collections.unmodifiableList(list);
	} catch (CardException ex) {
	    throw new SCIOException("Failed to retrieve list from terminals instance.", ex);
	}
    }

    private CardTerminals.State convertState(@Nonnull State state) {
	switch (state) {
	    case ALL:
		return CardTerminals.State.ALL;
	    case CARD_PRESENT:
		return CardTerminals.State.CARD_PRESENT;
	    case CARD_ABSENT:
		return CardTerminals.State.CARD_ABSENT;
	    default:
		logger.error("Unknown state type requested: {}", state);
		throw new IllegalArgumentException("Invalid state type requested.");
	}
    }

    private SCIOTerminal convertTerminal(@Nonnull CardTerminal scTerminal) {
	// TODO: check if we should only return the same instances (caching) here
	return new PCSCTerminal(scTerminal);
    }

    private ArrayList<SCIOTerminal> convertTerminals(List<CardTerminal> terminals) {
	ArrayList<SCIOTerminal> result = new ArrayList<>(terminals.size());
	for (CardTerminal t : terminals) {
	    result.add(convertTerminal(t));
	}
	return result;
    }

    @Override
    public SCIOTerminal getTerminal(@Nonnull String name) throws NoSuchTerminal {
	CardTerminal t = terminals.getTerminal(name);
	if (t == null) {
	    throw new NoSuchTerminal(String.format("Terminal '%s' does not exist in the system.", name));
	} else {
	    return convertTerminal(t);
	}
    }

    @Override
    public TerminalWatcher getWatcher() throws SCIOException {
	return new PCSCWatcher(this);
    }


    ///
    /// Terminal Watcher part
    ///

    private static class PCSCWatcher implements TerminalWatcher {

	private final PCSCTerminals parent;
	private final PCSCTerminals own;

	private Queue<StateChangeEvent> pendingEvents;
	private Collection<String> terminals;
	private Collection<String> cardPresent;

	public PCSCWatcher(@Nonnull PCSCTerminals parent) {
	    this.parent = parent;
	    this.own = new PCSCTerminals(parent.terminalFactory);
	}

	@Override
	public SCIOTerminals getTerminals() {
	    // the terminal used to create the watcher
	    return parent;
	}

	@Override
	public List<TerminalState> start() throws SCIOException {
	    if (pendingEvents != null) {
		throw new IllegalStateException("Trying to initialize already initialized watcher instance.");
	    }
	    pendingEvents = new LinkedList<>();
	    terminals = new HashSet<>();
	    cardPresent = new HashSet<>();

	    try {
		// call wait for change and directly afterwards get current list of cards
		// with a bit of luck no change has happened in between and the list is coherent
		own.terminals.waitForChange(1);
		List<CardTerminal> javaTerminals = own.terminals.list();
		ArrayList<TerminalState> result = new ArrayList<>(javaTerminals.size());
		// fill sets according to state of the terminals
		for (CardTerminal next : javaTerminals) {
		    String name = next.getName();
		    terminals.add(name);
		    if (next.isCardPresent()) {
			cardPresent.add(name);
			result.add(new TerminalState(name, true));
		    } else {
			result.add(new TerminalState(name, false));
		    }
		}
		// return list of our terminals
		return result;
	    } catch (CardException ex) {
		throw new SCIOException("Failed to retrieve status from the PCSC system.", ex);
	    }
	}

	@Override
	public StateChangeEvent waitForChange() throws SCIOException {
	    return waitForChange(0);
	}

	@Override
	public StateChangeEvent waitForChange(long timeout) throws SCIOException {
	    if (pendingEvents == null) {
		throw new IllegalStateException("Calling wait on uninitialized watcher instance.");
	    }

	    // try to return any present events first
	    StateChangeEvent nextEvent = pendingEvents.poll();
	    if (nextEvent != null) {
		return nextEvent;
	    } else {
		boolean changed;
		try {
		    changed = own.terminals.waitForChange(timeout);
		} catch (CardException ex) {
		    throw new SCIOException("Error while waiting for a state change in the terminals.", ex);
		}
		if (! changed) {
		    return new StateChangeEvent();
		} else {
		    // something has changed, retrieve actual terminals from the system and see what has changed
		    Collection<String> newTerminals = new HashSet<>();
		    Collection<String> newCardPresent = new HashSet<>();
		    try {
			List<CardTerminal> newStates = own.terminals.list();
			for (CardTerminal next : newStates) {
			    String name = next.getName();
			    newTerminals.add(name);
			    if (next.isCardPresent()) {
				newCardPresent.add(name);
			    }
			}
		    } catch (CardException ex) {
			throw new SCIOException("Failed to retrieve status of the observed terminals.", ex);
		    }

		    // calculate what has actually happened
		    // removed cards
		    Collection<String> cardRemoved = subtract(cardPresent, newCardPresent);
		    Collection<StateChangeEvent> crEvents = createEvents(EventType.CARD_REMOVED, cardRemoved);
		    // removed terminals
		    Collection<String> termRemoved = subtract(terminals, newTerminals);
		    Collection<StateChangeEvent> trEvents = createEvents(EventType.TERMINAL_REMOVED, termRemoved);
		    // added terminals
		    Collection<String> termAdded = subtract(newTerminals, terminals);
		    Collection<StateChangeEvent> taEvents = createEvents(EventType.TERMINAL_ADDED, termAdded);
		    // added cards
		    Collection<String> cardAdded = subtract(newCardPresent, cardPresent);
		    Collection<StateChangeEvent> caEvents = createEvents(EventType.CARD_INSERTED, cardAdded);

		    // update internal status with the calculated state
		    terminals = newTerminals;
		    cardPresent = newCardPresent;
		    pendingEvents.addAll(crEvents);
		    pendingEvents.addAll(trEvents);
		    pendingEvents.addAll(taEvents);
		    pendingEvents.addAll(caEvents);
		    // use remove so we get an exception when no event has been recorded
		    // this would mean our algorithm is corrupt
		    return pendingEvents.remove();
		}
	    }
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
