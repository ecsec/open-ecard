/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of a promise resembling the semantics of a future.
 * This is a wrapper, so that a future delivery mechanism can be used where promises are used otherwise.
 *
 * @author Tobias Wich
 * @param <T> The result type of the promise.
 */
public class FuturePromise<T> extends Promise<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FuturePromise.class);
    private static final AtomicInteger THREAD_NUM = new AtomicInteger(1);

    public FuturePromise(final Callable<T> function) {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		T result;
		try {
		    result = function.call();
		} catch (Exception ex) {
		    LOG.error("Failed to complete computation of the result.", ex);
		    result = null;
		}
		// We have either a result or an error
		FuturePromise.super.deliver(result);
	    }
	}, "FuturePromise-" + THREAD_NUM.getAndIncrement()).start();
    }

    @Override
    public synchronized T deliver(T value) throws IllegalStateException {
	// nobody should call this function, as the delivery is performed by the background thread
	throw new IllegalStateException("The promise is already delivered.");
    }

}
