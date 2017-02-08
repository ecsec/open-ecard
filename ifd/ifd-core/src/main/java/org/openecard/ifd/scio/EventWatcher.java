/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.ifd.scio;

import iso.std.iso_iec._24727.tech.schema.ChannelHandleType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.ifd.scio.TerminalState;
import org.openecard.common.ifd.scio.TerminalWatcher;
import org.openecard.ifd.scio.wrapper.ChannelManager;
import org.openecard.ifd.scio.wrapper.SingleThreadChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class EventWatcher implements Callable<List<IFDStatusType>> {

    private static final Logger LOG = LoggerFactory.getLogger(EventWatcher.class);

    private final ChannelManager cm;
    private final long timeout;
    private final ChannelHandleType callback;
    private final TerminalWatcher watcher;

    private ArrayList<IFDStatusType> currentState;
    private List<IFDStatusType> expectedState;

    public EventWatcher(@Nonnull ChannelManager cm, long timeout, @Nullable ChannelHandleType callback)
	    throws SCIOException {
	this.cm = cm;
	this.timeout = timeout;
	this.callback = callback;
	this.watcher = cm.getTerminals().getWatcher();
    }

    @Nonnull
    public List<IFDStatusType> start() throws SCIOException {
	List<TerminalState> initialState = watcher.start();
	currentState = convert(initialState);
	// convert again to be safe from manipulation from the outside
	return convert(initialState);
    }

    public void setExpectedState(@Nonnull List<IFDStatusType> expectedState) {
	this.expectedState = expectedState;
    }

    public boolean isAsync() {
	return callback != null;
    }

    @Override
    public List<IFDStatusType> call() throws SCIOException {
	// compare expected status and see if we are already finished
	List<IFDStatusType> diff = compare(expectedState);
	if (! diff.isEmpty()) {
	    return diff;
	}

	// nothing happened so far, update our state
	ArrayList<TerminalWatcher.StateChangeEvent> events = new ArrayList<>();
	TerminalWatcher.StateChangeEvent event = watcher.waitForChange(timeout);
	if (! event.isCancelled()) {
	    do {
		events.add(event);
		// wait minimum amount of time to collect all remaining events
		event = watcher.waitForChange(1);
	    } while (! event.isCancelled());
	}
	// update internal state according to all events which have occurred
	for (TerminalWatcher.StateChangeEvent next : events) {
	    updateState(next);
	}

	// compare again
	diff = compare(expectedState);

	// TODO: implement callbacks

	return diff;
    }


    private void updateState(TerminalWatcher.StateChangeEvent event) {
	String name = event.getTerminal();
	if (event.getState() == TerminalWatcher.EventType.TERMINAL_ADDED) {
	    currentState.add(createEmptyState(name));
	} else {
	    Iterator<IFDStatusType> it = currentState.iterator();
	    while (it.hasNext()) {
		IFDStatusType next = it.next();
		SlotStatusType slot = next.getSlotStatus().get(0);
		if (next.getIFDName().equals(name)) {
		    switch (event.getState()) {
			case CARD_INSERTED:
			    try {
				SingleThreadChannel ch = new SingleThreadChannel(watcher.getTerminals().getTerminal(name), true);
				slot.setCardAvailable(true);
				slot.setATRorATS(ch.getChannel().getCard().getATR().getBytes());
				ch.shutdown();
			    } catch (NoSuchTerminal | SCIOException ex) {
				LOG.error("Failed to open master channel for terminal '" + name + "'.", ex);
				slot.setCardAvailable(false);
			    }
			    break;
			case CARD_REMOVED:
			    cm.closeMasterChannel(name);
			    slot.setCardAvailable(false);
			    break;
			case TERMINAL_REMOVED:
			    slot.setCardAvailable(false); // just in case
			    next.setConnected(false);
			    break;
		    }
		    // no need to look any further
		    break;
		}
	    }
	}
    }

    /**
     * Compares the current status against the expected status and returns the difference if any.
     * This function does not request new information from the hardware, but only uses the last state retrieved. The
     * result of this function can be used as events in
     * {@link org.openecard.ws.IFD#wait(iso.std.iso_iec._24727.tech.schema.Wait)}.
     *
     * @param expectedStatus Status known to the caller of the function.
     * @return The difference between the internal state of this object and the given reference status.
     */
    @Nonnull
    public List<IFDStatusType> compare(@Nonnull List<IFDStatusType> expectedStatus) {
	ArrayList<IFDStatusType> remaining = new ArrayList<>(currentState);

	for (IFDStatusType nextExpect : expectedStatus) {
	    Iterator<IFDStatusType> it = remaining.iterator();
	    boolean matchFound = false;
	    // see if the current state contains the terminal that is expected to be present
	    while (it.hasNext()) {
		IFDStatusType nextRemain = it.next();
		// found matching terminal
		if (nextRemain.getIFDName().equals(nextExpect.getIFDName())) {
		    matchFound = true;
		    // see if there is any difference between the two
		    if (isStateEqual(nextRemain, nextExpect)) {
			// no difference, so delete this entry
			it.remove();
		    }
		    break;
		}
	    }
	    // if remaining does not contain the expected status, the terminal was removed
	    if (! matchFound) {
		IFDStatusType removed = clone(nextExpect);
		removed.setIFDName(nextExpect.getIFDName());
		removed.setConnected(false);
		remaining.add(removed);
	    }
	}

	// clone entries, to prevent altering the state of this object from the outside
	return clone(remaining);
    }


    @Nonnull
    private ArrayList<IFDStatusType> convert(@Nonnull List<TerminalState> terminals) {
	ArrayList<IFDStatusType> result = new ArrayList<>(terminals.size());
	for (TerminalState next : terminals) {
	    result.add(convert(next));
	}
	return result;
    }

    @Nonnull
    private IFDStatusType convert(@Nonnull TerminalState next) {
	IFDStatusType result = new IFDStatusType();
	result.setIFDName(next.getName());
	result.setConnected(true);
	SlotStatusType slot = new SlotStatusType();
	result.getSlotStatus().add(slot);
	slot.setIndex(BigInteger.ZERO);
	slot.setCardAvailable(next.isCardPresent());
	return result;
    }

    @Nonnull
    private static List<IFDStatusType> clone(@Nonnull List<IFDStatusType> orig) {
	ArrayList<IFDStatusType> result = new ArrayList<>(orig.size());
	for (IFDStatusType next : orig) {
	    result.add(clone(next));
	}
	return result;
    }

    @Nonnull
    private static IFDStatusType clone(@Nonnull IFDStatusType orig) {
	IFDStatusType newStat = new IFDStatusType();
	newStat.setIFDName(orig.getIFDName());
	newStat.setConnected(orig.isConnected());

	for (SlotStatusType next : orig.getSlotStatus()) {
	    newStat.getSlotStatus().add(clone(next));
	}

	return newStat;
    }

    @Nonnull
    private static SlotStatusType clone(@Nonnull SlotStatusType orig) {
	SlotStatusType slot = new SlotStatusType();
	slot.setCardAvailable(orig.isCardAvailable());
	slot.setIndex(orig.getIndex());
	byte[] atr = orig.getATRorATS();
	if (atr != null) {
	    slot.setATRorATS(atr.clone());
	}
	return slot;
    }

    private static IFDStatusType createEmptyState(String name) {
	IFDStatusType status = new IFDStatusType();
	status.setIFDName(name);
	status.setConnected(true);
	status.getSlotStatus().add(createEmptySlot());
	return status;
    }

    private static SlotStatusType createEmptySlot() {
	SlotStatusType slot = new SlotStatusType();
	slot.setCardAvailable(false);
	slot.setIndex(BigInteger.ZERO);
	return slot;
    }

    private static boolean isStateEqual(@Nonnull IFDStatusType a, @Nonnull IFDStatusType b) {
	if (! a.getIFDName().equals(b.getIFDName())) {
	    return false;
	}
	if (! a.isConnected().equals(b.isConnected())) {
	    return false;
	}
	List<SlotStatusType> sa = a.getSlotStatus();
	List<SlotStatusType> sb = b.getSlotStatus();
	if (sa.size() != sb.size()) {
	    return false;
	}
	for (int i = 0; i < sa.size(); i++) {
	    if (! isSlotEqual(sa.get(i), sb.get(i))) {
		return false;
	    }
	}

	return true;
    }

    private static boolean isSlotEqual(@Nonnull SlotStatusType a, @Nonnull SlotStatusType b) {
	if (a.isCardAvailable() != b.isCardAvailable()) {
	    return false;
	}
	if (! a.getIndex().equals(b.getIndex())) {
	    return false;
	}
	// ATR is ignored, because it is not read by the conversion function
	return true;
//	if (a.getATRorATS() == null && b.getATRorATS() == null) {
//	    return true;
//	} else {
//	    // this method returns false when both are null, thatswhy the if before
//	    return ByteUtils.compare(a.getATRorATS(), b.getATRorATS());
//	}
    }

}
