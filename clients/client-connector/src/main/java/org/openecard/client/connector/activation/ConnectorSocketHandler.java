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
package org.openecard.client.connector.activation;

import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.connector.common.ErrorPage;
import org.openecard.client.connector.handler.ConnectorHandler;
import org.openecard.client.connector.messages.ActivationRequest;
import org.openecard.client.connector.messages.ActivationResponse;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ConnectorSocketHandler implements Runnable {

    private final Thread thread;
    private Socket socket;

    /**
     * Creates an new ConnectorHandler.
     *
     * @param socket Socket
     */
    public ConnectorSocketHandler(Socket socket) {
	this.socket = socket;
	thread = new Thread(this);
    }

    /**
     * Starts the handler.
     */
    public void start() {
	thread.start();
    }

    @Override
    public void run() {
	ClientRequest clientRequest = null;
	ClientResponse clientReponse = null;
	ConnectorHandler aHandler = null;

	try {
	    try {
		// Handle request
		ActivationRequest activationRequest = new ActivationRequest(socket.getInputStream());
		List<ConnectorHandler> handlers = Connector.getInstance().getConnectorHandlers();

		for (Iterator<ConnectorHandler> it = handlers.iterator(); it.hasNext();) {
		    aHandler = it.next();
		    clientRequest = aHandler.handleRequest(activationRequest.getInput());
		    if (clientRequest != null) {
			break;
		    }
		}
	    } catch (Exception ex) {
		Logger.getLogger(ConnectorSocketHandler.class.getName()).log(Level.SEVERE, "Exception", ex);
		ActivationResponse response = new ActivationResponse(socket.getOutputStream());
		response.setOutput(new ErrorPage(ex.getMessage()).getHTML());
		throw new RuntimeException();
	    }

	    try {
		// Start client
		List<ConnectorListener> listeners = Connector.getInstance().getConnectorListeners();

		for (Iterator<ConnectorListener> it = listeners.iterator(); it.hasNext();) {
		    ConnectorListener listener = it.next();
		    clientReponse = listener.request(clientRequest);
		    if (clientReponse != null) {
			break;
		    }
		}

		// Handle response
		ActivationResponse activationResponse = new ActivationResponse(socket.getOutputStream());
		activationResponse.setOutput(aHandler.handleResponse(clientReponse));
	    } catch (Exception ex) {
		Logger.getLogger(ConnectorSocketHandler.class.getName()).log(Level.SEVERE, "Exception", ex);
		ActivationResponse response = new ActivationResponse(socket.getOutputStream());
		response.setOutput(new ErrorPage(ex.getMessage()).getHTML());
	    }
	} catch (Throwable ignore) {
	    // Cannot handle such exceptions
	}

    }
}
