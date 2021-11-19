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
 * Runs submitted jobs as threads and maintains a list of the running threads.
 *
 * Each job is associated with a given key, so it possible to address this specific jobs' thread.
 * A job can be cancelled individually, or all threads can be cancelled at once.
 *
 * @author Tobias Wich
 * @param <Key> Type of the key used to map the threads. Must implement {@link #equals(java.lang.Object)}.
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

    /**
     * Submits a job to run as a thread in the background.
     *
     * @param key Key value identifying the thread.
     * @param job Job implementation that will run as a thread.
     * @throws IllegalStateException Thrown in case a job for the given key already exists.
     */
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

    /**
     * Stops the thread for the given key.
     * This method uses a default wait timeout defined in {@link #DEFAULT_WAIT}.
     *
     * @param key Key identifying the running job.
     * @throws InterruptedException Thrown in case waiting for the thread to terminate is interrupted or timed out.
     * @see #stopThread(java.lang.Object, long)
     */
    public void stopThread(Key key) throws InterruptedException {
	stopThread(key, DEFAULT_WAIT);
    }

    /**
     * Stops the thread for the given key.
     *
     * @param key Key identifying the running job.
     * @param maxWaitTime Time in milliseconds to wait for the thread to die before raising an exception.
     * @throws InterruptedException Thrown in case waiting for the thread to terminate is interrupted or timed out.
     */
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

    /**
     * Stops all threads managed in this instance.
     *
     * @throws InterruptedException Thrown in case waiting for any thread to terminate is interrupted or timed out.
     * @see #stopThreads(long)
     */
    public void stopThreads() throws InterruptedException {
	stopThreads(DEFAULT_WAIT);
    }

    /**
     * Stops all threads managed in this instance.
     * This method uses a default wait timeout defined in {@link #DEFAULT_WAIT}.
     *
     * @param maxWaitTime Time in milliseconds to wait for any thread to die before raising an exception.
     * @throws InterruptedException Thrown in case waiting for any thread to terminate is interrupted or timed out.
     */
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
