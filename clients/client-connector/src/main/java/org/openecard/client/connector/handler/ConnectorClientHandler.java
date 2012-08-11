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

package org.openecard.client.connector.handler;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.openecard.client.connector.ConnectorException;
import org.openecard.client.connector.ConnectorHTTPException;
import org.openecard.client.connector.client.ClientRequest;
import org.openecard.client.connector.client.ClientResponse;
import org.openecard.client.connector.client.ConnectorListener;
import org.openecard.client.connector.client.ConnectorListeners;
import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.http.HTTPStatusCode;
import org.openecard.client.connector.http.header.StatusLine;
import org.openecard.client.connector.interceptor.cors.CORSRequestInterceptor;
import org.openecard.client.connector.interceptor.cors.CORSResponseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class ConnectorClientHandler extends ConnectorHandler {

    private static final Logger _logger = LoggerFactory.getLogger(ConnectorClientHandler.class);

    private ConnectorListeners listeners;


    /**
     * Creates a new ConnectorRequestHandler.
     *
     * @param listeners ConnectorListeners
     * @param path Path
     */
    public ConnectorClientHandler(String path, ConnectorListeners listeners) {
	super(path);
	this.listeners = listeners;
    }

    /**
     * Handles a HTTP request and creates a client request.
     *
     * @param httpRequest HTTP request
     * @return A client request or null
     * @throws Exception If the request should be handled by the handler but is malformed
     */
    public abstract ClientRequest handleRequest(HTTPRequest httpRequest) throws Exception;

    /**
     * Handles a client response and creates a HTTP response.
     *
     * @param clientResponse Client response
     * @return A HTTP response
     * @throws Exception
     */
    public abstract HTTPResponse handleResponse(ClientResponse clientResponse) throws Exception;

    /**
     * Handles a HTTP request.
     *
     * @param request HttpRequest
     * @param response HttpResponse
     * @param context HttpContext
     * @throws HttpException
     * @throws IOException
     */
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
	HTTPResponse httpResponse = new HTTPResponse();

	try {
	    _logger.debug("HTTP request: {}", request.toString());
	    HTTPRequest httpRequest = new HTTPRequest();
	    httpRequest.fromHttpRequest(request);

	    ClientRequest clientRequest = handleRequest(httpRequest);
	    ClientResponse clientResponse = null;

	    if (clientRequest != null) {
		for (ConnectorListener listener : listeners.getConnectorListeners()) {
		    clientResponse = listener.request(clientRequest);
		    if (clientResponse != null) {
			break;
		    }
		}
	    } else {
		throw new ConnectorException();
	    }

	    // Forward HttpContext attributes to response parameters
	    BasicHttpParams params = new BasicHttpParams();
	    params.setParameter(
		    CORSResponseInterceptor.class.getName(),
		    context.getAttribute(CORSRequestInterceptor.class.getName()));
	    response.setParams(params);

	    httpResponse = handleResponse(clientResponse);
	} catch (ConnectorHTTPException e) {
	    httpResponse.setStatusLine(new StatusLine(e.getHTTPStatusCode()));
	    httpResponse.setMessageBody(e.getMessage());
	} catch (ConnectorException e) {
	    httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.BAD_REQUEST_400));
	    httpResponse.setMessageBody(e.getMessage());
	} catch (Exception e) {
	    httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.INTERNAL_SERVER_ERROR_500));
	    _logger.error("Exception", e);
	} finally {
	    httpResponse.toHttpResponse(response);
	    _logger.debug("HTTP response: {}", response);
	    _logger.debug("HTTP request handled by: {}", this.getClass().getName());
	}
    }

}
