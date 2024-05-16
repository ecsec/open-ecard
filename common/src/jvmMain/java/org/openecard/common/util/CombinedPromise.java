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
 ***************************************************************************/

package org.openecard.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility to wait for a buch of promises.
 *
 * @author Tobias Wich
 * @param <T> Type of the promise value.
 */
public class CombinedPromise <T> {

    private static final Logger LOG = LoggerFactory.getLogger(CombinedPromise.class);

    private final List<Promise<T>> promises;

    public CombinedPromise(List<Promise<T>> promises) {
	this.promises = promises;
    }

    /**
     * Waits for any of the promises to finish.
     *
     * @return
     * @throws InterruptedException
     */
    public T retrieveFirst() throws InterruptedException {
	ArrayList<Future<T>> futures = new ArrayList<>();
	try {
	    Executor e = Executors.newFixedThreadPool(promises.size());
	    CompletionService<T> cs = new ExecutorCompletionService<>(e);

	    // submit all tasks
	    for (Promise<T> next : promises) {
		Future<T> f = cs.submit(makeCallable(next));
		futures.add(f);
	    }

	    // wait for a result
	    return cs.take().get();
	} catch (ExecutionException ex) {
	    // propagate InterruptedException
	    if (ex.getCause() instanceof InterruptedException) {
		throw (InterruptedException) ex.getCause();
	    } else {
		LOG.error("Unexpected exception see while waiting for UI promise.", ex);
		throw new IllegalStateException("Unexpected exception see while waiting for UI promise.");
	    }
	} finally {
	    for (Future<T> f : futures) {
		f.cancel(true);
	    }
	}
    }

    private Callable<T> makeCallable(final Promise<T> p) {
	return new Callable<T>() {
	    @Override
	    public T call() throws Exception {
		return p.deref();
	    }
	};
    }

}
