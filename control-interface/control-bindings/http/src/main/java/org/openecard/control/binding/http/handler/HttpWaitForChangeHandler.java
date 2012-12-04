/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.control.binding.http.handler;

import java.io.IOException;
import java.net.URI;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.openecard.control.ControlException;
import org.openecard.control.binding.http.HTTPException;
import org.openecard.control.binding.http.common.Http11Response;
import org.openecard.control.module.status.GenericWaitForChangeHandler;
import org.openecard.control.module.status.StatusChangeRequest;
import org.openecard.ws.WSMarshaller;
import org.openecard.ws.WSMarshallerException;
import org.openecard.ws.WSMarshallerFactory;
import org.openecard.ws.schema.StatusChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a WaitForChangeHandler to get information about a status change of the client.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class HttpWaitForChangeHandler extends HttpControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpWaitForChangeHandler.class);
    private final WSMarshaller m;
    private final GenericWaitForChangeHandler genericWaitForChangeHandler;

    /**
     * Creates a new HttpWaitForChangeHandler.
     *
     * @param genericWaitForChangeHandler to handle the generic part of the WaitForChange request
     */
    public HttpWaitForChangeHandler(GenericWaitForChangeHandler genericWaitForChangeHandler) {
	super("/waitForChange");
	this.genericWaitForChangeHandler = genericWaitForChangeHandler;
	try {
	    m = WSMarshallerFactory.createInstance();
	    m.removeAllTypeClasses();
	    m.addXmlTypeClass(StatusChange.class);
	} catch (WSMarshallerException e) {
	    logger.error(e.getMessage(), e);
	    throw new RuntimeException(e);
	}
    }

    /**
     *
     * @param httpRequest the HTTPRequest for StatusChange
     * @return a StatusChangeRequest
     * @throws HTTPException if the request was bad
     */
    public StatusChangeRequest handleRequest(HttpRequest httpRequest) throws HTTPException {
	try {
	    RequestLine requestLine = httpRequest.getRequestLine();

	    if (requestLine.getMethod().equals("GET")) {
		URI requestURI = URI.create(requestLine.getUri());

		return genericWaitForChangeHandler.parseStatusChangeRequestURI(requestURI);
	    } else {
		throw new HTTPException(HttpStatus.SC_METHOD_NOT_ALLOWED);
	    }
	} catch (HTTPException e) {
	    throw e;
	} catch (Exception e) {
	    throw new HTTPException(HttpStatus.SC_BAD_REQUEST, e.getMessage());
	}
    }

    /**
     *
     * @param statusChange The statusChange to respond.
     * @return A HttpResponse containing the statusChange as XML.
     * @throws HTTPException If creating the HttpResponse fails.
     */
    public HttpResponse handleResponse(StatusChange statusChange) throws HTTPException {
	try {
	    HttpResponse httpResponse = new Http11Response(HttpStatus.SC_ACCEPTED);

	    String xml = m.doc2str(m.marshal(statusChange));

	    ContentType contentType = ContentType.create(ContentType.TEXT_XML.getMimeType(), "UTF-8");
	    StringEntity entity = new StringEntity(xml, contentType);
	    httpResponse.setEntity(entity);

	    return httpResponse;
	} catch (Exception e) {
	    throw new HTTPException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws IOException {
	logger.debug("HTTP request: {}", request.toString());
	HttpResponse httpResponse = null;
	try {
	    StatusChangeRequest statusRequest = this.handleRequest(request);
	    StatusChange status = genericWaitForChangeHandler.getStatusChange(statusRequest);
	    if (status == null) {
		String msg = "There is no event queue for the specified session identifier existing.";
		httpResponse = new Http11Response(HttpStatus.SC_BAD_REQUEST, msg );
	    } else {
		httpResponse = this.handleResponse(status);
	    }
	    response.setParams(request.getParams());
	} catch (ControlException e) {
	    httpResponse = new Http11Response(HttpStatus.SC_BAD_REQUEST);

	    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
		httpResponse.setEntity(new StringEntity(e.getMessage(), "UTF-8"));
	    }

	    if (e instanceof HTTPException) {
		httpResponse.setStatusCode(((HTTPException) e).getHTTPStatusCode());
	    }
	} catch (Exception e) {
	    httpResponse = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	    logger.error(e.getMessage(), e);
	} finally {
	    Http11Response.copyHttpResponse(httpResponse, response);
	    logger.debug("HTTP response: {}", response);
	    logger.debug("HTTP request handled by: {}", this.getClass().getName());
	}
    }

}
