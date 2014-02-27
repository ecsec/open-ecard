/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.enums.EventType;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.common.util.IFDStatusDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Thread implementation checking the IFD status for changes after waiting for changes in the IFD.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class EventRunner implements Callable<Void> {

    private static final Logger logger = LoggerFactory.getLogger(EventRunner.class);

    private final EventManager evtManager;
    private final HandlerBuilder builder;

    private List<IFDStatusType> oldStati;
    private Future<Boolean> wait;

    public EventRunner(EventManager evtManager, HandlerBuilder builder) {
	this.evtManager = evtManager;
	this.builder = builder;
	this.oldStati = new ArrayList<IFDStatusType>();
    }


    @Override
    public Void call() throws Exception {
	try {
	    while (true) {
		wait = evtManager.threadPool.submit(new WaitFuture(evtManager));
		try {
		    List<IFDStatusType> newStati = evtManager.ifdStatus();
		    IFDStatusDiff diff = new IFDStatusDiff(oldStati);
		    diff.diff(newStati, true);
		    if (diff.hasChanges()) {
			logger.debug("Difference in status detected.");
			analyzeEvent(oldStati, diff.result());
		    }
		    oldStati = newStati;
		} catch (WSException ex) {
		    logger.warn("GetStatus returned with error.", ex);
		} catch (Exception ex) {
		    logger.error("Unexpected exception occurred.", ex);
		    throw ex;
		}
		// wait for change if it hasn't already happened
		wait.get();
	    }
	} finally {
	    if (wait != null) {
		wait.cancel(true);
	    }
	}
    }

    private void analyzeEvent(List<IFDStatusType> oldS, List<IFDStatusType> changed) {
	logger.debug("Analyzing IFD event.");
	for (IFDStatusType next : changed) {
	    String ifdName = next.getIFDName();
	    IFDStatusType counterPart = getCorresponding(ifdName, oldS);

	    // next is completely new
	    if (counterPart == null) {
		ConnectionHandleType h = makeConnectionHandle(ifdName, null);
		logger.debug("Found a terminal added event ({}).", ifdName);
		evtManager.notify(EventType.TERMINAL_ADDED, h);
		// create empty counterPart so all slots raise events
		counterPart = new IFDStatusType();
		counterPart.setIFDName(ifdName);
	    }

	    // inspect every slot
	    for (SlotStatusType nextSlot : next.getSlotStatus()) {
		SlotStatusType counterPartSlot = getCorresponding(nextSlot.getIndex(), counterPart.getSlotStatus());
		if (counterPartSlot == null) {
		    // slot is new, send event when card is present
		    if (nextSlot.isCardAvailable()) {
			logger.debug("Found a card insert event ({}).", ifdName);
			ConnectionHandleType handle = makeUnknownCardHandle(ifdName, nextSlot);
			evtManager.notify(EventType.CARD_INSERTED, handle);
			if (evtManager.recognize) {
			    evtManager.threadPool.submit(new Recognizer(evtManager, handle));
			}
		    }
		} else {
		    // compare slot for difference
		    if (nextSlot.isCardAvailable() != counterPartSlot.isCardAvailable()) {
			if (nextSlot.isCardAvailable()) {
			    logger.debug("Found a card insert event ({}).", ifdName);
			    ConnectionHandleType handle = makeUnknownCardHandle(ifdName, nextSlot);
			    evtManager.notify(EventType.CARD_INSERTED, handle);
			    if (evtManager.recognize) {
				evtManager.threadPool.submit(new Recognizer(evtManager, handle));
			    }
			} else {
			    logger.debug("Found a card removed event ({}).", ifdName);
			    ConnectionHandleType h = makeConnectionHandle(ifdName, nextSlot.getIndex());
			    evtManager.notify(EventType.CARD_REMOVED, h);
			}
		    } else {
			// compare atr
			if (nextSlot.isCardAvailable()) {
			    if (! Arrays.equals(nextSlot.getATRorATS(), counterPartSlot.getATRorATS())) {
				if (logger.isDebugEnabled()) {
				    logger.debug("Ignoring: Found a card changed event ({}).", ifdName);
				    String newATR = ByteUtils.toHexString(nextSlot.getATRorATS());
				    String oldATR = ByteUtils.toHexString(counterPartSlot.getATRorATS());
				    logger.debug("Dump ATR\nnew: {}\nold: {}", newATR, oldATR);
				}
				//evtManager.sendAsyncEvents(ifdName, nextSlot, EventType.CARD_RECOGNIZED);
			    }
			}
		    }
		}
	    }

	    // remove terminal
	    if (! next.isConnected()) {
		ConnectionHandleType h = makeConnectionHandle(ifdName, null);
		logger.debug("Found a terminal removed event ({}).", ifdName);
		evtManager.notify(EventType.TERMINAL_REMOVED, h);
	    }
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

}
