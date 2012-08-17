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
import java.net.InetAddress;
import java.net.ServerSocket;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.openecard.client.connector.handler.ConnectorHandlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ConnectorServer implements Runnable {

    public static final int DEFAULT_PORT = 24727;
    public static final int RANDOM_PORT = 0;

    private static final Logger _logger = LoggerFactory.getLogger(ConnectorServer.class);
    private static final int backlog = 10;

    private final Thread thread;
    private final ServerSocket server;
    private final ConnectorHTTPService httpService;

    /**
     * Create a new ConnectorServer.
     * The port is opened on the specified port. When the port is 0, then an available port number will be selected.
     *
     * @param port Port the server should listen on.
     * @param handlers ConnectorHandlers
     * @param interceptors ConnectorInterceptors
     * @throws IOException if an I/O error occurs when opening the socket.
     */
    protected ConnectorServer(int port, ConnectorHandlers handlers, BasicHttpProcessor interceptors) throws IOException {
	this.thread = new Thread(this, "Open-eCard Localhost-Binding");
	this.server = new ServerSocket(port, backlog, InetAddress.getByName("127.0.0.1"));
	this.httpService = new ConnectorHTTPService(handlers, interceptors);
    }


    /**
     * Get bound port number of the server socket.
     * The specification defines the number to be 24727, but when used e.g. in an applet, it makes sense to dynamically
     * bind the port. And tell the number to the calling context (the Browser).
     *
     * @return Port number the socket is listening on.
     */
    public int getPortNumber() {
	return server.getLocalPort();
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
		DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
		connection.bind(this.server.accept(), new BasicHttpParams());
		httpService.handle(connection);
	    } catch (Exception e) {
		_logger.error("Exception", e);
	    }
	}
    }

}
