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

package org.openecard.client.connector.handler.tctoken;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.entity.StringEntity;
import org.openecard.client.common.ECardConstants;
import org.openecard.client.connector.ConnectorConstants;
import org.openecard.client.connector.ConnectorException;
import org.openecard.client.connector.ConnectorHTTPException;
import org.openecard.client.connector.client.ClientRequest;
import org.openecard.client.connector.client.ClientResponse;
import org.openecard.client.connector.client.ConnectorListeners;
import org.openecard.client.connector.handler.ConnectorClientHandler;
import org.openecard.client.connector.http.HeaderTypes;
import org.openecard.client.connector.http.Http11Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class TCTokenHandler extends ConnectorClientHandler {

    private static final Logger _logger = LoggerFactory.getLogger(TCTokenHandler.class);

    /**
     * Create a new TCTokenHandler.
     *
     * @param listeners ConnectorListeners
     */
    public TCTokenHandler(ConnectorListeners listeners) {
	super("/eID-Client", listeners);
    }

    @Override
    public ClientRequest handleRequest(HttpRequest httpRequest) throws ConnectorException, Exception {
	try {
	    RequestLine requestLine = httpRequest.getRequestLine();

	    if (requestLine.getMethod().equals("GET")) {
		URI requestURI = URI.create(requestLine.getUri());

		TCTokenRequest tcTokenRequest = new TCTokenRequest();
		String query[] = requestURI.getQuery().split("&");

		for (String q : query) {
		    String name = q.substring(0, q.indexOf("="));
		    String value = q.substring(q.indexOf("=") + 1, q.length());

		    if (name.startsWith("tcTokenURL")) {
			if (!value.isEmpty()) {
			    value = URLDecoder.decode(value, "UTF-8");
			    TCToken token = parseTCToken(new URL(value));
			    tcTokenRequest.setTCToken(token);
			} else {
			    throw new IllegalArgumentException("Malformed TCTokenURL");
			}

		    } else if (name.startsWith("ifdName")) {
			if (!value.isEmpty()) {
			    value = URLDecoder.decode(value, "UTF-8");
			    tcTokenRequest.setIFDName(value);
			} else {
			    throw new IllegalArgumentException("Malformed IFDName");
			}

		    } else if (name.startsWith("contextHandle")) {
			if (!value.isEmpty()) {
			    tcTokenRequest.setContextHandle(value);
			} else {
			    throw new IllegalArgumentException("Malformed ContextHandle");
			}

		    } else if (name.startsWith("slotIndex")) {
			if (!value.isEmpty()) {
			    tcTokenRequest.setSlotIndex(value);
			} else {
			    throw new IllegalArgumentException("Malformed SlotIndex");
			}
		    } else {
			_logger.debug("Unknown query element: {}", name);
		    }
		}

		return tcTokenRequest;
	    } else {
		throw new ConnectorHTTPException(HttpStatus.SC_METHOD_NOT_ALLOWED);
	    }
	} catch (ConnectorHTTPException e) {
	    throw e;
	} catch (Exception e) {
	    throw new ConnectorHTTPException(HttpStatus.SC_BAD_REQUEST, e.getMessage());
	}
    }

    @Override
    public HttpResponse handleResponse(ClientResponse clientResponse) throws ConnectorException, Exception {
	if (clientResponse instanceof TCTokenResponse) {
	    TCTokenResponse response = (TCTokenResponse) clientResponse;
	    Result result = response.getResult();

	    if (result.getResultMajor().equals(ECardConstants.Major.OK)) {
		if (response.getRefreshAddress() != null) {
		    return handleRedirectResponse(response.getRefreshAddress());
		} else {
		    return new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
	    } else {
		if (result.getResultMessage().getValue() != null) {
		    return handleErrorResponse(result.getResultMessage().getValue());
		} else {
		    return new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
	    }
	}
	return new Http11Response(HttpStatus.SC_BAD_REQUEST);
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
	 * TCTokenConverter converter = new TCTokenConverter();
	 * data = converter.convert(data);
	 */

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();
	List<TCToken> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException(ConnectorConstants.ConnectorError.TC_TOKEN_NOT_AVAILABLE.toString());
	}

	// Verify the TCToken
	TCTokenVerifier ver = new TCTokenVerifier(tokens.get(0));
	ver.verify();

	return tokens.get(0);
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
     * @param message Message
     * @return HTTP response
     */
    private HttpResponse handleErrorResponse(String message) throws UnsupportedEncodingException {
	Http11Response httpResponse = new Http11Response(HttpStatus.SC_BAD_REQUEST);
	// TODO: set correct mime type. Is it text/html?
	httpResponse.setEntity(new StringEntity(message, "UTF-8"));

	return httpResponse;
    }

}
