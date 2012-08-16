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

import org.apache.http.protocol.BasicHttpProcessor;
import org.openecard.client.connector.client.ConnectorListeners;
import org.openecard.client.connector.common.DocumentRoot;
import org.openecard.client.connector.handler.ConnectorHandlers;
import org.openecard.client.connector.handler.common.DefaultHandler;
import org.openecard.client.connector.handler.common.FileHandler;
import org.openecard.client.connector.handler.common.IndexHandler;
import org.openecard.client.connector.handler.status.StatusHandler;
import org.openecard.client.connector.handler.tctoken.TCTokenHandler;
import org.openecard.client.connector.interceptor.CORSRequestInterceptor;
import org.openecard.client.connector.interceptor.CORSResponseInterceptor;
import org.openecard.client.connector.interceptor.ErrorResponseInterceptor;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class Connector {

    private final ConnectorHandlers handlers = new ConnectorHandlers();
    private final ConnectorListeners listeners = new ConnectorListeners();
    private final BasicHttpProcessor interceptors = new BasicHttpProcessor();
    private final ConnectorServer connectorServer;
    private final DocumentRoot documentRoot;

    /**
     * Creates a new Connector.
     *
     * @param port Port the server should listen on.
     * @throws Exception If an I/O error occurs when opening the socket
     */
    public Connector(int port) throws Exception {
	this(port, "/www");
    }

    /**
     * Creates a new Connector.
     *
     * @param port Port the server should listen on.
     * @param documentRootPath Path of the document root
     * @throws Exception If an I/O error occurs when opening the socket
     */
    public Connector(int port, String documentRootPath) throws Exception {
	// Create document root
	documentRoot = new DocumentRoot(documentRootPath);

	// Add handlers
	handlers.addConnectorHandler(new TCTokenHandler(listeners));
	handlers.addConnectorHandler(new StatusHandler(listeners));
	handlers.addConnectorHandler(new IndexHandler());
	handlers.addConnectorHandler(new FileHandler(documentRoot));
	handlers.addConnectorHandler(new DefaultHandler());

	// Add interceptors
	interceptors.addInterceptor(new ErrorResponseInterceptor(documentRoot, "/templates/error.html"));
	interceptors.addInterceptor(new CORSResponseInterceptor());
	interceptors.addInterceptor(new CORSRequestInterceptor());

	// Start up server
	connectorServer = new ConnectorServer(port, handlers, interceptors);
	connectorServer.start();
    }

    /**
     * Create a new Connector.
     * The port is set to any available number.
     *
     * @throws Exception If an I/O error occurs when opening the socket
     */
    public Connector() throws Exception {
	this(0);
    }

    /**
     * Returns the handlers.
     *
     * @return Handlers
     */
    public ConnectorHandlers getHandlers() {
	return handlers;
    }

    /**
     * Returns the listeners.
     *
     * @return Listeners
     */
    public ConnectorListeners getListeners() {
	return listeners;
    }

    /**
     * @see ConnectorServer#getPortNumber()
     * @return Port number where the connector is reachable
     */
    public int getPortNumber() {
	return connectorServer.getPortNumber();
    }

}
