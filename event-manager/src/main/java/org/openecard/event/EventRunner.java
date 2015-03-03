/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.enums.EventType;
import org.openecard.common.util.HandlerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Thread implementation checking the IFD status for changes after waiting for changes in the IFD.
 *
 * @author Tobias Wich
 */
public class EventRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EventRunner.class);
    private static final long[] RECOVER_TIME = { 1, 500, 2000, 5000, 30000, 60000 };

    private final EventManager evtManager;
    private final HandlerBuilder builder;

    private final List<IFDStatusType> initialState;
    private final List<IFDStatusType> currentState;

    public EventRunner(EventManager evtManager, HandlerBuilder builder) throws WSException {
	this.evtManager = evtManager;
	this.builder = builder;
	this.initialState = new ArrayList<>(evtManager.ifdStatus());
	this.currentState = new ArrayList<>();
    }


    @Override
    public void run() {
	// fire events for current state
	fireEvents(initialState);
	try {
	    int failCount = 0;
	    while (true) {
		try {
		    List<IFDStatusType> diff = evtManager.wait(currentState);
		    fireEvents(diff); // also updates current status
		    failCount = 0;
		} catch (WSException ex) {
		    logger.warn("IFD Wait returned with error.", ex);
		    // wait a bit and try again
		    int sleepIdx = failCount < RECOVER_TIME.length ? failCount : RECOVER_TIME.length - 1;
		    Thread.sleep(RECOVER_TIME[sleepIdx]);
		    failCount++;
		}
	    }
	} catch (InterruptedException ex) {
	    logger.info("Event thread interrupted.", ex);
	}
    }

    private IFDStatusType getCorresponding(String ifdName, List<IFDStatusType> statuses) {
	for (IFDStatusType next : statuses) {
	    if (next.getIFDName().equals(ifdName)) {
		return next;
	    }
	}
	return null;
    }
    private SlotStatusType getCorresponding(BigInteger idx, List<SlotStatusType> statuses) {
	for (SlotStatusType next : statuses) {
	    if (next.getIndex().equals(idx)) {
		return next;
	    }
	}
	return null;
    }


    private ConnectionHandleType makeConnectionHandle(String ifdName, BigInteger slotIdx) {
	ConnectionHandleType h = builder.setIfdName(ifdName)
		.setSlotIdx(slotIdx)
		.buildConnectionHandle();
	return h;
    }

    private ConnectionHandleType makeUnknownCardHandle(String ifdName, SlotStatusType status) {
	ConnectionHandleType h = builder
		.setIfdName(ifdName)
		.setSlotIdx(status.getIndex())
		.setCardType(ECardConstants.UNKNOWN_CARD)
		.setCardIdentifier(status.getATRorATS())
		.buildConnectionHandle();
	return h;
    }

    private void fireEvents(@Nonnull List<IFDStatusType> diff) {
	for (IFDStatusType term : diff) {
	    String ifdName = term.getIFDName();
	    boolean terminalPresent = term.isConnected();

	    if (! terminalPresent) {
		// TERMINAL REMOVED
		Iterator<IFDStatusType> it = currentState.iterator();
		while (it.hasNext()) {
		    IFDStatusType oldTerm = it.next();
		    if (oldTerm.getIFDName().equals(term.getIFDName())) {
			it.remove();
		    }
		}
		ConnectionHandleType h = makeConnectionHandle(ifdName, null);
		logger.debug("Found a terminal removed event ({}).", ifdName);
		evtManager.notify(EventType.TERMINAL_REMOVED, h);

	    } else {
		// find out if the terminal is new, or only a slot got updated
		IFDStatusType oldTerm = getCorresponding(ifdName, currentState);
		boolean terminalAdded = oldTerm == null;

		if (terminalAdded) {
		    // TERMINAL ADDED
		    currentState.add(term);
		    // TODO: make copy of term
		    oldTerm = new IFDStatusType();
		    oldTerm.setIFDName(ifdName);
		    oldTerm.setConnected(true);
		    // create event
		    ConnectionHandleType h = makeConnectionHandle(ifdName, null);
		    logger.debug("Found a terminal added event ({}).", ifdName);
		    evtManager.notify(EventType.TERMINAL_ADDED, h);
		}

		// check each slot
		for (SlotStatusType slot : term.getSlotStatus()) {
		    SlotStatusType oldSlot = getCorresponding(slot.getIndex(), oldTerm.getSlotStatus());
		    boolean cardPresent = slot.isCardAvailable();
		    boolean cardWasPresent = oldSlot != null && oldSlot.isCardAvailable();

		    if (cardPresent && ! cardWasPresent) {
			// CARD ADDED
			// copy slot and add to list
			SlotStatusType newSlot = oldSlot;
			if (newSlot == null) {
			    newSlot = new SlotStatusType();
			    oldTerm.getSlotStatus().add(newSlot);
			}
			newSlot.setIndex(slot.getIndex());
			newSlot.setCardAvailable(true);
			newSlot.setATRorATS(slot.getATRorATS());
			// create event
			logger.debug("Found a card insert event ({}).", ifdName);
			ConnectionHandleType handle = makeUnknownCardHandle(ifdName, newSlot);
			evtManager.notify(EventType.CARD_INSERTED, handle);
			if (evtManager.recognize) {
			    evtManager.threadPool.submit(new Recognizer(evtManager, handle));
			}

		    } else if (! terminalAdded && ! cardPresent && cardWasPresent) {
			// this makes only sense when the terminal was already there
			// CARD REMOVED
			// remove slot entry
			BigInteger idx = oldSlot.getIndex();
			Iterator<SlotStatusType> it = oldTerm.getSlotStatus().iterator();
			while (it.hasNext()) {
			    SlotStatusType next = it.next();
			    if (idx.equals(next.getIndex())) {
				it.remove();
				break;
			    }
			}
			logger.debug("Found a card removed event ({}).", ifdName);
			ConnectionHandleType h = makeConnectionHandle(ifdName, idx);
			evtManager.notify(EventType.CARD_REMOVED, h);
		    }
		}
	    }
	}
    }

}
