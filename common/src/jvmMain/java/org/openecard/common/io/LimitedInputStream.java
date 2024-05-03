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

package org.openecard.common.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Implements a input stream with a limit.
 *
 * @author Moritz Horsch
 */
public class LimitedInputStream extends FilterInputStream {

    private int limit;

    /**
     * Creates a new limited input stream.
     *
     * @param inputStream Input stream
     */
    public LimitedInputStream(InputStream inputStream) {
	// Default limit is 1 MB
	this(inputStream, 1048576);
    }

    /**
     * Creates a new limited input stream.
     *
     * @param inputStream Input stream
     * @param limit Limit
     */
    public LimitedInputStream(InputStream inputStream, int limit) {
	super(inputStream);
	this.limit = limit;
    }

    @Override
    public int read() throws IOException {
	int res = super.read();
	if (res != -1) {
	    limit--;
	    checkLimit();
	}
	return res;
    }

    @Override
    public int read(byte[] b) throws IOException {
	return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
	int res = super.read(b, off, len);
	if (res != -1) {
	    limit -= len;
	    checkLimit();
	}
	return res;
    }

    /**
     * Checks if the limit of the stream is reached.
     *
     * @throws IOException
     */
    private void checkLimit() throws IOException {
	if (limit < 1) {
	    throw new IOException("Input streams limit is reached.");
	}
    }

}
