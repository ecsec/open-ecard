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

import java.net.Socket;
import java.util.Iterator;
import java.util.List;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.connector.handler.ConnectorHandler;
import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.http.HTTPStatusCode;
import org.openecard.client.connector.http.header.StatusLine;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ConnectorSocketHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorSocketHandler.class);
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
		HTTPRequest httpRequest = new HTTPRequest();
		httpRequest.setInputStream(socket.getInputStream());

		List<ConnectorHandler> handlers = Connector.getInstance().getConnectorHandlers();
		for (Iterator<ConnectorHandler> it = handlers.iterator(); it.hasNext();) {
		    aHandler = it.next();
		    clientRequest = aHandler.handleRequest(httpRequest);
		    if (clientRequest != null) {
			break;
		    }
		}

		if (clientRequest == null) {
		    HTTPResponse httpResponse = new HTTPResponse();
		    httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.BAD_REQUEST_400));
		    httpResponse.setOutputStream(socket.getOutputStream());
		    throw new ConnectorException("Cannot handle such a request");
		}

	    } catch (IllegalArgumentException e) {
		HTTPResponse httpResponse = new HTTPResponse();
		httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.BAD_REQUEST_400));
		httpResponse.setOutputStream(socket.getOutputStream());
		throw new ConnectorException("Cannot handle such a request");
	    } catch (Exception e) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
		logger.error(LoggingConstants.THROWING, "Exception", e);
		// </editor-fold>

		HTTPResponse httpResponse = new HTTPResponse();
		httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.INTERNAL_SERVER_ERROR_500));
		httpResponse.setOutputStream(socket.getOutputStream());

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
		HTTPResponse httpResponse = aHandler.handleResponse(clientReponse);
		httpResponse.setOutputStream(socket.getOutputStream());
	    } catch (Exception e) {
		// <editor-fold defaultstate="collapsed" desc="log exception">
		logger.error(LoggingConstants.THROWING, "Exception", e);
		// </editor-fold>

		HTTPResponse httpResponse = new HTTPResponse();
		httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.INTERNAL_SERVER_ERROR_500));
		httpResponse.setOutputStream(socket.getOutputStream());
	    }
	} catch (Throwable ignore) {
	    // Cannot handle such exceptions
	}

    }
}
