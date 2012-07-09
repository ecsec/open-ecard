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

import java.net.Socket;
import java.util.Iterator;
import java.util.List;
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
    private final Socket socket;
    private final ConnectorHandlers handlers;
    private final ConnectorListeners listeners;


    /**
     * Creates an new ConnectorHandler.
     *
     * @param socket Socket
     */
    public ConnectorSocketHandler(Socket socket, ConnectorHandlers handlers, ConnectorListeners listeners) {
	this.socket = socket;
	this.handlers = handlers;
	this.listeners = listeners;
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

		List<ConnectorHandler> handlerList = this.handlers.getConnectorHandlers();
		for (Iterator<ConnectorHandler> it = handlerList.iterator(); it.hasNext();) {
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
		logger.error("Exception", e);
		// </editor-fold>

		HTTPResponse httpResponse = new HTTPResponse();
		httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.INTERNAL_SERVER_ERROR_500));
		httpResponse.setOutputStream(socket.getOutputStream());

		throw new RuntimeException();
	    }

	    try {
		// Start client
		List<ConnectorListener> listenerList = this.listeners.getConnectorListeners();

		for (Iterator<ConnectorListener> it = listenerList.iterator(); it.hasNext();) {
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
		logger.error("Exception", e);

		HTTPResponse httpResponse = new HTTPResponse();
		httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.INTERNAL_SERVER_ERROR_500));
		httpResponse.setOutputStream(socket.getOutputStream());
	    }
	} catch (Throwable ignore) {
	    // FIXME: get rid of Throwable catch
	    // Cannot handle such exceptions
	}

    }
}
