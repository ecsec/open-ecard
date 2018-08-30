/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

package org.openecard.common.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.interfaces.EventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * EventDispatcherImpl class distributing the events to all registered listeners.
 * Filtering is applied as requested at registration of the listener.
 *
 * @author Tobias Wich
 * @author René Lottes
 */
public class EventDispatcherImpl implements EventDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(EventDispatcherImpl.class);

    private final ThreadFactory threadFactory;

    private ConcurrentHashMap<EventCallback, ArrayList<EventFilter>> eventFilter;
    private HashMap<EventCallback, ExecutorService> threadPools;
    private boolean initialized;

    public EventDispatcherImpl() {
	this.threadFactory = new ThreadFactory() {
	    private final AtomicInteger num = new AtomicInteger(0);
	    private final ThreadGroup group = new ThreadGroup("Event Dispatcher");

	    @Override
	    public Thread newThread(Runnable r) {
		String name = String.format("Dispatcher Event %d", num.getAndIncrement());
		Thread t = new Thread(group, r, name);
		t.setDaemon(false);
		return t;
	    }
	};
    }


    @Override
    public synchronized void start() {
	this.eventFilter = new ConcurrentHashMap<>();
	this.threadPools = new HashMap<>();

	this.initialized = true;
    }

    @Override
    public synchronized void terminate() {
	if (initialized) {
	    // remove everything and thereby shutdown thread pools
	    for (EventCallback entry : Collections.list(eventFilter.keys())) {
		del(entry);
	    }

	    initialized = false;
	    eventFilter = null;
	    threadPools = null;
	}
    }


    @Override
    public EventCallback add(EventCallback cb) {
	add(cb, new EventTypeFilter());
	return cb;
    }

    @Override
    public EventCallback add(EventCallback cb, EventType ... eventTypes) {
	add(cb, new EventTypeFilter(eventTypes));
	return cb;
    }

    @Override
    public synchronized EventCallback add(EventCallback cb, EventFilter filter) {
	if (initialized) {
	    if (! eventFilter.containsKey(cb)) {
		eventFilter.put(cb, new ArrayList<>());
	    }
	    eventFilter.get(cb).add(filter);
	    // create an executor service for each callback
	    createExecutorService(cb);
	}
	return cb;
    }

    @Override
    public synchronized EventCallback del(EventCallback cb) {
	if (initialized && eventFilter.containsKey(cb)) {
	    eventFilter.remove(cb);
	    ExecutorService exec = threadPools.remove(cb);
	    exec.shutdownNow();
	}
	return cb;
    }

    @Override
    public synchronized void notify(EventType t, EventObject o) {
	for (Map.Entry<EventCallback, ArrayList<EventFilter>> entry : eventFilter.entrySet()) {
	    EventCallback cb = entry.getKey();
	    for (EventFilter filter : entry.getValue()) {
		// when there is a filter match, then fire out the event (only once!)
		if (filter.matches(t, o)) {
		    LOG.debug("Sending event notification {} to EventCallback {}.", t, cb);
		    ExecutorService executor = threadPools.get(cb);
		    executor.execute(() -> cb.signalEvent(t, o));
		    break;
		}
	    }
	}
    }

    private void createExecutorService(EventCallback cb) {
	ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
	threadPools.put(cb, executor);
    }

}
