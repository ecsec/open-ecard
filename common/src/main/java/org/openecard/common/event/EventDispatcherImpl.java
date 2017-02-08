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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.openecard.common.interfaces.EventCallback;
import org.openecard.common.interfaces.EventDispatcher;
import org.openecard.common.interfaces.EventFilter;
import org.openecard.common.util.Pair;
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
    private static final long MAX_DISPATCH_MILLIS = 5 * 1000;


    private final ConcurrentHashMap<EventCallback,ArrayList<EventFilter>> eventFilter;
    private final BlockingQueue<Pair<EventType, EventObject>> eventQueue;

    private ExecutorService threadPool;

    public EventDispatcherImpl() {
	this.eventFilter = new ConcurrentHashMap<>();
	this.eventQueue = new LinkedBlockingQueue<>();
    }


    @Override
    public synchronized void start() {
	threadPool = Executors.newCachedThreadPool(new ThreadFactory() {
	    private final AtomicInteger num = new AtomicInteger(0);
	    private final ThreadGroup group = new ThreadGroup("Event Dispatcher");
	    @Override
	    public Thread newThread(Runnable r) {
		String name = String.format("Dispatcher Event %d", num.getAndIncrement());
		Thread t = new Thread(group, r, name);
		t.setDaemon(false);
		return t;
	    }
	});
	threadPool.submit(new NotificationSender());
    }

    @Override
    public synchronized void terminate() {
	threadPool.shutdownNow();
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
	if (! eventFilter.containsKey(cb)) {
	    eventFilter.put(cb, new ArrayList<EventFilter>());
	}
	eventFilter.get(cb).add(filter);
	return cb;
    }

    @Override
    public synchronized EventCallback del(EventCallback cb) {
	if (eventFilter.containsKey(cb)) {
	    eventFilter.remove(cb);
	}
	return cb;
    }

    @Override
    public void notify(EventType t, EventObject o) {
	if (eventQueue.offer(new Pair<>(t, o))) {
	    LOG.debug("Added event {} into event queue.", t);
	} else {
	    LOG.error("Failed to add event {} into the queue, the queue is full.", t);
	}
    }


    private class NotificationSender implements Runnable {

	@Override
	public void run() {
	    try {
		while (true) {
		    Pair<EventType, EventObject> pair = eventQueue.take();
		    notify(pair.p1, pair.p2);
		}
	    } catch (InterruptedException ex) {
		// just terminate
	    }
	}

	private void notify(EventType t, EventObject o) {
	    // synchronize, so we have an unmodified view on the dispatcher object
	    synchronized (EventDispatcherImpl.this) {
		// check every callback for a matching filter
		ArrayList<Future<?>> futures = new ArrayList<>();
		for (Map.Entry<EventCallback, ArrayList<EventFilter>> entry : eventFilter.entrySet()) {
		    EventCallback cb = entry.getKey();
		    for (EventFilter filter : entry.getValue()) {
			if (filter.matches(t, o)) {
			    futures.add(fork(cb, t, o));
			    break;
			}
		    }
		}

		// wait until all events have been dispatched
		long remaining = MAX_DISPATCH_MILLIS;
		for (Future<?> next : futures) {
		    if (remaining > 0) {
			long start = System.currentTimeMillis();
			try {
			    next.get(remaining, TimeUnit.MILLISECONDS);
			} catch (ExecutionException ex) {
			    // I don't care
			} catch (InterruptedException ex) {
			    // someone wants us to stop as soon as possible, so omit waiting for anymore futures
			    break;
			} catch (TimeoutException ex) {
			    // time is up
			    remaining = -1;
			    continue;
			}
			long diff = System.currentTimeMillis() - start;
			remaining -= diff;
		    } else {
			LOG.warn("Skipping wait for event notification thread.");
		    }
		}
	    }
	}

	private Future<?> fork(EventCallback cb, EventType t, EventObject o) {
	    Future<?> f = threadPool.submit(new EventRunner(cb, t, o));
	    return f;
	}

    }

}
