/*
 * Copyright 2012 Moritz Horsch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.richclient.activation.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * Implements a input stream with a limit.
 *
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class LimitedInputStream extends FilterInputStream {

    // Set limit to 1 MB
    private int limit = 1048576;

    /**
     * Creates a new limited input stream.
     *
     * @param inputStream Input stream
     */
    public LimitedInputStream(InputStream inputStream) {
	super(inputStream);
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
    public int read(byte b[]) throws IOException {
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
