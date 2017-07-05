/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import javax.xml.datatype.DatatypeFactory;
import org.openecard.common.ECardConstants;
import org.openecard.common.event.EventObject;
import org.openecard.common.event.EventType;
import org.openecard.common.interfaces.Environment;
import org.openecard.common.util.HandlerBuilder;
import org.openecard.mdlw.sal.MwModule;
import org.openecard.mdlw.sal.MwSlot;
import org.openecard.mdlw.sal.MwToken;
import org.openecard.mdlw.sal.enums.Flag;
import org.openecard.mdlw.sal.enums.TokenState;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
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

    MwEventRunner(Environment env, HandlerBuilder builder, DatatypeFactory dataFactory, MwModule mwModule) {
	this.env = env;
	this.dataFactory = dataFactory;
	this.builder = builder;
	this.mwModule = mwModule;
	slots = new HashMap<>();
    }

    void initRunner() throws CryptokiException {
	for (MwSlot slot : this.mwModule.getSlotList(TokenState.NotPresent)) {
	    this.sendTerminalAdded(slot);
	    String ifdName = slot.getSlotInfo().getSlotDescription();

	    try {
		slot.getTokenInfo().getLabel();
		this.sendCardInserted(slot);

		// send recognized
		this.sendCardRecognized(slot);
	    } catch (TokenException | SessionException e) {
		LOG.debug("Error getting token information, no card present in the requested slot.", e);
	    }
	}
    }

    @Override
    public void run() {
	LOG.debug("Start event loop.");
	while (true) {
	    try {
		LOG.debug("Waiting for Middleware event.");
		long slotId = mwModule.waitForSlotEvent(0);
		LOG.debug("Middleware event detected.");

		//Flag to check if Terminal was removed
		boolean isProcessed = false;
		// find actual slot object
		for (MwSlot slot : this.mwModule.getSlotList(false)) {
		    if (slot.getSlotInfo().getSlotID() == slotId) {
			isProcessed = true;
			String ifdName = slot.getSlotInfo().getSlotDescription();
			LOG.debug("Slot event recognized, slotId={}, ifdName={}.", slotId, ifdName);
			try {
			    slot.getTokenInfo().getLabel();

			    // send card inserted
			    this.sendCardInserted(slot);

			    // send recognized
			    this.sendCardRecognized(slot);
			} catch (TokenException | SessionException ex) {
			    LOG.debug("Error requesting token information.", ex);
			    this.sendCardRemoved(slot);
			}
		    } else {
		    }
		}
		if (! isProcessed) {
		    this.sendTerminalRemoved(slotId);
		}
	    } catch (CryptokiException ex) {
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
	//Add Terminal to cache if not pressent
	if (slots.get(slot.getSlotInfo().getSlotID()) == null) {
	    SlotInfo sl = new SlotInfo();
	    sl.ifdName = slot.getSlotInfo().getSlotDescription();
	    sl.slotId = slot.getSlotInfo().getSlotID();

	    slots.put(slot.getSlotInfo().getSlotID(), sl);
	} else {
	    return; //Event already sended
	}
	String ifdName = slot.getSlotInfo().getSlotDescription();
	// send terminal added
	LOG.debug("Sending TERMINAL_ADDED event, ifdName={}.", ifdName);
	ConnectionHandleType insertHandle = makeConnectionHandle(ifdName);
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

	LOG.debug("Sending TERMINAL_REMOVED event, ifdName={}.", ifdName);
	ConnectionHandleType insertHandle = makeConnectionHandle(ifdName);
	MwEventObject addedEvent = new MwEventObject(insertHandle, null);
	notify(EventType.TERMINAL_REMOVED, addedEvent);

	//remove from cache
	slots.remove(slotId);
    }

    private void sendCardInserted(MwSlot slot) {
	//Add new Terminal to cache if needed
	this.sendTerminalAdded(slot);

	if (slots.get(slot.getSlotInfo().getSlotID()).isCardPresent) {
	    return; //Event already sended
	}
	String ifdName = slot.getSlotInfo().getSlotDescription();
	// send card inserted
	ConnectionHandleType insertHandle = makeUnknownCardHandle(ifdName);
	MwEventObject insertEvent = new MwEventObject(insertHandle, slot);

	notify(EventType.CARD_INSERTED, insertEvent);

	//For Cache
	slots.get(slot.getSlotInfo().getSlotID()).isCardPresent = true;
    }

    private void sendCardRecognized(MwSlot slot) throws CryptokiException {
	if (slots.get(slot.getSlotInfo().getSlotID()).isCardRecognized) {
	    return; //Event already sended
	}

	MwToken token = slot.getTokenInfo();

	String ifdName = slot.getSlotInfo().getSlotDescription();

	String cardType = String.format("%s_%s", token.getManufacturerID(), token.getModel());
	cardType = mwModule.getMiddlewareSALConfig().mapMiddlewareType(cardType);
	if (cardType != null) {
	    boolean protectedAuthPath = token.containsFlag(Flag.CKF_PROTECTED_AUTHENTICATION_PATH);
	    ConnectionHandleType recHandle = makeKnownCardHandle(ifdName, cardType, protectedAuthPath);
	    MwEventObject recEvent = new MwEventObject(recHandle, slot);
	    notify(EventType.CARD_RECOGNIZED, recEvent);

	    //For Cache
	    slots.get(slot.getSlotInfo().getSlotID()).isCardRecognized = true;
	}
    }

    private void sendCardRemoved(MwSlot slot) {
        CkSlot ckSlot = slot.getSlotInfo();
        SlotInfo slotInfo = slots.get(ckSlot.getSlotID());
	if (slotInfo == null || ! slotInfo.isCardPresent) {
	    return; //Event already sent
	}
	String ifdName = slot.getSlotInfo().getSlotDescription();

	ConnectionHandleType handle = makeConnectionHandle(ifdName);
	MwEventObject remEvent = new MwEventObject(handle, slot);
	notify(EventType.CARD_REMOVED, remEvent);

	//For Cache
	slots.get(slot.getSlotInfo().getSlotID()).isCardPresent = false;
	slots.get(slot.getSlotInfo().getSlotID()).isCardRecognized = false;
    }

    private void sendCardRemoved(SlotInfo sl) {
	if (!slots.get(sl.slotId).isCardPresent) {
	    return; //Event already sended
	}
	String ifdName = sl.ifdName;

	ConnectionHandleType handle = makeConnectionHandle(ifdName);
	MwEventObject remEvent = new MwEventObject(handle, null);
	notify(EventType.CARD_REMOVED, remEvent);

	//For Cache
	slots.get(sl.slotId).isCardPresent = false;
	slots.get(sl.slotId).isCardRecognized = false;
    }

    private ConnectionHandleType makeConnectionHandle(String ifdName) {
	ConnectionHandleType h = builder.setIfdName(ifdName)
		.setSlotIdx(BigInteger.ZERO)
		.buildConnectionHandle();
	return h;
    }

    private ConnectionHandleType makeUnknownCardHandle(String ifdName) {
	ConnectionHandleType h = builder
		.setIfdName(ifdName)
		.setSlotIdx(BigInteger.ZERO)
		.setCardType(ECardConstants.UNKNOWN_CARD)
		.buildConnectionHandle();
	return h;
    }

    private ConnectionHandleType makeKnownCardHandle(String ifdName, String cardType, boolean isProtectedAuthPath) {
	ConnectionHandleType.RecognitionInfo rInfo = new ConnectionHandleType.RecognitionInfo();
	rInfo.setCardType(cardType);
	rInfo.setCaptureTime(dataFactory.newXMLGregorianCalendar(new GregorianCalendar()));

	ConnectionHandleType h = builder
		.setIfdName(ifdName)
		.setSlotIdx(BigInteger.ZERO)
		.setRecognitionInfo(rInfo)
		.setProtectedAuthPath(isProtectedAuthPath)
		.buildConnectionHandle();
	return h;
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
