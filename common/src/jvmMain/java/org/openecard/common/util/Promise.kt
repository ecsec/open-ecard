/****************************************************************************
 * Copyright (C) 2013-2017 ecsec GmbH.
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
 */
package org.openecard.common.util

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Implementation of a promise inspired by clojure's promise.
 * The implementation blocks until the value is set or a timeout occured.
 *
 * @param <T> Type returned by the promise.
 * @see `[clojure.core/promise](http://clojuredocs.org/clojure_core/clojure.core/promise.source)`
 *
 * @author Tobias Wich
</T> */
open class Promise<T> {
	private val gate = CountDownLatch(1)

	/**
	 * Gets the promises cancelled status.
	 *
	 * @return `true` if promise is cancelled, `false` otherwise.
	 */
	@get:Synchronized
	var isCancelled: Boolean = false
		private set
	private var value: T? = null

	@get:Synchronized
	val isDelivered: Boolean
		/**
		 * Checks if the promise is already delivered.
		 *
		 * @return `true` if the promise is delivered, `false` otherwise.
		 */
		get() {
			val isSet = gate.count <= 0
			return isSet && !isCancelled
		}

	/**
	 * Cancel the promise, forcing to return all waiting threads.
	 *
	 * @see .deref
	 * @see .deref
	 */
	@Synchronized
	fun cancel() {
		isCancelled = true
		if (gate.count > 0) {
			gate.countDown()
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
	@Synchronized
	open fun deliver(value: T?): T? {
		if (!isDelivered) {
			if (!isCancelled) {
				this.value = value
				gate.countDown()
			}
			return value
		} else {
			throw IllegalStateException("Failed to deliver promise, as it is already delivered.")
		}
	}

	/**
	 * Dereferences the promise, aka tries to get its result.
	 * The function waits as long as the promise is not delivered yet.
	 *
	 * @return The value that has been delivered to the promise.
	 * @throws InterruptedException Thrown in case the current thread has been interrupted while waiting.
	 */
	fun deref(): T? =
		try {
			// wait an infinite time, most certainly longer than the machine running this program exists :p
			deref(Long.MAX_VALUE, TimeUnit.DAYS)
		} catch (ex: TimeoutException) {
			// ignore the exception, as the timeout is quasi forever
			null
		}

	/**
	 * Dereferences the promise, aka tries to get its result.
	 * The function waits as long as defined by the wait parameters. A timeout value of 0 indicates not to wait at all.
	 *
	 * @param timeout Value from 0 to [Long.MAX_VALUE] indicating the number of units to wait.
	 * @param unit The unit qualifying the timeout value.
	 * @return The value that has been delivered to the promise.
	 * @throws InterruptedException Thrown in case the current thread has been interrupted while waiting. This also
	 * includes a cancellation of the promise.
	 * @throws TimeoutException Thrown in case a timeout occured.
	 */
	fun deref(
		timeout: Long,
		unit: TimeUnit?,
	): T? {
		val delivered = gate.await(timeout, unit)
		synchronized(this) {
			if (isCancelled) {
				throw InterruptedException("Promise has been cancelled.")
			} else if (delivered) {
				return this.value
			} else {
				throw TimeoutException("Wait for promised value timed out.")
			}
		}
	}

	/**
	 * Gets the value of the promise but does not wait for its delivery.
	 * This method is different than calling [.deref] with a timeout value of
	 * 0. It does not throw a TimeoutException, but returns null when no value is delivered yet. This may be
	 * unambiguous with a delivered null value.
	 *
	 * @return The delivered value or `null` when no value has been delivered yet or the promise has been
	 * cancelled.
	 */
	fun derefNonblocking(): T? =
		if (isDelivered) {
			value
		} else {
			null
		}
}
