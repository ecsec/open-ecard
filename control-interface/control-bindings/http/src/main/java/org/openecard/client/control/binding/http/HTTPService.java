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
package org.openecard.client.control.binding.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponseFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.openecard.client.control.handler.ControlHandler;
import org.openecard.client.control.handler.ControlHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class HTTPService implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HTTPService.class);
    private static final int backlog = 10;
    private final Thread thread;
    private final ServerSocket server;
    private final HttpService service;

    /**
     * Creates a new HTTPService.
     * 
     * @param port Port
     * @param handlers Handlers
     * @param interceptors Interceptors
     * @throws Exception
     */
    public HTTPService(int port, ControlHandlers handlers, BasicHttpProcessor interceptors) throws Exception {
	thread = new Thread(this, "Open-eCard Localhost-Binding");
	server = new ServerSocket(port, backlog, InetAddress.getByName("127.0.0.1"));
	logger.debug("Starting HTTPBinding on port {}", server.getLocalPort());

	// Reuse strategy
	ConnectionReuseStrategy connectionReuseStrategy = new DefaultConnectionReuseStrategy();
	// Response factory
	HttpResponseFactory responseFactory = new DefaultHttpResponseFactory();
	// Interceptors
	HttpProcessor httpProcessor = new ImmutableHttpProcessor(interceptors, interceptors);
	// Deprecated since 4.1 but the only constructor in Android
	service = new HttpService(httpProcessor, connectionReuseStrategy, responseFactory);

	// Set up handler registry
	HttpRequestHandlerRegistry handlerRegistry = new HttpRequestHandlerRegistry();
	for (ControlHandler handler : handlers.getControlHandlers()) {
	    if (handler instanceof HttpRequestHandler) {
		logger.debug("Add handler [{}] for ID [{}]", new Object[]{handler.getClass().getCanonicalName(), handler.getID()});
		handlerRegistry.register(handler.getID(), (HttpRequestHandler) handler);
	    } else {
		logger.error("Handler [{}] is not supported by the HTTPBinding");
	    }
	}

	// Add handler registry
	service.setHandlerResolver(handlerRegistry);
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

    @Override
    public void run() {
	while (!Thread.interrupted()) {
	    try {
		final DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
		connection.bind(this.server.accept(), new BasicHttpParams());

		new Thread() {
		    @Override
		    public void run() {
			try {
			    if (connection.isOpen()) {
				service.handleRequest(connection, new BasicHttpContext());
			    }
			} catch (Exception e) {
			    logger.error("Exception", e);
			} finally {
			    try {
				connection.shutdown();
			    } catch (IOException ignore) {
			    }
			}
		    }

		}.start();
	    } catch (Exception e) {
		logger.error("Exception", e);
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
