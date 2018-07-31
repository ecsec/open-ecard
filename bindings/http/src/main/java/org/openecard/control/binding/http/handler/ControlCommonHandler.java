/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.openecard.control.binding.http.common.Http11Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch
 */
public abstract class ControlCommonHandler extends HttpControlHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ControlCommonHandler.class);

    /**
     * Creates a new new ControlCommonHandler.
     */
    protected ControlCommonHandler() {
	super("*");
    }

    /**
     * Creates a new new ControlCommonHandler.
     *
     * @param path Path
     */
    protected ControlCommonHandler(String path) {
	super(path);
    }

    /**
     * Handles a HTTP request.
     *
     * @param httpRequest HTTPRequest
     * @return HTTPResponse
     * @throws HttpException
     * @throws Exception
     */
    public abstract HttpResponse handle(HttpRequest httpRequest) throws HttpException, Exception;

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
	LOG.debug("HTTP request: {}", request.toString());
	HttpResponse httpResponse = null;

	try {
	    // Forward request parameters to response parameters
	    response.setParams(request.getParams());

	    httpResponse = handle(request);
	} catch (org.openecard.control.binding.http.HttpException e) {
	    httpResponse = new Http11Response(HttpStatus.SC_BAD_REQUEST);
	    httpResponse.setEntity(new StringEntity(e.getMessage(), "UTF-8"));

	    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
		httpResponse.setEntity(new StringEntity(e.getMessage(), "UTF-8"));
	    }

	    httpResponse.setStatusCode(e.getHTTPStatusCode());
	} catch (Exception e) {
	    httpResponse = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	    LOG.error(e.getMessage(), e);
	} finally {
	    Http11Response.copyHttpResponse(httpResponse, response);
	    LOG.debug("HTTP response: {}", response);
	    LOG.debug("HTTP request handled by: {}", this.getClass().getName());
	}
    }

}
