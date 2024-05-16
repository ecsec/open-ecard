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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
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
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.ValueGenerators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openecard.addon.sal.SalStateView;
import org.openecard.common.sal.util.HexShim;


/**
 *
 * @author Tobias Wich
 */
public class SalStateManager implements SalStateView {

    private static final Logger LOG = LoggerFactory.getLogger(SalStateManager.class);

    private final Set<CardEntry> cards;
    private final Map<String, StateEntry> sessions;

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
	    return ce;
	}
    }

    public void addCard(ConnectedCardEntry connectedCardEntry) {
	if (!this.cards.add(connectedCardEntry)) {
	    this.cards.remove(connectedCardEntry);
	    this.cards.add(connectedCardEntry);
	    LOG.debug("Added card: {}", connectedCardEntry);
	}
    }

    public boolean removeCard(byte[] ctx, String ifdName, BigInteger idx) {
	LOG.debug("Request remove card: {}", new HexShim(ctx), ifdName, idx);
	Iterator<CardEntry> it = cards.iterator();
	boolean removed = false;
	while (it.hasNext()) {
	    CardEntry next = it.next();
	    if (next.matches(ctx, ifdName, idx)) {
		LOG.debug("Removing known matching card [{}, {}, {}]", new HexShim(ctx), ifdName, idx);
		it.remove();

		removed = true;
	    } else {
		LOG.debug("Did not match for removal: [{}, {}, {}]", new HexShim(next.ctxHandle), next.ifdName, next.slotIdx);
	    }
	}
	Map.Entry<String, StateEntry> matchingState = this.getStateEntry(ctx);
	if (matchingState != null) {
	    LOG.debug("Removing known matching state {}", new HexShim(ctx));
	    final StateEntry stateEntry = matchingState.getValue();
	    final ConnectedCardEntry connectedCard = stateEntry.getCardEntry();
	    if (connectedCard != null) {
		stateEntry.removeCard();
		this.removeCard(connectedCard.ctxHandle, connectedCard.ifdName, connectedCard.slotIdx);
	    }
	}
	if (!removed) {
	    LOG.debug("No matching for card [{}, {}, {}]", new HexShim(ctx), ifdName, idx);
	}
	// nothing removed
	return removed;
    }

    public List<CardEntry> listCardEntries() {
	return new ArrayList<>(cards);
    }

    @Override
    public boolean isDisconnected(byte[] contextHandle, String givenIfdName, byte[] givenSlotIndex) {
	if (contextHandle == null || givenSlotIndex == null || givenIfdName == null) {
	    return true;
	}

	for (ConnectionHandleType cardHandle : this.listCardHandles()) {
	    if (ByteUtils.compare(contextHandle, cardHandle.getContextHandle())
		    &&  givenIfdName.equals(cardHandle.getIFDName())
		    && ByteUtils.compare(givenSlotIndex, cardHandle.getSlotHandle())) {
		return false;
	    }
	}
	return true;
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
    public StateEntry createSession(byte[] contextHandle) {
       String session = ValueGenerators.genBase64Session();
       try {
           return createSession(session, contextHandle);
       } catch (SessionAlreadyExists ex) {
           LOG.warn("Randomly chosen session already exists, trying again with a different session.");
           return createSession(contextHandle);
       }
    }


    public StateEntry createSession(String session, byte[] contextHandle) throws SessionAlreadyExists {
	if (sessions.containsKey(session)) {
	    throw new SessionAlreadyExists(String.format("The requested session=%s already exists.", session));
	} else {
	    StateEntry newEntry = new StateEntry(session, contextHandle);
	    sessions.put(session, newEntry);
	    return newEntry;
	}
    }

    public StateEntry getSessionBySlotHandle(byte[] slotHandle) throws NoSuchSession {
	StateEntry found = null;
	for (StateEntry currentSession : this.sessions.values()) {
	    ConnectedCardEntry currentEntry = currentSession.getCardEntry();
	    if (currentEntry != null && ByteUtils.compare(slotHandle, currentEntry.getSlotHandle())) {
		return currentSession;
	    }
	}
	throw new NoSuchSession(String.format("The requested session=%s does not exist.", slotHandle));
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
	    return false;
	} else {
	    return destroySession(stateEntry.getKey());
	}
    }

    private boolean destroySession(String session) {
	return sessions.remove(session) != null;
    }

    @Override
    public List<ConnectionHandleType> listCardHandles() {
	List<ConnectionHandleType> results = new ArrayList<>(this.cards.size());

	for (CardEntry card : this.cards) {
	    results.add(card.copyHandle());
	}
	return results;
    }


}
