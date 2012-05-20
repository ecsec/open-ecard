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

import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.richclient.RichClient;
import org.openecard.client.richclient.activation.common.ErrorPage;
import org.openecard.client.richclient.activation.messages.ActivationRequest;
import org.openecard.client.richclient.activation.messages.ActivationResponse;
import org.openecard.client.richclient.activation.messages.common.ClientRequest;
import org.openecard.client.richclient.activation.messages.common.ClientResponse;
import org.openecard.client.richclient.handler.ActivationHandler;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ActivationSocketHandler implements Runnable {

    private final Thread t;
    private Socket socket;
    private List<ActivationHandler> handlers;

    /**
     * Creates an new ActivationHandler.
     *
     * @param socket Socket
     */
    public ActivationSocketHandler(Socket socket, List<ActivationHandler> handlers) {
	this.socket = socket;
	this.handlers = handlers;

	t = new Thread(this);
    }

    /**
     * Starts the handler.
     */
    public void start() {
	t.start();
    }

    @Override
    public void run() {
	ClientRequest clientRequest = null;
	ActivationHandler aHandler = null;
	try {
	    try {
		// Handle request
		ActivationRequest activationRequest = new ActivationRequest(socket.getInputStream());

		for (Iterator<ActivationHandler> it = handlers.iterator(); it.hasNext();) {
		    aHandler = it.next();
		    clientRequest = aHandler.handleRequest(activationRequest.getInput());
		    if (clientRequest != null) {
			break;
		    }
		}
	    } catch (Exception ex) {
		Logger.getLogger(ActivationSocketHandler.class.getName()).log(Level.SEVERE, "Exception", ex);
		ActivationResponse response = new ActivationResponse(socket.getOutputStream());
		response.setOutput(new ErrorPage(ex.getMessage()).getHTML());
		throw new RuntimeException();
	    }

	    try {
		// Start client
		RichClient app = RichClient.getInstance();
		ClientResponse clientReponse = app.request(clientRequest);

		// Handle response
		ActivationResponse activationResponse = new ActivationResponse(socket.getOutputStream());
		activationResponse.setOutput(aHandler.handleResponse(clientReponse));
	    } catch (Exception ex) {
		Logger.getLogger(ActivationSocketHandler.class.getName()).log(Level.SEVERE, "Exception", ex);
		ActivationResponse response = new ActivationResponse(socket.getOutputStream());
		response.setOutput(new ErrorPage(ex.getMessage()).getHTML());
	    }
	} catch (Throwable ignore) {
	    // Cannot handle such exceptions
	}

    }
}
