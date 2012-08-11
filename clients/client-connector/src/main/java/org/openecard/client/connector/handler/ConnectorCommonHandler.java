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
public abstract class ConnectorCommonHandler extends ConnectorHandler {

    private static final Logger _logger = LoggerFactory.getLogger(ConnectorCommonHandler.class);


    /**
     * Creates a new new ConnectorCommonHandler.
     */
    protected ConnectorCommonHandler() {
	super("*");
    }

    /**
     * Creates a new new ConnectorCommonHandler.
     *
     * @param path Path
     */
    protected ConnectorCommonHandler(String path) {
	super(path);
    }

    /**
     * Handles a HTTP request.
     *
     * @param httpRequest HTTPRequest
     * @return HTTPResponse
     * @throws Exception
     */
    public abstract HTTPResponse handle(HTTPRequest httpRequest) throws Exception;

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

	    // Forward HttpContext attributes to response parameters
	    BasicHttpParams params = new BasicHttpParams();
	    params.setParameter(
		    CORSResponseInterceptor.class.getName(),
		    context.getAttribute(CORSRequestInterceptor.class.getName()));
	    response.setParams(params);

	    httpResponse = handle(httpRequest);
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
