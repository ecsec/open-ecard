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
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
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

    private boolean supportsBlockingWait = true;
    private boolean supportsNonBlockingWait = true;

    MwEventRunner(Environment env, HandlerBuilder builder, DatatypeFactory dataFactory, MwModule mwModule) {
	this.env = env;
	this.dataFactory = dataFactory;
	this.builder = builder;
	this.mwModule = mwModule;
	slots = new HashMap<>();
    }

    void initRunner() throws CryptokiException {
	for (MwSlot slot : this.mwModule.getSlotList(TokenState.NotPresent)) {
	    if (isHwSlot(slot)) {
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
    }

    @Override
    public void run() {
	LOG.debug("Start event loop.");
	while (true) {
	    try {
		LOG.debug("Waiting for Middleware event.");
		long slotId;
		if (supportsBlockingWait) {
		    slotId = mwModule.waitForSlotEvent(0);
		} else if (supportsNonBlockingWait) {
		    // TODO: this polling causes to flood logs in case debug is enabled for the wait call
		    slotId = mwModule.waitForSlotEvent(1);
		    if (slotId == -1) {
			// nothing changed
			try {
			    Thread.sleep(1000);
			    continue;
			} catch (InterruptedException ex) {
			    LOG.debug("Middleware Event Runner interrupted.");
			    return;
			}
		    }
		} else {
		    throw new IllegalStateException("This point should never be reached");
		}
		LOG.debug("Middleware event detected.");

		//Flag to check if Terminal was removed
		boolean isProcessed = false;
		// find actual slot object
		for (MwSlot slot : this.mwModule.getSlotList(false)) {
		    if (isHwSlot(slot) && slot.getSlotInfo().getSlotID() == slotId) {
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
			LOG.info("Non-blocking wait is not supported. Terminating event thread.");
			supportsNonBlockingWait = false;
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
	//Add Terminal to cache if not present
	if (slots.get(slot.getSlotInfo().getSlotID()) == null) {
	    SlotInfo sl = new SlotInfo();
	    sl.ifdName = slot.getSlotInfo().getSlotDescription();
	    sl.slotId = slot.getSlotInfo().getSlotID();

	    slots.put(slot.getSlotInfo().getSlotID(), sl);
	} else {
	    return; //Event already sent
	}
	String ifdName = slot.getSlotInfo().getSlotDescription();
	long slotId = slot.getSlotInfo().getSlotID();
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
	this.sendTerminalAdded(slot);

	if (slots.get(slot.getSlotInfo().getSlotID()).isCardPresent) {
	    return; //Event already sended
	}
	CkSlot ckSlot = slot.getSlotInfo();
	// send card inserted
	ConnectionHandleType insertHandle = makeUnknownCardHandle(ckSlot.getSlotDescription(), ckSlot.getSlotID());
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

	CkSlot ckSlot = slot.getSlotInfo();

	String cardType = String.format("%s_%s", token.getManufacturerID(), token.getModel());
	LOG.info("Middleware card type: {}", cardType);
	cardType = mwModule.getMiddlewareSALConfig().mapMiddlewareType(cardType);
	if (cardType != null) {
	    boolean protectedAuthPath = token.containsFlag(Flag.CKF_PROTECTED_AUTHENTICATION_PATH);
	    ConnectionHandleType recHandle = makeKnownCardHandle(ckSlot.getSlotDescription(), ckSlot.getSlotID(),
		    cardType, protectedAuthPath);
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

	ConnectionHandleType handle = makeConnectionHandle(slotInfo.ifdName, slotInfo.slotId);
	MwEventObject remEvent = new MwEventObject(handle, slot);
	notify(EventType.CARD_REMOVED, remEvent);

	//For Cache
	slots.get(slot.getSlotInfo().getSlotID()).isCardPresent = false;
	slots.get(slot.getSlotInfo().getSlotID()).isCardRecognized = false;
    }

    private void sendCardRemoved(SlotInfo sl) {
	if (! slots.get(sl.slotId).isCardPresent) {
	    return; //Event already sended
	}

	ConnectionHandleType handle = makeConnectionHandle(sl.ifdName, sl.slotId);
	MwEventObject remEvent = new MwEventObject(handle, null);
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
