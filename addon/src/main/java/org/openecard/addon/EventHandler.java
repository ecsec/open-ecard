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

package org.openecard.addon;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.openecard.common.enums.EventType;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventManager;
import org.openecard.ws.schema.StatusChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class EventHandler implements EventCallback {

    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

    private final Map<String, LinkedBlockingQueue<StatusChange>> eventQueues;
    private final Map<String, ReschedulableTimer> timers;
    // after this delay of inactivity an event queue (and it's timer) will be deleted
    private static final int deleteDelay = 60 * 1000;

    /**
     * Create a new EventHandler.
     *
     * @param eventManager event manager to get events (status changes) from
     */
    public EventHandler(EventManager eventManager) {
	eventQueues = new HashMap<>();
	timers = new HashMap<>();
	eventManager.registerAllEvents(this);
    }

    /**
     *
     * @param session
     * @return a StatusChange containing the new status, or null if no eventQueue for the given session exists or if
     *   interrupted
     */
    public StatusChange next(String session) {
	//String session = statusChangeRequest.getSessionIdentifier();
	StatusChange handle = null;
	LinkedBlockingQueue<StatusChange> queue = eventQueues.get(session);
	if (queue == null) {
	    logger.error("No queue found for session {}", session);
	    return null;
	}
	do {
	    try {
		timers.get(session).reschedule(deleteDelay);
		handle = eventQueues.get(session).poll(30, TimeUnit.SECONDS);
		logger.debug("WaitForChange event pulled from event queue.");
	    } catch (InterruptedException ex) {
		return null;
	    }
	} while (handle == null);
	return handle;
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventData instanceof ConnectionHandleType) {
	    ConnectionHandleType connectionHandle = (ConnectionHandleType) eventData;

	    for (Map.Entry<String, LinkedBlockingQueue<StatusChange>> entry : eventQueues.entrySet()) {
		try {
		    LinkedBlockingQueue<StatusChange> queue = entry.getValue();
		    StatusChange statusChange = new StatusChange();
		    statusChange.setAction(eventType.getEventTypeIdentifier());
		    statusChange.setConnectionHandle(connectionHandle);
		    queue.put(statusChange);
		} catch (InterruptedException ignore) {
		}
	    }
	}
    }

    /**
     * Adds a new EventQueue for a given session.
     *
     * @param sessionIdentifier session identifier
     */
    public void addQueue(final String sessionIdentifier) {
	if (eventQueues.get(sessionIdentifier) == null) {
	    eventQueues.put(sessionIdentifier, new LinkedBlockingQueue<StatusChange>());
	    ReschedulableTimer timer = new ReschedulableTimer();
	    timer.schedule(new DeleteTask(sessionIdentifier), deleteDelay);
	    timers.put(sessionIdentifier, timer);
	} else {
	    timers.get(sessionIdentifier).reschedule(deleteDelay);
	}
    }

    private final class DeleteTask implements Runnable {
	private final String sessionIdentifier;

	public DeleteTask(String sessionIdentifier) {
	    this.sessionIdentifier = sessionIdentifier;
	}

	@Override
	public void run() {
	    eventQueues.remove(sessionIdentifier);
	    timers.remove(sessionIdentifier);
	}
    }

}
