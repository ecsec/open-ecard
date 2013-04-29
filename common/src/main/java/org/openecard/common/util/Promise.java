/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.common.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * Implementation of a promise inspired by clojure's promise.
 * The implementation blocks until the value is set or a timeout occured.
 *
 * @see <tt><a href="http://clojuredocs.org/clojure_core/clojure.core/promise#source">clojure.core/promise</a></tt>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Promise <T> {

    private final CountDownLatch gate;

    private T value;

    /**
     * Creates an undelivered promise.
     */
    public Promise() {
	gate = new CountDownLatch(1);
    }

    /**
     * Checks if the promise is already delivered.
     *
     * @return {@code true} if the promise is delivered, {@code false} otherwise.
     */
    public synchronized boolean isDelivered() {
	try {
	    boolean isSet = gate.await(0, TimeUnit.MILLISECONDS);
	    return isSet;
	} catch (InterruptedException ex) {
	    // very unlikely if not impossible, but if it happens the right thing to do is probably kill the thread
	    throw new RuntimeException("Promise interrupted while waiting. Shutting down.");
	}
    }

    /**
     * Delivers the given value to the promise.
     * The result of calling this function is that the promise is delivered and either contains the given value or a
     * value which has been set previously. In the latter case an IllegalStateException is thrown. If that happens it is
     * an indicator that the implementation using the promise is flawed.
     *
     * @param value Value to be delivered.
     * @return The value that has been delivered.
     * @throws IllegalStateException Thrown in case the promise is already delivered when this function gets called.
     */
    public synchronized @Nullable T deliver(@Nullable final T value) throws IllegalStateException {
	if (! isDelivered()) {
	    this.value = value;
	    this.gate.countDown();
	    return this.value;
	} else {
	    throw new IllegalStateException("Failed to deliver promise, as it is already delivered.");
	}
    }

    /**
     * Dereferences the promise, aka tries to get its result.
     * The function waits as long as the promise is not delivered yet.
     *
     * @return The value that has been delivered to the promise.
     * @throws InterruptedException Thrown in case the current thread has been interrupted while waiting.
     */
    public @Nullable T deref() throws InterruptedException {
	try {
	    // wait an infinite time, most certainly longer than the machine running this program exists :p
	    return deref(Long.MAX_VALUE, TimeUnit.DAYS);
	} catch (TimeoutException ex) {
	    // ignore the exception, as trhe timout is quasi forever
	    return null;
	}
    }
    /**
     * Dereferences the promise, aka tries to get its result.
     * The function waits as long as defined by the wait parameters. A timeout value of 0 indicates not to wait at all.
     *
     * @param timeout Value from 0 to {@link Long#MAX_VALUE} indicating the number of units to wait.
     * @param unit The unit qualifying the timeout value.
     * @return The value that has been delivered to the promise.
     * @throws InterruptedException Thrown in case the current thread has been interrupted while waiting.
     * @throws TimeoutException Thrown in case a timeout occured.
     */
    public @Nullable T deref(@Nonnegative long timeout, @Nonnull TimeUnit unit) throws InterruptedException,
	    TimeoutException {
	boolean delivered = gate.await(timeout, unit);
	synchronized (this) {
	    if (delivered) {
		return this.value;
	    } else {
		throw new TimeoutException("Wait for promised value timed out.");
	    }
	}
    }

    /**
     * Gets the value of the promise but does not wait for its delivery.
     * This method is different than calling {@link #deref(long, java.util.concurrent.TimeUnit)} with a timeout value of
     * 0. It does not throw a TimeoutException, but returns null when no value is delivered yet. This may be
     * unambiguous with a delivered null value.
     *
     * @return The delivered value or {@code null} when no value has been deliverd yet.
     */
    public @Nullable T derefNonblocking() {
	if (isDelivered()) {
	    return value;
	} else {
	    return null;
	}
    }

}
