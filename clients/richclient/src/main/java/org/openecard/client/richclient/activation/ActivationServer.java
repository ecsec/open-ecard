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
package org.openecard.client.richclient.activation;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.logging.LogManager;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ActivationServer implements Runnable {

    private static final Logger logger = LogManager.getLogger(ActivationServer.class.getName());

    private ServerSocket server;
    private int port = 24727;
    private int backlog = 10;
    private boolean running;
    private Thread t;

    /**
     * Creates a new ActivationSocket.
     *
     * @throws IOException
     */
    protected ActivationServer() throws IOException {
	t = new Thread(this);
	server = new ServerSocket(port, backlog, InetAddress.getLoopbackAddress());
    }

    /**
     * Starts the server.
     */
    public void start() {
	running = true;
	t.start();
    }

    /**
     * Stops the server.
     */
    public void stop() {
	running = false;
    }

    /**
     * Closes the server.
     */
    public void close() {
	try {
	    server.close();
	} catch (IOException ignore) {
	}
    }

    @Override
    public void run() {
	while (running) {
	    try {
		Socket socket = server.accept();
		ActivationHandler handler = new ActivationHandler(socket);
		handler.start();
	    } catch (Exception ex) {
		logger.log(Level.SEVERE, "Cannot handle activation: " + ex.getMessage(), ex);
	    }
	}
    }

}
