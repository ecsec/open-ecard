/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.control.binding.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;
import org.openecard.apache.http.ConnectionClosedException;
import org.openecard.apache.http.ConnectionReuseStrategy;
import org.openecard.apache.http.HttpRequestInterceptor;
import org.openecard.apache.http.HttpResponseFactory;
import org.openecard.apache.http.HttpResponseInterceptor;
import org.openecard.apache.http.impl.DefaultBHttpServerConnection;
import org.openecard.apache.http.impl.DefaultConnectionReuseStrategy;
import org.openecard.apache.http.impl.DefaultHttpResponseFactory;
import org.openecard.apache.http.protocol.BasicHttpContext;
import org.openecard.apache.http.protocol.HttpProcessor;
import org.openecard.apache.http.protocol.HttpRequestHandler;
import org.openecard.apache.http.protocol.ImmutableHttpProcessor;
import org.openecard.apache.http.protocol.UriHttpRequestHandlerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch
 * @author Tobias Wich
 */
public class HttpService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HttpService.class);
    private static final int backlog = 10;
    private final Thread thread;
    private final org.openecard.apache.http.protocol.HttpService service;
    protected final ServerSocket server;

    /**
     * Creates a new HTTPService.
     *
     * @param port Port
     * @param handler Handler
     * @param reqInterceptors
     * @param respInterceptors
     * @throws Exception
     */
    public HttpService(int port, HttpRequestHandler handler, List<HttpRequestInterceptor> reqInterceptors,
	    List<HttpResponseInterceptor> respInterceptors) throws Exception {
	thread = new Thread(this, "Open-eCard Localhost-Binding");
	server = new ServerSocket(port, backlog, InetAddress.getByName("127.0.0.1"));
	logger.debug("Starting HTTPBinding on port {}", server.getLocalPort());

	// Reuse strategy
	ConnectionReuseStrategy connectionReuseStrategy = new DefaultConnectionReuseStrategy();
	// Response factory
	HttpResponseFactory responseFactory = new DefaultHttpResponseFactory();
	// Interceptors
	HttpProcessor httpProcessor = new ImmutableHttpProcessor(reqInterceptors, respInterceptors);

	// Set up handler registry
	UriHttpRequestHandlerMapper handlerRegistry = new UriHttpRequestHandlerMapper();
	logger.debug("Add handler [{}] for ID [{}]", new Object[]{handler.getClass().getCanonicalName(), "*"});
	handlerRegistry.register("*", handler);

	// create service instance
	service = new org.openecard.apache.http.protocol.HttpService(httpProcessor, connectionReuseStrategy, responseFactory, handlerRegistry);
    }

    /**
     * Starts the server.
     */
    public void start() {
	thread.start();
    }

    /**
     * Interrupts the server.
     */
    public void interrupt() {
	try {
	    thread.interrupt();
	    server.close();
	} catch (Exception ignore) {
	}
    }

    protected Socket accept() throws IOException, HttpServiceError {
	return this.server.accept();
    }

    @Override
    public void run() {
	while (! Thread.interrupted()) {
	    try {
		final DefaultBHttpServerConnection connection;
		CharsetDecoder dec = Charset.forName("UTF-8").newDecoder();
		CharsetEncoder enc = Charset.forName("UTF-8").newEncoder();
		connection = new DefaultBHttpServerConnection(8192, dec, enc, null);
		connection.bind(accept());

		new Thread() {
		    @Override
		    public void run() {
			try {
			    while (connection.isOpen()) {
				service.handleRequest(connection, new BasicHttpContext());
			    }
			} catch (ConnectionClosedException ex) {
			    // connection closed by client, this is the expected outcome
			} catch (org.openecard.apache.http.HttpException ex) {
			    logger.error("Error processing HTTP request or response.", ex);
			} catch (IOException ex) {
			    logger.error("IO Error while processing HTTP request or response.", ex);
			} finally {
			    try {
				connection.shutdown();
			    } catch (IOException ignore) {
			    }
			}
		    }

		}.start();
	    } catch (HttpsServiceError ex) {
		logger.error("Failed to initialize TLS server socket.", ex);
		// no chance this gets better, just close down the server
		return;
	    } catch (IOException | HttpServiceError ex) {
		logger.error(ex.getMessage(), ex);
	    }
	}
    }

    /**
     * Returns the port number on which the HTTP binding is listening.
     *
     * @return Port
     */
    public int getPort() {
	return server.getLocalPort();
    }

}
