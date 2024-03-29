/****************************************************************************
 * Copyright (C) 2016-2018 ecsec GmbH.
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

package org.openecard.mdlw.event;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.datatype.DatatypeFactory;
import org.openecard.common.ECardConstants;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.mdlw.sal.MwModule;
import org.openecard.mdlw.sal.MwSlot;
import org.openecard.mdlw.sal.MwToken;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.openecard.mdlw.sal.enums.Flag;
import org.openecard.mdlw.sal.enums.TokenState;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.FinalizationException;
import org.openecard.mdlw.sal.exceptions.InitializationException;
import org.openecard.mdlw.sal.exceptions.SessionException;
import org.openecard.mdlw.sal.exceptions.TokenException;
import org.openecard.mdlw.sal.struct.CkSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
class MwEventRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(MwEventRunner.class);

    private final Environment env;
    private final HandlerBuilder builder;
    private final DatatypeFactory dataFactory;
    private final MwModule mwModule;
    private final Map<Long, SlotInfo> slots;

    private boolean supportsBlockingWait = true;
    private boolean supportsNonBlockingWait = true;

    MwEventRunner(Environment env, HandlerBuilder builder, DatatypeFactory dataFactory, MwModule mwModule) {
	this.env = env;
	this.dataFactory = dataFactory;
	this.builder = builder;
	this.mwModule = mwModule;
	this.slots = new HashMap<>();
    }

    void initRunner() throws CryptokiException {
	for (MwSlot slot : this.mwModule.getSlotList(TokenState.NotPresent)) {
	    if (isHwSlot(slot)) {
		this.sendTerminalAdded(slot);

		try {
		    slot.getTokenInfo();
		    this.sendCardInserted(slot);

		    // send recognized
		    this.sendCardRecognized(slot);
		} catch (TokenException | SessionException e) {
		    LOG.debug("Error getting token information, no card present in the requested slot.", e);
		}
	    }
	}
    }

    @Override
    public void run() {
	LOG.debug("Start event loop.");
	boolean repeatingNoEvent = false;
	while (true) {
	    try {
		if (! repeatingNoEvent) {
		    LOG.debug("Waiting for Middleware event.");
		}
		long slotId;
		if (supportsBlockingWait) {
		    slotId = mwModule.waitForSlotEvent(0);
		} else if (supportsNonBlockingWait) {
		    // TODO: this polling causes to flood logs in case debug is enabled for the wait call
		    slotId = mwModule.waitForSlotEvent(1);
		    if (slotId == -1) {
			// nothing changed
			repeatingNoEvent = true;
			try {
			    Thread.sleep(1000);
			    continue;
			} catch (InterruptedException ex) {
			    LOG.debug("Middleware Event Runner interrupted.");
			    return;
			}
		    }
		} else {
		    try {
			slotId = pollForSlotChange();
		    } catch (InterruptedException ex) {
			LOG.debug("Middleware Event Runner interrupted.");
			return;
		    }
		}
		LOG.debug("Middleware event detected.");
		repeatingNoEvent = false;

		// Flag to check if Terminal was removed
		boolean isProcessed = false;
		// find actual slot object
		for (MwSlot slot : this.mwModule.getSlotList(false)) {
		    if (isHwSlot(slot) && slot.getSlotInfo().getSlotID() == slotId) {
			isProcessed = true;
			String ifdName = slot.getSlotInfo().getSlotDescription();
			LOG.debug("Slot event recognized, slotId={}, ifdName={}.", slotId, ifdName);
			try {
			    MwToken tok = slot.getTokenInfo();

			    // send card inserted
			    this.sendCardInserted(slot);

			    // send recognized
			    this.sendCardRecognized(slot);
			} catch (TokenException | SessionException ex) {
			    LOG.debug("Error requesting token information.", ex);
			    this.sendCardRemoved(slot);
			}
		    }
		}
		if (! isProcessed) {
		    this.sendTerminalRemoved(slotId);
		}
	    } catch (CryptokiException ex) {
		// handle downgrade of the wait method
		if (ex.getErrorCode() == CryptokiLibrary.CKR_FUNCTION_NOT_SUPPORTED) {
		    if (supportsBlockingWait) {
			LOG.info("Blocking wait is not supported. Falling back to non-blocking wait.");
			supportsBlockingWait = false;
			continue;
		    } else if (supportsNonBlockingWait) {
			LOG.info("Non-blocking wait is not supported. Falling back to polling mode.");
			supportsNonBlockingWait = false;
			continue;
		    } else {
			LOG.error("Determining the card status is not possible with this middleware.", ex);
			return;
		    }
		} else if (ex.getErrorCode() == CryptokiLibrary.CKR_GENERAL_ERROR) {
		    LOG.error("Unrecoverable error during operation on the token list.", ex);
		    try {
			restartMiddleware();
		    } catch (InterruptedException ex2) {
			LOG.debug("Middleware Event Runner interrupted.");
			return;
		    }
		}

		LOG.error("Unrecoverable error during operation on the token list.", ex);
		try {
		    Thread.sleep(10000);
		} catch (InterruptedException ex1) {
                    LOG.debug("Middleware Event Runner interrupted.");
		    return;
		}
	    } catch (RuntimeException ex) {
                LOG.error("Unexpected exception occurred in Middleware Event Runner.", ex);
                throw ex;
            }
	}
    }

    private void sendTerminalAdded(MwSlot slot) {
	CkSlot ckSlot = slot.getSlotInfo();
	//Add Terminal to cache if not present
	SlotInfo sl;
	if (slots.get(ckSlot.getSlotID()) == null) {
	    sl = new SlotInfo();
	    sl.ifdName = ckSlot.getSlotDescription();
	    sl.slotId = ckSlot.getSlotID();

	    slots.put(sl.slotId, sl);
	} else {
	    return; //Event already sent
	}

	String ifdName = sl.ifdName;
	long slotId = sl.slotId;
	// send terminal added
	LOG.debug("Sending TERMINAL_ADDED event, ifdName={} id={}.", ifdName, slotId);
	ConnectionHandleType insertHandle = makeConnectionHandle(ifdName, slotId);
	MwEventObject addedEvent = new MwEventObject(insertHandle, slot);
	notify(EventType.TERMINAL_ADDED, addedEvent);
    }

    private void sendTerminalRemoved(long slotId) {
	if (slots.get(slotId) == null) {
	    return; //event already sended
	}

	//Remove Card
	this.sendCardRemoved(slots.get(slotId));

	String ifdName = slots.get(slotId).ifdName;

	LOG.debug("Sending TERMINAL_REMOVED event, ifdName={} id={}.", ifdName, slotId);
	ConnectionHandleType insertHandle = makeConnectionHandle(ifdName, slotId);
	MwEventObject addedEvent = new MwEventObject(insertHandle, null);
	notify(EventType.TERMINAL_REMOVED, addedEvent);

	//remove from cache
	slots.remove(slotId);
    }

    private void sendCardInserted(MwSlot slot) {
	//Add new Terminal to cache if needed
	LOG.debug("Sending terminal added event.");
	this.sendTerminalAdded(slot);

	CkSlot ckSlot = slot.getSlotInfo();
	String ifdName = ckSlot.getSlotDescription();
	long slotId = ckSlot.getSlotID();

	if (slots.get(slotId).isCardPresent) {
	    LOG.debug("Processing of already sent card inserted event detected. Not sending event.");
	    return; //Event already sended
	}
	// send card inserted
	ConnectionHandleType insertHandle = makeUnknownCardHandle(ifdName, slotId);
	MwEventObject insertEvent = new MwEventObject(insertHandle, slot);

	LOG.debug("Sending CARD_INSERTED event, ifdName={} id={}.", ifdName, slotId);
	notify(EventType.CARD_INSERTED, insertEvent);

	//For Cache
	slots.get(slotId).isCardPresent = true;
    }

    private void sendCardRecognized(MwSlot slot) throws CryptokiException {
	if (slots.get(slot.getSlotInfo().getSlotID()).isCardRecognized) {
	    LOG.debug("Processing of already sent card recognized event detected. Not sending event.");
	    return; // Event already sent
	}

	MwToken token = slot.getTokenInfo();
	CkSlot ckSlot = slot.getSlotInfo();
	String ifdName = ckSlot.getSlotDescription();
	long slotId = ckSlot.getSlotID();

	String manufacturer = token.getManufacturerID();
	String model = token.getModel();
	String label = token.getLabel();
	LOG.info("Middleware token identifiers: <{}> <{}> <{}>", manufacturer, model, label);
	String cardType = mwModule.getMiddlewareSALConfig().mapMiddlewareType(manufacturer, model, label);
	if (cardType != null) {
	    boolean protectedAuthPath = token.containsFlag(Flag.CKF_PROTECTED_AUTHENTICATION_PATH);
	    ConnectionHandleType recHandle = makeKnownCardHandle(ifdName, slotId, cardType, protectedAuthPath);
	    MwEventObject recEvent = new MwEventObject(recHandle, slot);

	    // TODO: make it work again according to redesign
//	    // recognize card and create card state entry
//	    if (mwCallback.addEntry(recEvent)) {
//		LOG.debug("Sending CARD_RECOGNIZED event, ifdName={} id={} type={}.", ifdName, slotId, cardType);
//		notify(EventType.CARD_RECOGNIZED, recEvent);
//	    } else {
//		LOG.debug("Detected card could not be added to the SAL states, not sending card recognized event.");
////		recEvent.getHandle().setContextHandle(null);
////		recEvent.getHandle().setChannelHandle(null);
////		LOG.debug("Sending SAL-less CARD_RECOGNIZED event, ifdName={} id={} type={}.", ifdName, slotId, cardType);
////		notify(EventType.CARD_RECOGNIZED, recEvent);
//	    }

	    //For Cache
	    slots.get(slot.getSlotInfo().getSlotID()).isCardRecognized = true;
	} else {
	    LOG.debug("Middleware instance is not responsible for this type of cards.");
	}
    }

    private void sendCardRemoved(MwSlot slot) {
        CkSlot ckSlot = slot.getSlotInfo();
        SlotInfo slotInfo = slots.get(ckSlot.getSlotID());
	if (slotInfo == null) {
	    // Event already sent
	    LOG.debug("Processing of card removed event prevented due to terminal removed already being delivered.");
	} else {
	    sendCardRemoved(slotInfo);
	}
    }

    private void sendCardRemoved(SlotInfo sl) {
	if (! slots.get(sl.slotId).isCardPresent) {
	    LOG.debug("Processing of already sent card removed event detected. Not sending event.");
	    return; // Event already sended
	}

	ConnectionHandleType handle = makeConnectionHandle(sl.ifdName, sl.slotId);
	MwEventObject remEvent = new MwEventObject(handle, null);

	// TODO: make it work again according to redesign
//	// remove card state entry
//	mwCallback.removeEntry(remEvent);

	LOG.debug("Sending CARD_REMOVED event, ifdName={} id={} type={}.", sl.ifdName, sl.slotId);
	notify(EventType.CARD_REMOVED, remEvent);

	//For Cache
	slots.get(sl.slotId).isCardPresent = false;
	slots.get(sl.slotId).isCardRecognized = false;
    }

    private ConnectionHandleType makeConnectionHandle(String ifdName, long slotIdx) {
	ConnectionHandleType h = builder.setIfdName(ifdName)
		.setSlotIdx(BigInteger.valueOf(slotIdx))
		.buildConnectionHandle();
	return h;
    }

    private ConnectionHandleType makeUnknownCardHandle(String ifdName, long slotIdx) {
	ConnectionHandleType h = builder
		.setIfdName(ifdName)
		.setSlotIdx(BigInteger.valueOf(slotIdx))
		.setCardType(ECardConstants.UNKNOWN_CARD)
		.buildConnectionHandle();
	return h;
    }

    private ConnectionHandleType makeKnownCardHandle(String ifdName, long slotIdx, String cardType, boolean isProtectedAuthPath) {
	ConnectionHandleType.RecognitionInfo rInfo = new ConnectionHandleType.RecognitionInfo();
	rInfo.setCardType(cardType);
	rInfo.setCaptureTime(dataFactory.newXMLGregorianCalendar(new GregorianCalendar()));

	ConnectionHandleType h = builder
		.setIfdName(ifdName)
		.setSlotIdx(BigInteger.valueOf(slotIdx))
		.setRecognitionInfo(rInfo)
		.setProtectedAuthPath(isProtectedAuthPath)
		.buildConnectionHandle();
	return h;
    }

    private boolean isHwSlot(MwSlot slot) {
	return (slot.getSlotInfo().getFlags() & CryptokiLibrary.CKF_HW_SLOT) > 0;
    }

    private void restartMiddleware() throws InterruptedException {
	boolean shutDownSuccess = false;
	while (! shutDownSuccess) {
	    try {
		LOG.info("Trying to shutdown middleware.");
		mwModule.destroy();
		shutDownSuccess = true;
		LOG.info("Successfully terminated middleware.");
	    } catch (FinalizationException ex) {
		if (ex.getErrorCode() == CryptokiLibrary.CKR_CRYPTOKI_NOT_INITIALIZED) {
		    LOG.info("Middleware is already terminated.");
		    shutDownSuccess = true;
		} else {
		    LOG.error("Failed to terminate middleware, wait and try again.", ex);
		    Thread.sleep(5000);
		}
	    }
	}

	// delete all references to the current slots
	for (SlotInfo slot : slots.values()) {
	    sendCardRemoved(slot);
	}
	slots.clear();

	while (true) {
	    try {
		// give the system some time before trying again
		Thread.sleep(5000);

		LOG.info("Trying to initialize middleware.");
		mwModule.initialize();
		LOG.info("Successfully initialized middleware.");
		return;
	    } catch (InitializationException ex) {
		if (ex.getErrorCode() == CryptokiLibrary.CKR_CRYPTOKI_ALREADY_INITIALIZED) {
		    LOG.debug("Middleware is already initialized.");
		    return;
		} else {
		    LOG.error("Failed to initialize middleware.", ex);
		}
	    }
	}
    }

    private long pollForSlotChange() throws CryptokiException, InterruptedException {
	// loop until an event has been found
	while (true) {
	    List<MwSlot> currentSlots = mwModule.getSlotList(TokenState.NotPresent);

	    // remove non hw slots
	    Iterator<MwSlot> i = currentSlots.iterator();
	    while (i.hasNext()) {
		MwSlot s = i.next();
		if (! isHwSlot(s)) {
		    i.remove();
		}
	    }

	    // check if a new terminal appeared
	    for (MwSlot next : currentSlots) {
		if (! slots.containsKey(next.getSlotInfo().getSlotID())) {
		    return next.getSlotInfo().getSlotID();
		}
	    }

	    // check if a terminal vanished
	    {
		ArrayList<Long> checkIds = new ArrayList<>();
		for (MwSlot next : currentSlots) {
		    checkIds.add(next.getSlotInfo().getSlotID());
		}
		Set<Long> remainingIds = new HashSet<>(slots.keySet());
		remainingIds.removeAll(checkIds);
		if (! remainingIds.isEmpty()) {
		    return remainingIds.iterator().next();
		}
	    }

	    // check if a card has been inserted or removed
	    for (MwSlot next : currentSlots) {
		long id = next.getSlotInfo().getSlotID();
		SlotInfo nextInfo = slots.get(id);

		boolean cardPresent;
		try {
		    next.getTokenInfo(); // craises error when no card is present
		    cardPresent = true;
		} catch (TokenException | SessionException ex) {
		    cardPresent = false;
		}

		if (nextInfo.isCardPresent != cardPresent) {
		    return id;
		}
	    }

	    // nothing found, sleep a bit and try again
	    Thread.sleep(1000);
	}
    }

    //Struct for caching
    private class SlotInfo {
	public long slotId;
	public String ifdName;
	public boolean isCardPresent = false;
	public boolean isCardRecognized = false;
    }

    private synchronized void notify(EventType eventType, EventObject eventData) {
	LOG.debug("Notify {}, {}", eventType, eventData);
	env.getEventDispatcher().notify(eventType, eventData);
    }

}
