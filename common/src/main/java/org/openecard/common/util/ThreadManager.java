/****************************************************************************
 * Copyright (C) 2021 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *
 * @author Tobias Wich
 * @param <Key>
 */
public class ThreadManager <Key> {

    public static final long DEFAULT_WAIT = 5000;

    private final AtomicInteger threadNumber;
    private final ThreadGroup tg;
    private final Map<Key, Thread> threads;

    public ThreadManager(String threadGroupName) {
	this.threadNumber = new AtomicInteger(0);
	this.tg = new ThreadGroup(threadGroupName);
	this.threads = new HashMap<>();
    }

    public void submit(Key key, Runnable job) {
	synchronized (threads) {
	    if (threads.containsKey(key)) {
		throw new IllegalStateException(String.format("A job for key %s already exists.", key.toString()));
	    }

	    String name = String.format("%s-%d", key.toString(), threadNumber.incrementAndGet());
	    Thread t = new Thread(tg, job, name);
	    t.start();
	    threads.put(key, t);
	}
    }

    public void stopThread(Key key) throws InterruptedException {
	stopThread(key, DEFAULT_WAIT);
    }

    public void stopThread(Key key, long maxWaitTime) throws InterruptedException {
	Thread t;
	synchronized (threads) {
	    t = threads.remove(key);
	}
	if (t != null) {
	    stopThread(t, maxWaitTime);
	}
    }

    private void stopThread(Thread t, long maxWaitTime) throws InterruptedException {
	t.interrupt();
	t.join(maxWaitTime);
    }

    public void stopThreads() throws InterruptedException {
	stopThreads(DEFAULT_WAIT);
    }

    public void stopThreads(long maxWaitTime) throws InterruptedException {
	synchronized (threads) {
	    Iterator<Map.Entry<Key, Thread>> i = threads.entrySet().iterator();
	    while (i.hasNext()) {
		Map.Entry<Key, Thread> next = i.next();
		i.remove();
		stopThread(next.getValue(), maxWaitTime);
	    }
	}
    }

}
