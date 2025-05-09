/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger { }

/**
 * Implementation of a promise resembling the semantics of a future.
 * This is a wrapper, so that a future delivery mechanism can be used where promises are used otherwise.
 *
 * @author Tobias Wich
 * @param <T> The result type of the promise.
</T> */
class FuturePromise<T>(
	function: Callable<T>,
) : Promise<T>() {
	init {
		Thread({
			var result: T?
			try {
				result = function.call()
			} catch (ex: Exception) {
				logger.error(ex) { "Failed to complete computation of the result." }
				result = null
			}
			// We have either a result or an error
			super@FuturePromise.deliver(result)
		}, "FuturePromise-" + THREAD_NUM.getAndIncrement()).start()
	}

	@Synchronized
	override fun deliver(value: T?): T? {
		// nobody should call this function, as the delivery is performed by the background thread
		throw IllegalStateException("The promise is already delivered.")
	}

	companion object {
		private val THREAD_NUM = AtomicInteger(1)
	}
}
