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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.openecard.apache.http.impl.AbstractHttpClientConnection;
import org.openecard.apache.http.params.HttpParams;


/**
 * Stream based HTTP client. <br/>
 * In contrast to the default client ({@link org.apache.http.impl.DefaultHttpClientConnection}, this implementation
 * uses an already existing connection instead of creating a new one.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StreamHttpClientConnection extends AbstractHttpClientConnection {

    private static final int BUFSIZE = 4 * 1024;

    private final InputStream in;
    private final OutputStream out;
    private final HttpParams params;

    private boolean open = true;

    /**
     * Create a HTTP client connection based on an already existing and opened stream.
     * This constructor relays its initialisation to
     * {@link #StreamHttpClientConnection(InputStream, OutputStream, HttpParams)}. The HttpParams element is initialised
     * to an unmodified instance of {@link OECBasicHttpParams}.
     *
     * @param in Input stream to the server.
     * @param out Output stream to the server.
     */
    public StreamHttpClientConnection(InputStream in, OutputStream out) {
	this(in, out, new OECBasicHttpParams());
    }
    /**
     * Create a HTTP client connection based on an already existing and opened stream.
     *
     * @param in Input stream to the server.
     * @param out Output stream to the server.
     * @param params Parameters of the HTTP connection.
     */
    public StreamHttpClientConnection(InputStream in, OutputStream out, HttpParams params) {
	this.in = in;
	this.out = out;
	this.params = params;

	StreamSessionInputBuffer sin = new StreamSessionInputBuffer(in, BUFSIZE, params);
	StreamSessionOutputBuffer sout = new StreamSessionOutputBuffer(out, BUFSIZE, params);

	init(sin, sout, params);
    }


    /**
     * Get HttpParams of this client connection.
     *
     * @return Mutable instance of the HTTP parameters of this connection.
     */
    public HttpParams getParams() {
	return params;
    }


    @Override
    protected void assertOpen() throws IllegalStateException {
	if (! isOpen()) {
	    throw new IllegalStateException("Underlying channel of HTTP connection is not open.");
	}
    }

    @Override
    public boolean isOpen() {
	return open;
    }

    @Override
    public void shutdown() throws IOException {
	open = false;
	in.close();
	out.close();
    }

    @Override
    public void close() throws IOException {
	if (open) {
	    open = false;
	    // send pending bytes
	    doFlush();
	    // close streams gracefully
	    try {
		in.close();
	    } catch (IOException ex) {
		// ignore
	    }
	    try {
		out.close();
	    } catch (IOException ex) {
		// ignore
	    }
	}
    }

    @Override
    public void setSocketTimeout(int timeout) {
	throw new UnsupportedOperationException("Not supported in this type of connection.");
    }

    @Override
    public int getSocketTimeout() {
	throw new UnsupportedOperationException("Not supported in this type of connection.");
    }

}
