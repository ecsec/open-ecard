/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import javax.annotation.Nonnull;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpStatus;
import org.openecard.apache.http.RequestLine;
import org.openecard.apache.http.entity.StringEntity;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.common.ECardConstants;
import org.openecard.control.ControlException;
import org.openecard.control.binding.http.HTTPException;
import org.openecard.control.binding.http.common.HeaderTypes;
import org.openecard.control.binding.http.common.Http11Response;
import org.openecard.control.module.tctoken.GenericTCTokenHandler;
import org.openecard.control.module.tctoken.TCTokenRequest;
import org.openecard.control.module.tctoken.TCTokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class HttpTCTokenHandler extends HttpControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpTCTokenHandler.class);

    private final GenericTCTokenHandler genericTCTokenHandler;

    /**
     * Create a new HttpTCTokenHandler.
     *
     * @param genericTCTokenHandler to handle the generic part of the TCToken request
     */
    public HttpTCTokenHandler(GenericTCTokenHandler genericTCTokenHandler) {
	super("/eID-Client");
	this.genericTCTokenHandler = genericTCTokenHandler;
    }

    protected HttpTCTokenHandler(@Nonnull String path, @Nonnull GenericTCTokenHandler genericTCTokenHandler) {
	super(path);
	this.genericTCTokenHandler = genericTCTokenHandler;
    }

    /**
     *
     * @param response TC Token response after handling the activation
     * @return HTTP Response with a redirect to the determined refreshAddress or with status 400 or 500 in error
     *    situations
     * @throws Exception
     */
    private HttpResponse handleResponse(TCTokenResponse response) throws Exception {
	HttpResponse httpResponse = new Http11Response(HttpStatus.SC_BAD_REQUEST);

	Result result = response.getResult();

	if (result.getResultMajor().equals(ECardConstants.Major.OK)) {
	    if (response.getRefreshAddress() != null) {
		return handleRedirectResponse(response.getRefreshAddress());
	    } else {
		httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	    }
	} else {
	    if (result.getResultMessage().getValue() != null) {
		return handleErrorResponse(result.getResultMessage().getValue());
	    } else {
		httpResponse.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	    }
	}

	return httpResponse;
    }

    /**
     * Handle a redirect response.
     *
     * @param location Redirect location
     * @return HTTP response
     */
    private HttpResponse handleRedirectResponse(URL location) {
	HttpResponse httpResponse = new Http11Response(HttpStatus.SC_SEE_OTHER);
	httpResponse.setHeader(HeaderTypes.LOCATION.fieldName(), location.toString());

	return httpResponse;
    }

    /**
     * Handle a error response.
     *
     * @param message an error message that serves as string entity for the HttpResponse
     * @return a HttpResponse with HttpStatus.SC_BAD_REQUEST containing the error message
     * @throws UnsupportedEncodingException if the charset of the message is not supported
     */
    private HttpResponse handleErrorResponse(String message) throws UnsupportedEncodingException {
	Http11Response httpResponse = new Http11Response(HttpStatus.SC_BAD_REQUEST);
	// TODO: set correct mime type. Is it text/html?
	httpResponse.setEntity(new StringEntity(message, "UTF-8"));

	return httpResponse;
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext context) throws IOException {
	logger.debug("HTTP request: {}", httpRequest.toString());
	HttpResponse response = null;
	try {
	    RequestLine requestLine = httpRequest.getRequestLine();
	    URI requestURI = URI.create(requestLine.getUri());
	    if (!requestLine.getMethod().equals("GET")) {
		throw new HTTPException(HttpStatus.SC_METHOD_NOT_ALLOWED);
	    }
	    TCTokenRequest tcTokenRequest = genericTCTokenHandler.parseRequestURI(requestURI);
	    TCTokenResponse tcTokenResponse = genericTCTokenHandler.handleActivate(tcTokenRequest);
	    addResultParams(tcTokenResponse);

	    response = this.handleResponse(tcTokenResponse);
	    response.setParams(httpRequest.getParams());
	    Http11Response.copyHttpResponse(response, httpResponse);
	} catch (ControlException e) {
	    response = new Http11Response(HttpStatus.SC_BAD_REQUEST);

	    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
		response.setEntity(new StringEntity(e.getMessage(), "UTF-8"));
	    }

	    if (e instanceof HTTPException) {
		response.setStatusCode(((HTTPException) e).getHTTPStatusCode());
	    }
	} catch (Exception e) {
	    response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	    logger.error(e.getMessage(), e);
	} finally {
	    Http11Response.copyHttpResponse(response, httpResponse);
	    logger.debug("HTTP response: {}", response);
	    logger.debug("HTTP request handled by: {}", this.getClass().getName());
	}
    }

    private void addResultParams(TCTokenResponse response) {
	URL url = response.getRefreshAddress();
	Result r = response.getResult();
	try {
	    if (url != null && r != null) {
		if (ECardConstants.Major.OK.equals(r.getResultMajor())) {
		    url = addQueryParam(url, "ResultMajor", "ok");
		} else {
		    url = addQueryParam(url, "ResultMajor", "error");
		    String minor = getErrorString(r.getResultMinor());
		    url = addQueryParam(url, "ResultMinor", minor);
		}
	    }
	} catch (MalformedURLException ex) {
	    logger.warn("Produced RefreshAddress is not a valid URL, omitting error parameters.", ex);
	} catch (UnsupportedEncodingException ex) {
	    logger.error("Unkown encoding used.", ex);
	}
	// write back the result
	response.setRefreshAddress(url);
    }

    private URL addQueryParam(URL base, String key, String value) throws MalformedURLException {
	String url = base.toString();
	if (base.getFile().isEmpty() && ! url.endsWith("/")) {
	    url += "/";
	}
	if (! url.contains("?")) {
	    url += "?";
	}
	if (url.endsWith("?")) {
	    return new URL(url + key + "=" + value);
	} else {
	    return new URL(url + "&" + key + "=" + value);
	}
    }

    private String getErrorString(String errorUri) throws UnsupportedEncodingException {
	int idx = errorUri.lastIndexOf('#');
	if (idx == -1) {
	    idx = errorUri.lastIndexOf('/');
	}
	if (idx == -1 || errorUri.length() == idx + 1) {
	    return "";
	} else {
	    String sub = errorUri.substring(idx + 1);
	    return URLEncoder.encode(sub, "UTF-8");
	}
    }

}
