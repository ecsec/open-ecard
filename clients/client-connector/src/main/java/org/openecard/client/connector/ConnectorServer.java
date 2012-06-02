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
package org.openecard.client.connector;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.openecard.client.common.logging.LoggingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ConnectorServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorServer.class);
    private static ConnectorServer connectorServer;
    private final Thread thread;
    private ServerSocket server;
    private int port = 24727;
    private int backlog = 10;

    public static ConnectorServer getInstance() throws Exception {
	if (connectorServer == null) {
	    connectorServer = new ConnectorServer();
	}
	return connectorServer;
    }

    /**
     * Creates a new ConnectorServer.
     *
     * @throws Exception if an I/O error occurs when opening the socket.
     */
    protected ConnectorServer() throws Exception {
	this.thread = new Thread(this);
	this.server = new ServerSocket(port, backlog, InetAddress.getLoopbackAddress());

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
		// <editor-fold defaultstate="collapsed" desc="log exception">
		logger.error(LoggingConstants.THROWING, "Exception", e);
		// </editor-fold>
	    }
	}
    }
}
