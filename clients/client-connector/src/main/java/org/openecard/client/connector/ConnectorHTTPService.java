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

package org.openecard.client.connector;

import java.io.IOException;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpServerConnection;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.protocol.*;
import org.openecard.client.connector.handler.ConnectorHandler;
import org.openecard.client.connector.handler.ConnectorHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ConnectorHTTPService {

    private static final Logger _logger = LoggerFactory.getLogger(ConnectorHTTPService.class);

    private static HttpService service;

    /**
     * Creates a new ConnectorHTTPService.
     *
     * @param handlers ConnectorHandlers
     * @param interceptors ConnectorInterceptors
     */
    public ConnectorHTTPService(ConnectorHandlers handlers, BasicHttpProcessor interceptors) {
	ConnectionReuseStrategy connectionReuseStrategy = new DefaultConnectionReuseStrategy();
	HttpResponseFactory responseFactory = new DefaultHttpResponseFactory();

	// Interceptors
	HttpProcessor httpProcessor = new ImmutableHttpProcessor(interceptors, interceptors);

	// Deprecated since 4.1 but the only constructor in Android
	service = new HttpService(httpProcessor, connectionReuseStrategy, responseFactory);

	// Set up handler registry
	HttpRequestHandlerRegistry handlerRegistry = new HttpRequestHandlerRegistry();
	for (ConnectorHandler handler : handlers.getConnectorHandlers()) {
	    _logger.debug("Add handler [{}] for path [{}]", new Object[]{handler.getClass().getCanonicalName(), handler.getPath()});
	    handlerRegistry.register(handler.getPath(), handler);
	}

	// Add handler registry
	service.setHandlerResolver(handlerRegistry);
    }

    /**
     * Handles a connection.
     *
     * @param connection HttpServerConnection
     */
    public void handle(final HttpServerConnection connection) {
	new Thread() {
	    @Override
	    public void run() {
		try {
		    if (connection.isOpen()) {
			service.handleRequest(connection, new BasicHttpContext());
		    }
		} catch (Exception e) {
		    _logger.error("Exception", e);
		} finally {
		    try {
			connection.shutdown();
		    } catch (IOException ignore) {
		    }
		}
	    }

	}.start();
    }

}
