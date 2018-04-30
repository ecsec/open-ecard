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
import org.openecard.apache.http.Header;
import org.openecard.apache.http.HttpClientConnection;
import org.openecard.apache.http.HttpConnectionMetrics;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpEntityEnclosingRequest;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpMessage;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpStatus;
import org.openecard.apache.http.config.MessageConstraints;
import org.openecard.apache.http.entity.BasicHttpEntity;
import org.openecard.apache.http.entity.ContentLengthStrategy;
import org.openecard.apache.http.impl.HttpConnectionMetricsImpl;
import org.openecard.apache.http.impl.entity.LaxContentLengthStrategy;
import org.openecard.apache.http.impl.entity.StrictContentLengthStrategy;
import org.openecard.apache.http.impl.io.ChunkedInputStream;
import org.openecard.apache.http.impl.io.ChunkedOutputStream;
import org.openecard.apache.http.impl.io.ContentLengthInputStream;
import org.openecard.apache.http.impl.io.ContentLengthOutputStream;
import org.openecard.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.openecard.apache.http.impl.io.DefaultHttpResponseParserFactory;
import org.openecard.apache.http.impl.io.IdentityInputStream;
import org.openecard.apache.http.impl.io.IdentityOutputStream;
import org.openecard.apache.http.io.HttpMessageParser;
import org.openecard.apache.http.io.HttpMessageWriter;
import org.openecard.apache.http.message.BasicHeader;
import org.openecard.apache.http.protocol.HTTP;
import org.openecard.common.AppVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Stream based HTTP client. <br>
 * In contrast to the default client ({@link org.openecard.apache.http.impl.DefaultBHttpClientConnection}, this
 * implementation uses an already existing connection instead of creating a new one.
 *
 * @author Tobias Wich
 */
public class StreamHttpClientConnection implements HttpClientConnection {

    private static final Logger logger = LoggerFactory.getLogger(StreamHttpClientConnection.class);

    private static final int BUFSIZE = 4 * 1024;

    private final InputStream in;
    private final OutputStream out;
    private final StreamSessionInputBuffer sin;
    private final StreamSessionOutputBuffer sout;
    private final HttpConnectionMetricsImpl metrics;
    private final HttpMessageParser<HttpResponse> responseParser;
    private final HttpMessageWriter<HttpRequest> requestWriter;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;

    private boolean open = true;

    /**
     * Create a HTTP client connection based on an already existing and opened stream.
     *
     * @param in Input stream to the server.
     * @param out Output stream to the server.
     */
    public StreamHttpClientConnection(InputStream in, OutputStream out) {
	this.in = in;
	this.out = out;
	this.sin = new StreamSessionInputBuffer(in, BUFSIZE);
	this.sout = new StreamSessionOutputBuffer(out, BUFSIZE);
	this.metrics = new HttpConnectionMetricsImpl(sin.getMetrics(), sout.getMetrics());
        this.requestWriter = DefaultHttpRequestWriterFactory.INSTANCE.create(sout);
        this.responseParser = DefaultHttpResponseParserFactory.INSTANCE.create(sin, MessageConstraints.DEFAULT);
        this.incomingContentStrategy = LaxContentLengthStrategy.INSTANCE;
        this.outgoingContentStrategy = StrictContentLengthStrategy.INSTANCE;

    }


    @Override
    public boolean isOpen() {
	return open;
    }

    @Override
    public void shutdown() throws IOException {
	open = false;
	try {
	    in.close();
	} catch (IOException ex) {
	    logger.warn("Error forcibly closing input stream.");
	}
	try {
	    out.close();
	} catch (IOException ex) {
	    logger.warn("Error forcibly closing output stream.");
	}
    }

    @Override
    public void close() throws IOException {
	if (open) {
	    open = false;
	    // send pending bytes
	    flush();
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

    private void assertOpen() throws IOException {
	if (! isOpen()) {
	    throw new IOException("HTTP connection is closed.");
	}
    }

    @Override
    public void setSocketTimeout(int timeout) {
	// ignore
	logger.info("Not supported in this type of connection.");
    }

    @Override
    public int getSocketTimeout() {
	// pretend to know the timeout value, set to infinite
	return 0;
    }

    @Override
    public boolean isResponseAvailable(int timeout) throws IOException {
	assertOpen();
	return sin.isDataAvailable(timeout);
    }

    @Override
    public void sendRequestHeader(HttpRequest request) throws HttpException, IOException {
	assertOpen();
	request.setHeader(new BasicHeader("User-Agent", AppVersion.getName() + "/" + AppVersion.getVersion()));
        this.requestWriter.write(request);
        incrementRequestCount();
    }

    @Override
    public void sendRequestEntity(HttpEntityEnclosingRequest request) throws HttpException, IOException {
	assertOpen();
        final HttpEntity entity = request.getEntity();
        if (entity == null) {
            return;
        }
        final OutputStream outstream = prepareOutput(request);
        entity.writeTo(outstream);
        outstream.close();
    }

    @Override
    public HttpResponse receiveResponseHeader() throws HttpException, IOException {
	assertOpen();
        final HttpResponse response = this.responseParser.parse();
        if (response.getStatusLine().getStatusCode() >= HttpStatus.SC_OK) {
            incrementResponseCount();
        }
        return response;
    }

    @Override
    public void receiveResponseEntity(HttpResponse response) throws HttpException, IOException {
	assertOpen();
        final HttpEntity entity = prepareInput(response);
        response.setEntity(entity);
    }

    @Override
    public void flush() throws IOException {
	sout.flush();
    }

    @Override
    public boolean isStale() {
	// TODO: determine whether the connection is still established by probing the input stream
	return isOpen();
    }

    @Override
    public HttpConnectionMetrics getMetrics() {
	return metrics;
    }

    protected void incrementRequestCount() {
        metrics.incrementRequestCount();
    }

    protected void incrementResponseCount() {
        metrics.incrementResponseCount();
    }

    protected OutputStream createOutputStream(long len) {
        if (len == ContentLengthStrategy.CHUNKED) {
            return new ChunkedOutputStream(2048, sout);
        } else if (len == ContentLengthStrategy.IDENTITY) {
            return new IdentityOutputStream(sout);
        } else {
            return new ContentLengthOutputStream(sout, len);
        }
    }

    protected OutputStream prepareOutput(final HttpMessage message) throws HttpException {
        final long len = this.outgoingContentStrategy.determineLength(message);
        return createOutputStream(len);
    }

    protected InputStream createInputStream(long len) {
        if (len == ContentLengthStrategy.CHUNKED) {
            return new ChunkedInputStream(sin);
        } else if (len == ContentLengthStrategy.IDENTITY) {
            return new IdentityInputStream(sin);
        } else {
            return new ContentLengthInputStream(sin, len);
        }
    }

    protected HttpEntity prepareInput(final HttpMessage message) throws HttpException {
        final BasicHttpEntity entity = new BasicHttpEntity();

        final long len = this.incomingContentStrategy.determineLength(message);
        final InputStream instream = createInputStream(len);
        if (len == ContentLengthStrategy.CHUNKED) {
            entity.setChunked(true);
            entity.setContentLength(-1);
            entity.setContent(instream);
        } else if (len == ContentLengthStrategy.IDENTITY) {
            entity.setChunked(false);
            entity.setContentLength(-1);
            entity.setContent(instream);
        } else {
            entity.setChunked(false);
            entity.setContentLength(len);
            entity.setContent(instream);
        }

        final Header contentTypeHeader = message.getFirstHeader(HTTP.CONTENT_TYPE);
        if (contentTypeHeader != null) {
            entity.setContentType(contentTypeHeader);
        }
        final Header contentEncodingHeader = message.getFirstHeader(HTTP.CONTENT_ENCODING);
        if (contentEncodingHeader != null) {
            entity.setContentEncoding(contentEncodingHeader);
        }
        return entity;
    }

}
