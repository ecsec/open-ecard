/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.transport.httpcore;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openecard.apache.http.impl.io.AbstractSessionInputBuffer;
import org.openecard.apache.http.params.HttpParams;


/**
 * Stream based input buffer for use in Apache httpcore.
 *
 * @author Tobias Wich
 */
public class StreamSessionInputBuffer extends AbstractSessionInputBuffer {

    private static final ExecutorService exec = Executors.newCachedThreadPool();

    /**
     * Creates a StreamSessionInputBuffer instance based on a given InputStream.
     *
     * @param in The destination input stream.
     * @param bufsize The size of the internal buffer.
     * @param params HTTP parameters.
     */
    public StreamSessionInputBuffer(InputStream in, int bufsize, HttpParams params) {
	// use a buffer stream, so the mark/reset operation is supported
	init(new BufferedInputStream(in, bufsize), bufsize, params);
    }

    @Override
    public boolean isDataAvailable(int timeout) throws IOException {
	Future<Boolean> task = exec.submit(new DataAvailabilityChecker());
	try {
	    if (timeout == 0) {
		return task.get();
	    } else {
		return task.get(timeout, TimeUnit.MILLISECONDS);
	    }
	} catch (TimeoutException ex) {
	    throw new IOException("Wait for data timed out.", ex);
	} catch (ExecutionException ex) {
	    throw (IOException) ex.getCause();
	} catch (InterruptedException ex) {
	    // these types of exception occur by interrupt and cancel methods
	    // it is no use proceeding after them, returning no data available seems to be the right choice
	    return false;
	}
    }

    private class DataAvailabilityChecker implements Callable<Boolean> {
	@Override
	public Boolean call() throws IOException {
	    // first check buffer if it already has some buffered data
	    boolean result = hasBufferedData();
	    if (!result) {
		// no data, try to fetch some
		fillBuffer();
		result = hasBufferedData();
	    }
	    return result;
	}
    }

}
