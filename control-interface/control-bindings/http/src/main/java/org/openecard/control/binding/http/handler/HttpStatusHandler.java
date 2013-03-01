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

package org.openecard.control.binding.http.handler;

import java.io.IOException;
import java.net.URI;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpStatus;
import org.openecard.apache.http.RequestLine;
import org.openecard.apache.http.entity.ContentType;
import org.openecard.apache.http.entity.StringEntity;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.control.ControlException;
import org.openecard.control.binding.http.HTTPException;
import org.openecard.control.binding.http.common.Http11Response;
import org.openecard.control.module.status.GenericStatusHandler;
import org.openecard.control.module.status.StatusRequest;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.schema.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a StatusHandler to get information about the functionality of the client.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class HttpStatusHandler extends HttpControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpStatusHandler.class);
    private final WSMarshaller m;
    private final GenericStatusHandler genericStatusHandler;

    /**
     * Creates a new StatusHandler.
     *
     * @param genericStatusHandler to handle the generic part of the Status request
     */
    public HttpStatusHandler(GenericStatusHandler genericStatusHandler) {
	super("/getStatus");
	this.genericStatusHandler = genericStatusHandler;
	try {
	    m = WSMarshallerFactory.createInstance();
	    m.removeAllTypeClasses();
	    m.addXmlTypeClass(Status.class);
	} catch (WSMarshallerException e) {
	    logger.error(e.getMessage(), e);
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws IOException {
	logger.debug("HTTP request: {}", request.toString());
	HttpResponse httpResponse = null;
	try {
	    StatusRequest statusRequest = this.handleRequest(request);
	    Status status = genericStatusHandler.handleRequest(statusRequest);
	    httpResponse = this.handleResponse(status);
	    response.setParams(request.getParams());
	    Http11Response.copyHttpResponse(httpResponse, response);
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

    /**
     *
     * @param httpRequest the HTTPRequest for Status
     * @return a StatusRequest
     * @throws HTTPException if the request was bad
     */
    private StatusRequest handleRequest(HttpRequest httpRequest) throws HTTPException {
	try {
	    RequestLine requestLine = httpRequest.getRequestLine();

	    if (requestLine.getMethod().equals("GET")) {
		URI requestURI = URI.create(requestLine.getUri());

		return genericStatusHandler.parseStatusRequestURI(requestURI);
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
     * @param status the status to respond
     * @return a HttpResponse containing the status as xml
     * @throws HTTPException if creating the HttpResponse fails
     */
    private HttpResponse handleResponse(Status status) throws HTTPException {
	try {
	    HttpResponse httpResponse = new Http11Response(HttpStatus.SC_ACCEPTED);

	    String xml = m.doc2str(m.marshal(status));

	    ContentType contentType = ContentType.create(ContentType.TEXT_XML.getMimeType(), "UTF-8");
	    StringEntity entity = new StringEntity(xml, contentType);
	    httpResponse.setEntity(entity);

	    return httpResponse;
	} catch (Exception e) {
	    throw new HTTPException(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getMessage());
	}
    }

}
