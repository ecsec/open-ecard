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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.richclient.RichClient;
import org.openecard.client.richclient.activation.messages.ActivationApplicationRequest;
import org.openecard.client.richclient.activation.messages.ActivationApplicationResponse;
import org.openecard.client.richclient.activation.messages.ActivationRequest;
import org.openecard.client.richclient.activation.messages.ActivationResponse;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ActivationHandler implements Runnable {

    private final Thread t;
    private Socket socket;

    /**
     * Creates an new ActivationHandler.
     *
     * @param socket Socket
     */
    public ActivationHandler(Socket socket) {
	this.socket = socket;

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
	ActivationRequest request = null;

	try {
	    try {
		// Handle request
		request = new ActivationRequest(socket.getInputStream());
		request.handleRequest();
	    } catch (Exception ex) {
		Logger.getLogger(ActivationHandler.class.getName()).log(Level.SEVERE, "Exception", ex);
		ActivationResponse response = new ActivationResponse(socket.getOutputStream());
		response.handleErrorResponse(ex.getMessage());
	    }

	    try {
		// Start client
		RichClient app = RichClient.getInstance();
		ActivationApplicationRequest applicationRequest = new ActivationApplicationRequest();
		applicationRequest.setTCToken(request.getTCTokens().get(0));
		ActivationApplicationResponse applicationReponse = app.activate(applicationRequest);

		// Handle response
		ActivationResponse response = new ActivationResponse(socket.getOutputStream());
		if (applicationReponse.getErrorPage() != null) {
		    response.handleErrorPage(applicationReponse.getErrorPage());
		} else if (applicationReponse.getErrorMessage() != null) {
		    response.handleErrorResponse(applicationReponse.getErrorMessage());
		} else {
		    response.handleRedirectResponse(applicationReponse.getRefreshAddress());
		}

	    } catch (Exception ex) {
		Logger.getLogger(ActivationHandler.class.getName()).log(Level.SEVERE, "Exception", ex);
		ActivationResponse response = new ActivationResponse(socket.getOutputStream());
		response.handleErrorResponse(ex.getMessage());
	    }
	} catch (Throwable ignore) {
	    // Cannot handle such exceptions
	}

    }

}
