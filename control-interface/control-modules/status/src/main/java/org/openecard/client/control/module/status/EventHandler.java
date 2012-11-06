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

package org.openecard.client.control.module.status;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.common.interfaces.EventManager;
import org.openecard.ws.schema.StatusChange;


/**
 * 
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class EventHandler implements EventCallback {

    private Map<String, LinkedBlockingQueue<StatusChange>> eventQueues;
    private Map<String, ReschedulableTimer> timers;
    // after this delay of inactivity an event queue (and it's timer) will be deleted
    private static final int deleteDelay = 60 * 1000;

    /**
     * Create a new EventHandler.
     * 
     * @param eventManager
     *            event manager to get events (status changes) from
     */
    public EventHandler(EventManager eventManager) {
	eventQueues = new HashMap<String, LinkedBlockingQueue<StatusChange>>();
	timers = new HashMap<String, ReschedulableTimer>();
	eventManager.registerAllEvents(this);
    }

    /**
     * 
     * @param statusChangeRequest
     *            a status change request for a specific session
     * @return a StatusChange containing the new status, or null if no eventQueue for the given session exists or if
     *         interrupted
     */
    public StatusChange next(StatusChangeRequest statusChangeRequest) {
	StatusChange handle = null;
	LinkedBlockingQueue<StatusChange> queue = eventQueues.get(statusChangeRequest.getSessionIdentifier());
	if (queue == null)
	    return null;
	do {
	    try {
		timers.get(statusChangeRequest.getSessionIdentifier()).reschedule(deleteDelay);
		handle = eventQueues.get(statusChangeRequest.getSessionIdentifier()).poll(30, TimeUnit.SECONDS);
	    } catch (InterruptedException ex) {
		return null;
	    }
	} while (handle == null);

	return handle;
    }

    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventData instanceof ConnectionHandleType) {
	    try {
		ConnectionHandleType connectionHandle = (ConnectionHandleType) eventData;
		String session = connectionHandle.getChannelHandle().getSessionIdentifier();
		LinkedBlockingQueue<StatusChange> queue = eventQueues.get(session);

		if (queue != null) {
		    StatusChange statusChange = new StatusChange();
		    statusChange.setAction(eventType.getEventTypeIdentifier());
		    statusChange.setConnectionHandle(connectionHandle);
		    queue.put(statusChange);
		}

	    } catch (InterruptedException ignore) {
	    }
	}
    }

    /**
     * Adds a new EventQueue for a given session.
     * 
     * @param sessionIdentifier
     *            session identifier
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
