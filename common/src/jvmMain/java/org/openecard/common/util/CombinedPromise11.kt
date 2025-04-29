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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.*

/**
 * Utility to wait for a buch of promises.
 *
 * @author Tobias Wich
 * @param <T> Type of the promise value.
</T> */
class CombinedPromise<T>(private val promises: List<Promise<T>>) {
    /**
     * Waits for any of the promises to finish.
     *
     * @return
     * @throws InterruptedException
     */
    @Throws(InterruptedException::class)
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
                LOG.error("Unexpected exception see while waiting for UI promise.", ex)
                throw IllegalStateException("Unexpected exception see while waiting for UI promise.")
            }
        } finally {
            for (f in futures) {
                f.cancel(true)
            }
        }
    }

    private fun makeCallable(p: Promise<T>): Callable<T> {
        return Callable { p.deref() }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(CombinedPromise::class.java)
    }
}
