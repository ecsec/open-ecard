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

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import org.openecard.client.connector.common.ConnectorConstants;
import org.openecard.client.connector.common.ErrorPage;
import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.http.HTTPStatusCode;
import org.openecard.client.connector.http.header.EntityHeader;
import org.openecard.client.connector.http.header.RequestLine;
import org.openecard.client.connector.http.header.ResponseHeader;
import org.openecard.client.connector.http.header.StatusLine;
import org.openecard.client.connector.messages.TCTokenRequest;
import org.openecard.client.connector.messages.TCTokenResponse;
import org.openecard.client.connector.messages.common.ClientRequest;
import org.openecard.client.connector.messages.common.ClientResponse;
import org.openecard.client.connector.tctoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <moritz.horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenHandler implements ConnectorHandler {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenHandler.class);

    private boolean corsRequest;

    /**
     * Create a new ActivationRequest.
     */
    public TCTokenHandler() {
	this.corsRequest = false;
    }

    @Override
    public ClientRequest handleRequest(HTTPRequest httpRequest) throws Exception {
	RequestLine requestLine = httpRequest.getRequestLine();

	if (requestLine.getMethod().equals(RequestLine.Methode.GET.name())) {
	    URI requestURI = requestLine.getRequestURI();

	    if (requestURI.getPath().equals("/eID-Client")) {
		TCTokenRequest tcTokenRequest = new TCTokenRequest();
		String query[] = requestURI.getQuery().split("&");

		for (String q : query) {
		    String name = q.substring(0, q.indexOf("="));
		    String value = q.substring(q.indexOf("=") + 1, q.length());

		    if (name.startsWith("tcTokenURL")) {
			if (!value.isEmpty()) {
			    value = URLDecoder.decode(value,"UTF-8");
			    TCToken token = parseTCToken(new URL(value));
			    tcTokenRequest.setTCToken(token);
			} else {
			    throw new IllegalArgumentException("Malformed tcTokenURL");
			}

		    } else if (name.startsWith("ifdName")) {
			if (!value.isEmpty()) {
			    value = URLDecoder.decode(value,"UTF-8");
			    tcTokenRequest.setIFDName(value);
			} else {
			    throw new IllegalArgumentException("Malformed ifd name");
			}

		    } else if (name.startsWith("contextHandle")) {
			if (!value.isEmpty()) {
			    tcTokenRequest.setContextHandle(value);
			} else {
			    throw new IllegalArgumentException("Malformed context handle");
			}

		    } else if (name.startsWith("slotIndex")) {
			if (!value.isEmpty()) {
			    tcTokenRequest.setSlotIndex(value);
			} else {
			    throw new IllegalArgumentException("Malformed slot index");
			}

		    } else if (name.startsWith("corsRequest")) {
			if (!value.isEmpty()) {
			    this.corsRequest = true;
			}
		    } else {
			logger.info("Unknown query element: {}", name);
		    }
		}

		return tcTokenRequest;
	    }
	}

	return null;
    }

    @Override
    public HTTPResponse handleResponse(ClientResponse clientResponse) throws Exception {
	if (clientResponse instanceof TCTokenResponse) {
	    TCTokenResponse response = (TCTokenResponse) clientResponse;

	    if (response.getErrorPage() != null) {
		return handleErrorPage(response.getErrorPage());
	    } else if (response.getErrorMessage() != null) {
		return handleErrorResponse(response.getErrorMessage());
	    } else if (response.getRefreshAddress() != null) {
		if(this.corsRequest) {
		    return handleCORSRedirectResponse(response.getRefreshAddress());
		} else {
		    return handleRedirectResponse(response.getRefreshAddress());
		}
	    } else {
		return handleErrorResponse(ConnectorConstants.ConnectorError.INTERNAL_ERROR.toString());
	    }
	}
	return null;
    }

    /**
     * Parses the TCToken.
     *
     * @throws TCTokenException
     */
    private TCToken parseTCToken(URL tokenURI) throws TCTokenException {
	// Get TCToken from the given url
	TCTokenGrabber grabber = new TCTokenGrabber();
	String data = grabber.getResource(tokenURI.toString());

	//FIXME Remove me
	/*
	  TCTokenConverter converter = new TCTokenConverter();
	  data = converter.convert(data);
	*/

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException(ConnectorConstants.ConnectorError.TC_TOKEN_NOT_AVAILABLE.toString());
	}

	// Verify the TCToken
	TCTokenVerifier ver = new TCTokenVerifier(tokens);
	ver.verify();

	return tokens.get(0);
    }

    /**
     * Handle a redirect response using CORS and the redirect URL in the response message body.
     * Used for Ajax requests.
     *
     * @param location Redirect location
     * @return HTTP response
     */
    public HTTPResponse handleCORSRedirectResponse(URL location) {
	HTTPResponse httpResponse = new HTTPResponse();
	httpResponse.addResponseHeaders(new ResponseHeader(ResponseHeader.Field.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
	httpResponse.addResponseHeaders(new ResponseHeader(ResponseHeader.Field.ACCESS_CONTROL_ALLOW_METHODS, "GET"));
	httpResponse.addEntityHeaders(new EntityHeader(EntityHeader.Field.CONTENT_TYPE, "text/plain"));
	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.OK_200));
	httpResponse.setMessageBody(location.toString());

	return httpResponse;
    }

    /**
     * Handle a redirect response.
     *
     * @param location Redirect location
     * @return HTTP response
     */
    public HTTPResponse handleRedirectResponse(URL location) {
	HTTPResponse httpResponse = new HTTPResponse();
	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.SEE_OTHER_303));
	httpResponse.addResponseHeaders(new ResponseHeader(ResponseHeader.Field.LOCATION, location.toString()));

	return httpResponse;
    }

    /**
     * Handle a error response.
     * The message will be placed in a HTML page.
     *
     * @param message Message
     * @return HTTP response
     */
    public HTTPResponse handleErrorResponse(String message) {
	ErrorPage p = new ErrorPage(message);
	String content = p.getHTML();

	HTTPResponse httpResponse = new HTTPResponse();
	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.OK_200));
	httpResponse.setMessageBody(content);

	return httpResponse;
    }

    /**
     * Handle a error HTML page.
     *
     * @param content Content
     * @return HTTP response
     */
    public HTTPResponse handleErrorPage(String content) {

	HTTPResponse httpResponse = new HTTPResponse();
	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.OK_200));
	httpResponse.setMessageBody(content);

	return httpResponse;
    }

}
