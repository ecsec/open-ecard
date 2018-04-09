/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

package org.openecard.ifd.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilities;
import iso.std.iso_iec._24727.tech.schema.GetIFDCapabilitiesResponse;
import iso.std.iso_iec._24727.tech.schema.GetStatus;
import iso.std.iso_iec._24727.tech.schema.GetStatusResponse;
import iso.std.iso_iec._24727.tech.schema.IFDCapabilitiesType;
import iso.std.iso_iec._24727.tech.schema.IFDStatusType;
import iso.std.iso_iec._24727.tech.schema.KeyPadCapabilityType;
import iso.std.iso_iec._24727.tech.schema.ListIFDs;
import iso.std.iso_iec._24727.tech.schema.ListIFDsResponse;
import iso.std.iso_iec._24727.tech.schema.SlotStatusType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.common.ECardConstants;
import org.openecard.common.WSHelper;
import org.openecard.common.WSHelper.WSException;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.event.IfdEventObject;
import org.openecard.common.ifd.scio.NoSuchTerminal;
import org.openecard.common.ifd.scio.SCIOException;
import org.openecard.common.util.ByteUtils;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.ifd.scio.wrapper.ChannelManager;
import org.openecard.ifd.scio.wrapper.SingleThreadChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Thread implementation checking the IFD status for changes after waiting for changes in the IFD.
 *
 * @author Tobias Wich
 */
public class IfdEventRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(IfdEventRunner.class);
    private static final long[] RECOVER_TIME = { 1, 500, 2000, 5000 };

    private final Environment env;
    private final IfdEventManager evtManager;
    private final HandlerBuilder builder;
    private final ChannelManager cm;
    private final byte[] ctxHandle;

    private final List<IFDStatusType> initialState;
    private final List<IFDStatusType> currentState;

    private boolean stopped;

    public IfdEventRunner(Environment env, IfdEventManager evtManager, HandlerBuilder builder, ChannelManager cm,
	    byte[] ctxHandle) throws WSException {
	this.env = env;
	this.evtManager = evtManager;
	this.builder = builder;
	this.cm = cm;
	this.ctxHandle = ctxHandle;
	this.initialState = new ArrayList<>(ifdStatus());
	this.currentState = new ArrayList<>();
	this.stopped = false;
    }


    @Nonnull
    private List<IFDStatusType> ifdStatus() throws WSException {
	LOG.debug("Requesting terminal names.");
	ListIFDs listReq = new ListIFDs();
	listReq.setContextHandle(ctxHandle);
	ListIFDsResponse ifds = env.getIFD().listIFDs(listReq);
	WSHelper.checkResult(ifds);

	LOG.debug("Requesting status for all terminals found.");
	ArrayList<IFDStatusType> result = new ArrayList<>();
	for (String ifd : ifds.getIFDName()) {
	    GetStatus status = new GetStatus();
	    status.setContextHandle(ctxHandle);
	    status.setIFDName(ifd);
	    GetStatusResponse statusResponse = env.getIFD().getStatus(status);

	    try {
		WSHelper.checkResult(statusResponse);
		result.addAll(statusResponse.getIFDStatus());
	    } catch (WSException ex) {
		String msg = "Failed to request status from terminal, assuming no card present.";
		LOG.error(msg, ex);
		IFDStatusType is = new IFDStatusType();
		is.setIFDName(ifd);
		result.add(is);
	    }
	}
	return result;
    }

    @Override
    public void run() {
	// fire events for current state
	fireEvents(initialState);
	try {
	    int failCount = 0;
	    while (! stopped) {
		try {
		    List<IFDStatusType> diff = evtManager.wait(currentState);
		    fireEvents(diff); // also updates current status
		    failCount = 0;
		} catch (WSException ex) {
		    LOG.warn("IFD Wait returned with error.", ex);
		    // wait a bit and try again
		    int sleepIdx = failCount < RECOVER_TIME.length ? failCount : RECOVER_TIME.length - 1;
		    Thread.sleep(RECOVER_TIME[sleepIdx]);
		    failCount++;
		}
	    }
	} catch (InterruptedException ex) {
	    LOG.info("Event thread interrupted.", ex);
	}
	LOG.info("Stopping IFD event thread.");
    }

    /**
     * Set stopped flag, so that the loop stops when another iteration is repeated.
     * This flag is used as a failsafe when the InterruptedException gets lost du to wrong code in the IFD stack.
     */
    public void setStoppedFlag() {
	stopped = true;
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


    private ConnectionHandleType makeConnectionHandle(String ifdName, BigInteger slotIdx,
	    IFDCapabilitiesType slotCapabilities) {
	ConnectionHandleType h = builder.setIfdName(ifdName)
		.setSlotIdx(slotIdx)
		.setProtectedAuthPath(hasKeypad(slotCapabilities))
		.buildConnectionHandle();
	return h;
    }

    private ConnectionHandleType makeUnknownCardHandle(String ifdName, SlotStatusType status,
	    IFDCapabilitiesType slotCapabilities) {
	ConnectionHandleType h = builder
		.setIfdName(ifdName)
		.setSlotIdx(status.getIndex())
		.setCardType(ECardConstants.UNKNOWN_CARD)
		.setCardIdentifier(status.getATRorATS())
		.setProtectedAuthPath(hasKeypad(slotCapabilities))
		.buildConnectionHandle();
	return h;
    }

    private void fireEvents(@Nonnull List<IFDStatusType> diff) {
	for (IFDStatusType term : diff) {
	    String ifdName = term.getIFDName();

	    // find out if the terminal is new, or only a slot got updated
	    IFDStatusType oldTerm = getCorresponding(ifdName, currentState);
	    boolean terminalAdded = oldTerm == null;
	    IFDCapabilitiesType slotCapabilities = getCapabilities(ifdName);

	    if (terminalAdded) {
		// TERMINAL ADDED
		// make copy of term
		oldTerm = new IFDStatusType();
		oldTerm.setIFDName(ifdName);
		oldTerm.setConnected(true);
		// add to current list
		currentState.add(oldTerm);
		// create event
		ConnectionHandleType h = makeConnectionHandle(ifdName, null, slotCapabilities);
		LOG.debug("Found a terminal added event ({}).", ifdName);
		env.getEventDispatcher().notify(EventType.TERMINAL_ADDED, new IfdEventObject(h));
	    }

	    // check each slot
	    for (SlotStatusType slot : term.getSlotStatus()) {
		SlotStatusType oldSlot = getCorresponding(slot.getIndex(), oldTerm.getSlotStatus());
		boolean cardPresent = slot.isCardAvailable();
		boolean cardWasPresent = oldSlot != null && oldSlot.isCardAvailable();

		if (cardPresent && ! cardWasPresent) {
		    // CARD INSERTED
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
		    LOG.debug("Found a card insert event ({}).", ifdName);
		    LOG.info("Card with ATR={} inserted.", ByteUtils.toHexString(slot.getATRorATS()));
		    ConnectionHandleType handle = makeUnknownCardHandle(ifdName, newSlot, slotCapabilities);
		    env.getEventDispatcher().notify(EventType.CARD_INSERTED, new IfdEventObject(handle));
		    try {
			SingleThreadChannel ch = cm.openMasterChannel(ifdName);
			if (evtManager.isRecognize()) {
			    String proto = ch.getChannel().getCard().getProtocol().toUri();
			    evtManager.threadPool.submit(new Recognizer(env, handle, proto));
			}
		    } catch (NoSuchTerminal | SCIOException ex) {
			LOG.error("Failed to connect card, nevertheless sending CARD_INSERTED event.", ex);
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
		    LOG.debug("Found a card removed event ({}).", ifdName);
		    ConnectionHandleType h = makeConnectionHandle(ifdName, idx, slotCapabilities);
		    env.getEventDispatcher().notify(EventType.CARD_REMOVED, new IfdEventObject(h));
		}
	    }

	    // terminal removed event comes after card removed events
	    boolean terminalPresent = term.isConnected();
	    if (! terminalPresent) {
		// TERMINAL REMOVED
		Iterator<IFDStatusType> it = currentState.iterator();
		while (it.hasNext()) {
		    IFDStatusType toDel = it.next();
		    if (toDel.getIFDName().equals(term.getIFDName())) {
			it.remove();
		    }
		}
		ConnectionHandleType h = makeConnectionHandle(ifdName, null, slotCapabilities);
		LOG.debug("Found a terminal removed event ({}).", ifdName);
		env.getEventDispatcher().notify(EventType.TERMINAL_REMOVED, new IfdEventObject(h));
	    }
	}
    }

    @Nullable
    private IFDCapabilitiesType getCapabilities(String ifdName) {
	GetIFDCapabilities req = new GetIFDCapabilities();
	req.setContextHandle(ctxHandle);
	req.setIFDName(ifdName);
	GetIFDCapabilitiesResponse res = (GetIFDCapabilitiesResponse) env.getDispatcher().safeDeliver(req);
	return res.getIFDCapabilities();
    }

    private boolean hasKeypad(@Nullable IFDCapabilitiesType capabilities) {
	if (capabilities != null) {
	    List<KeyPadCapabilityType> keyCaps = capabilities.getKeyPadCapability();
	    // the presence of the element is sufficient to know whether it has a pinpad
	    return ! keyCaps.isEmpty();
	}

	// nothing found
	return false;
    }

}
