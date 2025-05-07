/****************************************************************************
 * Copyright (C) 2018 ecsec GmbH.
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
import java.util.concurrent.CompletionService
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.Future

private val LOG = KotlinLogging.logger { }

/**
 * Utility to wait for a buch of promises.
 *
 * @author Tobias Wich
 * @param <T> Type of the promise value.
</T> */
class CombinedPromise<T>(
	private val promises: List<Promise<T>>,
) {
	/**
	 * Waits for any of the promises to finish.
	 *
	 * @return
	 * @throws InterruptedException
	 */
	fun retrieveFirst(): T {
		val futures = ArrayList<Future<T>>()
		try {
			val e: Executor = Executors.newFixedThreadPool(promises.size)
			val cs: CompletionService<T> = ExecutorCompletionService(e)

			// submit all tasks
			for (next in promises) {
				val f = cs.submit(makeCallable(next))
				futures.add(f)
			}

			// wait for a result
			return cs.take().get()
		} catch (ex: ExecutionException) {
			// propagate InterruptedException
			if (ex.cause is InterruptedException) {
				throw (ex.cause as InterruptedException?)!!
			} else {
				LOG.error(ex) { "Unexpected exception see while waiting for UI promise." }
				throw IllegalStateException("Unexpected exception see while waiting for UI promise.")
			}
		} finally {
			for (f in futures) {
				f.cancel(true)
			}
		}
	}

	private fun makeCallable(p: Promise<T>): Callable<T> = Callable { p.deref() }
}
