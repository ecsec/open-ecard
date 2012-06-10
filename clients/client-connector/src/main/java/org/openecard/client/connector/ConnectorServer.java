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

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.openecard.client.common.logging.LoggingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ConnectorServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorServer.class);

    private static final int defaultPort = 24727;
    private static final int backlog = 10;

    private final Thread thread;
    private final int port;
    private final ServerSocket server;

    private static ConnectorServer connectorServer;

    public static ConnectorServer getInstance() throws Exception {
	if (connectorServer == null) {
	    connectorServer = new ConnectorServer(defaultPort);
	}
	return connectorServer;
    }


    /**
     * Creates a new ConnectorServer.
     *
     * @param port Port the server should listen on.
     * @throws Exception if an I/O error occurs when opening the socket.
     */
    protected ConnectorServer(int port) throws Exception {
	this.thread = new Thread(this);
	this.thread.setName("Open-eCard Localhost-Binding");
	this.server = new ServerSocket(port, backlog, InetAddress.getByName("127.0.0.1"));
	this.port = this.server.getLocalPort();
    }


    /**
     * Get bound port number of the server socket.
     * The specification defines the number to be 24727, but when used e.g. in an applet, it makes sense to dynamically
     * bind the port. And tell the number to the calling context (the Browser).
     *
     * @return Port number the socket is listening on.
     */
    public int getPortNumber() {
	return port;
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
	while (!thread.isInterrupted()) {
	    try {
		Socket socket = server.accept();
		ConnectorSocketHandler handler = new ConnectorSocketHandler(socket);
		handler.start();
	    } catch (Exception e) {
		logger.error(LoggingConstants.THROWING, e.getMessage(), e);
	    }
	}
    }

}
