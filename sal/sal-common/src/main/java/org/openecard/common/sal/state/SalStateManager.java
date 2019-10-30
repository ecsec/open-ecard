/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.sal.state;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.openecard.common.sal.state.cif.CardInfoWrapper;
import org.openecard.common.util.ValueGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class SalStateManager {

    private static final Logger LOG = LoggerFactory.getLogger(SalStateManager.class);

    private final Set<CardEntry> cards;
    private final Map<String, StateEntry> sessions;
    private String floatingSession = null;

    public SalStateManager() {
	this.cards = new TreeSet<>();
	this.sessions = new HashMap<>();
    }

    // card handling
    public CardEntry addCard(byte[] ctx, String ifdName, BigInteger slotIdx, CardInfoWrapper cif) throws DuplicateCardEntry {
	CardEntry ce = new CardEntry(ctx, ifdName, slotIdx, cif);
	if (cards.contains(ce)) {
	    LOG.error("Failed to add duplicate card entry.");
	    throw new DuplicateCardEntry(String.format("Failed to add duplicate card entry for device=%s.", ifdName));
	} else {
	    cards.add(ce);
	    Map.Entry<String, StateEntry> matchingState = this.getStateEntry(ctx);
	    StateEntry foundStateEntry;
	    if (matchingState == null && floatingSession != null) {
		foundStateEntry = sessions.get(floatingSession);
		floatingSession = null;
	    } else {
		foundStateEntry = matchingState.getValue();
		if (matchingState.getKey().equals(floatingSession)) {
		    floatingSession = null;
		}
	    }
	    if (foundStateEntry != null) {
		foundStateEntry.setConnectedCard(slotIdx.toByteArray(), ce);
	    }
	    return ce;
	}
    }

    public boolean removeCard(byte[] ctx, String ifdName, BigInteger idx) {
	Iterator<CardEntry> it = cards.iterator();
	boolean removed = false;
	while (it.hasNext()) {
	    CardEntry next = it.next();
	    if (next.matches(ctx, ifdName, idx)) {
		it.remove();

		removed = true;
	    }
	}
	Map.Entry<String, StateEntry> matchingState = this.getStateEntry(ctx);
	if (matchingState != null) {
	    matchingState.getValue().removeCard();
	}
	// nothing removed
	return removed;
    }

    public List<CardEntry> listCardEntries() {
	return new ArrayList<>(cards);
    }

    public CardEntry getCardEntry(byte[] ctx, String ifdName, BigInteger slotIdx) {
	for (CardEntry next : cards) {
	    if (next.matches(ctx, ifdName, slotIdx)) {
		return next;
	    }
	}
	// no match
	return null;
    }

    public CardEntry getCardEntry(byte[] ctx) {
	for (CardEntry next : cards) {
	    if (next.matchesContextHandle(ctx)) {
		return next;
	    }
	}
	// no match
	return null;
    }

    private Map.Entry<String, StateEntry> getStateEntry(byte[] contextHandle) {
	if (contextHandle == null) {
	    return null;
	}
	for (Map.Entry<String, StateEntry> entry : sessions.entrySet()) {
	    StateEntry stateEntry = entry.getValue();
	    if (Arrays.equals(stateEntry.getContextHandle(), contextHandle)) {
		return entry;
	    }
	}
	return null;
    }

    // Session handling
    public StateEntry createSession() {
	String session = ValueGenerators.genBase64Session();
	try {
	    return createSession(session);
	} catch (SessionAlreadyExists ex) {
	    LOG.warn("Randomly chosen session already exists, trying again with a different session.");
	    return createSession();
	}
    }

    public StateEntry createSession(String session) throws SessionAlreadyExists {
	if (sessions.containsKey(session)) {
	    throw new SessionAlreadyExists(String.format("The requested session=%s already exists.", session));
	} else {
	    if (this.floatingSession != null) {
		LOG.debug("Destroying previous floating session while creating new session.");
		destroySession(this.floatingSession);
	    }
	    StateEntry newEntry = new StateEntry(session);
	    sessions.put(session, newEntry);
	    this.floatingSession = session;
	    return newEntry;
	}
    }

    public StateEntry getSession(String session) throws NoSuchSession {
	StateEntry se = sessions.get(session);
	if (se != null) {
	    return se;
	} else {
	    throw new NoSuchSession(String.format("The requested session=%s does not exist.", session));
	}
    }

    public boolean destroySessionByContextHandle(byte[] contextHandle) {
	Map.Entry<String, StateEntry> stateEntry = this.getStateEntry(contextHandle);
	if (stateEntry == null) {
	    if (floatingSession != null) {
		String session = floatingSession;
		floatingSession = null;
		return destroySession(session);
	    }
	    return false;
	} else {
	    if (stateEntry.getKey().equals(this.floatingSession)) {
		this.floatingSession = null;
	    }
	    return sessions.remove(stateEntry.getKey()) != null;
	}
    }

    private boolean destroySession(String session) {
	return sessions.remove(session) != null;
    }

}
