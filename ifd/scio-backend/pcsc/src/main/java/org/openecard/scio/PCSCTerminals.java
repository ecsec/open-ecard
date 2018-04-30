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
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOErrorCode;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.SCIOTerminal;
import org.openecard.common.ifd.scio.SCIOTerminals;
import org.openecard.common.ifd.scio.TerminalState;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.openecard.common.util.Pair;
import static org.openecard.scio.PCSCExceptionExtractor.getCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * PC/SC terminals implementation of the SCIOTerminals.
 *
 * @author Wael Alkhatib
 * @author Tobias Wich
 */
public class PCSCTerminals implements SCIOTerminals {

    private static final Logger LOG = LoggerFactory.getLogger(PCSCTerminals.class);
    private static final long WAIT_DELTA = 1500;

    private final PCSCFactory terminalFactory;
    private CardTerminals terminals;

    PCSCTerminals(@Nonnull PCSCFactory terminalFactory) {
	this.terminalFactory = terminalFactory;
	loadTerminals();
    }

    private void reloadFactory() {
	terminalFactory.reloadPCSC();
	loadTerminals();
    }

    private void loadTerminals() {
	terminals = terminalFactory.getRawFactory().terminals();
    }

    @Override
    public List<SCIOTerminal> list() throws SCIOException {
	return list(State.ALL);
    }

    @Override
    public List<SCIOTerminal> list(State state) throws SCIOException {
	return list(state, true);
    }

    public List<SCIOTerminal> list(State state, boolean firstTry) throws SCIOException {
	LOG.trace("Entering list().");
	try {
	    CardTerminals.State scState = convertState(state);
	    // get terminals with the specified state from the SmartcardIO
	    List<CardTerminal> scList = terminals.list(scState);
	    ArrayList<SCIOTerminal> list = convertTerminals(scList);
	    LOG.trace("Leaving list().");
	    return Collections.unmodifiableList(list);
	} catch (CardException ex) {
	    SCIOErrorCode code = getCode(ex);
	    if (code == SCIOErrorCode.SCARD_E_NO_READERS_AVAILABLE) {
		LOG.debug("No reader available exception.");
		return Collections.emptyList();
	    } else if (code == SCIOErrorCode.SCARD_E_NO_SERVICE || code == SCIOErrorCode.SCARD_E_SERVICE_STOPPED) {
		if (firstTry) {
		    LOG.debug("No service available exception, reloading PCSC and trying again.");
		    reloadFactory();
		    return list(state, false);
		} else {
		    LOG.debug("No service available exception, returning empty list.");
		    return Collections.emptyList();
		}
	    }
	    String msg = "Failed to retrieve list from terminals instance.";
	    LOG.error(msg, ex);
	    throw new SCIOException(msg, code, ex);
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
		LOG.error("Unknown state type requested: {}", state);
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
	    LOG.trace("Entering start().");
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
		LOG.debug("Detecting initial terminal status.");
		for (CardTerminal next : javaTerminals) {
		    String name = next.getName();
		    boolean cardInserted = next.isCardPresent();
		    LOG.debug("Terminal='{}' cardPresent={}", name, cardInserted);
		    terminals.add(name);
		    if (cardInserted) {
			cardPresent.add(name);
			result.add(new TerminalState(name, true));
		    } else {
			result.add(new TerminalState(name, false));
		    }
		}
		// return list of our terminals
		LOG.trace("Leaving start() with {} states.", result.size());
		return Collections.unmodifiableList(result);
	    } catch (CardException ex) {
		String msg = "Failed to retrieve status from the PCSC system.";
		SCIOErrorCode code = getCode(ex);
		if (code == SCIOErrorCode.SCARD_E_NO_READERS_AVAILABLE) {
		    LOG.debug("No reader available exception.");
		    return Collections.emptyList();
		} else if (code == SCIOErrorCode.SCARD_E_NO_SERVICE || code == SCIOErrorCode.SCARD_E_SERVICE_STOPPED || code == SCIOErrorCode.SCARD_E_INVALID_HANDLE) {
		    LOG.debug("No service available exception, reloading PCSC and returning empty list.");
		    parent.reloadFactory();
		    own.loadTerminals();
		    return Collections.emptyList();
		} else if (code == SCIOErrorCode.SCARD_E_INVALID_HANDLE) {
		    // don't log in order to prevent flooding
		} else {
		    LOG.error(msg, ex);
		}
		throw new SCIOException(msg, code, ex);
	    } catch (IllegalStateException ex) {
		LOG.debug("No reader available exception.");
		return Collections.emptyList();
	    }
	}

	@Override
	public StateChangeEvent waitForChange() throws SCIOException {
	    return waitForChange(0);
	}

	@Override
	public StateChangeEvent waitForChange(long timeout) throws SCIOException {
	    LOG.trace("Entering waitForChange() with timeout={}.", timeout);
	    if (pendingEvents == null) {
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
		    LOG.trace("Leaving waitForChange() with queued event.");
		    return nextEvent;
		} else {
		    Pair<Boolean, Boolean> waitResult;
		    try {
			waitResult = internalWait(timeout);
		    } catch (CardException ex) {
			String msg = "Error while waiting for a state change in the terminals.";
			LOG.error(msg, ex);
			throw new SCIOException(msg, getCode(ex), ex);
		    }
		    boolean changed = waitResult.p1;
		    boolean error = waitResult.p2;

		    if (! changed) {
			LOG.trace("Leaving waitForChange() with no event.");
			return new StateChangeEvent();
		    } else {
			// something has changed, retrieve actual terminals from the system and see what has changed
			Collection<String> newTerminals = new HashSet<>();
			Collection<String> newCardPresent = new HashSet<>();
			// only ask for terminals if there is no error
			if (! error) {
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
				String msg = "Failed to retrieve status of the observed terminals.";
				LOG.error(msg, ex);
				throw new SCIOException(msg, getCode(ex), ex);
			    }
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
			if (! pendingEvents.isEmpty()) {
			LOG.trace("Leaving waitForChange() with fresh event.");
			    return pendingEvents.remove();
			}
		    }
		}

		// calculate new timeout value
		long finishTime = System.nanoTime();
		long delta = finishTime - startTime;
		timeout = timeout - (delta / 1000_000);
		LOG.trace("Start wait loop again with reduced timeout value ({} ms).", timeout);
	    }

	    LOG.trace("Leaving waitForChange() with no event.");
	    return new StateChangeEvent();
	}

	private void sleep(long millis) throws SCIOException {
	    try {
		Thread.sleep(millis);
	    } catch (InterruptedException ex2) {
		String msg = "Wait interrupted by another thread.";
		throw new SCIOException(msg, SCIOErrorCode.SCARD_E_SERVICE_STOPPED);
	    }
	}

	/**
	 * Wait for events in the system.
	 * The SmartcardIO wait function only reacts on card events, new and removed terminals go unseen. in order to
	 * fix this, we wait only a short time and check the terminal list periodically.
	 *
	 * @param timeout Timeout values as in {@link #waitForChange(long)}.
	 * @return The first value is the changed flag . It is {@code true} if a change the terminals happened,
	 *   {@code false} if a timeout occurred. <br>
	 *   The second value is the error flag. It is {@code true} if an error was used to indicate that no terminals
	 *   are connected, {@code false} otherwise.
	 * @throws CardException Thrown if any error related to the SmartcardIO occured.
	 * @throws SCIOException Thrown if the thread was interrupted. Contains the code
	 *   {@link SCIOErrorCode#SCARD_E_SERVICE_STOPPED}.
	 */
	private Pair<Boolean, Boolean> internalWait(long timeout) throws CardException, SCIOException {
	    // the SmartcardIO wait function only reacts on card events, new and removed terminals go unseen
	    // to fix this, we wait only a short time and check the terminal list periodically
	    if (timeout < 0) {
		throw new IllegalArgumentException("Negative timeout value given.");
	    } else if (timeout == 0) {
		timeout = Long.MAX_VALUE;
	    }

	    while (true) {
		if (timeout == 0) {
		    // waited for all time and nothing happened
		    return new Pair<>(false, false);
		}
		// calculate next wait slice
		long waitTime;
		if (timeout < WAIT_DELTA) {
		    waitTime = timeout;
		    timeout = 0;
		} else {
		    timeout = timeout - WAIT_DELTA;
		    waitTime = WAIT_DELTA;
		}

		try {
		    // check if there is something new on the card side
		    // due to the wait call blocking every other smartcard operation, we only wait for the actual events
		    // very shortly and sleep for the rest of the time
		    boolean change = own.terminals.waitForChange(1);
		    if (change) {
			return new Pair<>(true, false);
		    }
		    sleep(waitTime);
		    // try again after sleeping
		    change = own.terminals.waitForChange(1);
		    if (change) {
			return new Pair<>(true, false);
		    }
		} catch (CardException ex) {
		    switch (getCode(ex)) {
			case SCARD_E_NO_SERVICE:
			case SCARD_E_SERVICE_STOPPED:
			    LOG.debug("No service available exception, reloading PCSC.");
			    parent.reloadFactory();
			    own.loadTerminals();
			case SCARD_E_NO_READERS_AVAILABLE:
			    // send events that everything is removed if there are any terminals connected right now
			    if (! terminals.isEmpty()) {
				return new Pair<>(true, true);
			    } else {
				LOG.debug("Waiting for PCSC system to become available again.");
				// if nothing changed, wait a bit and try again
				sleep(waitTime);
				continue;
			    }
			default:
			    throw ex;
		    }
		} catch (IllegalStateException ex) {
		    // send events that everything is removed if there are any terminals connected right now
		    if (! terminals.isEmpty()) {
			return new Pair<>(true, true);
		    } else {
			LOG.debug("Waiting for PCSC system to become available again.");
			// if nothing changed, wait a bit and try again
			sleep(waitTime);
			continue;
		    }
		}

		// check if there is something new on the terminal side
		ArrayList<CardTerminal> currentTerms = new ArrayList<>(own.terminals.list());
		if (currentTerms.size() != terminals.size()) {
		    return new Pair<>(true, false);
		}
		// same size, but still compare terminal names
		HashSet<String> newTermNames = new HashSet<>();
		for (CardTerminal next : currentTerms) {
		    newTermNames.add(next.getName());
		}
		int sizeBefore = newTermNames.size();
		if (sizeBefore != terminals.size()) {
		    return new Pair<>(false, false);
		}
		newTermNames.addAll(terminals);
		int sizeAfter = newTermNames.size();
		if (sizeBefore != sizeAfter) {
		    return new Pair<>(false, false);
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
