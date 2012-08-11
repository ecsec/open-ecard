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

package org.openecard.client.connector.http;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.openecard.client.common.io.LimitedInputStream;
import org.openecard.client.connector.ConnectorHTTPException;
import org.openecard.client.connector.http.header.EntityHeader;
import org.openecard.client.connector.http.header.GeneralHeader;
import org.openecard.client.connector.http.header.RequestHeader;
import org.openecard.client.connector.http.header.RequestLine;


/**
 * Implements a HTTP request.
 * See RFC 2616, section 5 Request.
 * See http://tools.ietf.org/html/rfc2616#section-5
 *
 * Request = Request-Line
 * (( general-header
 * | request-header
 * | entity-header ) CRLF)
 * CRLF
 * [ message-body ]
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class HTTPRequest extends HTTPMessage {

    /**
     * Stores the request-headers of a HTTP message.
     */
    private List<RequestHeader> requestHeaders = new ArrayList<RequestHeader>();

    /**
     * Creates a new HTTP request.
     */
    public HTTPRequest() {
    }

    /**
     * Returns the request-line of the HTTP request.
     *
     * @return Request-lines
     */
    public RequestLine getRequestLine() {
	return (RequestLine) startLine;
    }

    /**
     * Returns the general-headers of the HTTP request.
     *
     * @return General-headers
     */
    public List<GeneralHeader> getGeneralHeaders() {
	return generalHeaders;
    }

    /**
     * Returns the request-headers of the HTTP request.
     *
     * @return Request-headers
     */
    public List<RequestHeader> getRequestHeaders() {
	return requestHeaders;
    }

    /**
     * Returns the entity-headers of the HTTP request.
     *
     * @return Entity-headers
     */
    public List<EntityHeader> getEntityHeaders() {
	return entityHeaders;
    }

    /**
     * Returns the message body of the HTTP request.
     *
     * @return Message body
     */
    public byte[] getMessageBody() {
	return messageBody;
    }


    /**
     * Creates a org.openecard.client.connector.http.HTTPRequest
     * from a org.apache.http.HttpRequest.
     *
     * @param httpRequest HttpRequest
     * @throws ConnectorHTTPException
     */
    public void fromHttpRequest(HttpRequest httpRequest) throws ConnectorHTTPException {
	try {
	    // Read request line
	    startLine = RequestLine.getInstance(httpRequest.getRequestLine().toString());

	    // Read headers
	    for (Header h : httpRequest.getAllHeaders()) {
		String line = h.toString();
		GeneralHeader generalHeader = GeneralHeader.getInstance(line);
		if (generalHeader != null) {
		    generalHeaders.add(generalHeader);
		}
		RequestHeader requestHeader = RequestHeader.getInstance(line);
		if (requestHeader != null) {
		    requestHeaders.add(requestHeader);
		}
		EntityHeader entityHeader = EntityHeader.getInstance(line);
		if (entityHeader != null) {
		    entityHeaders.add(entityHeader);
		}
	    }

	    // Read message body
	    if (httpRequest instanceof HttpEntityEnclosingRequest) {
		InputStream is = ((HttpEntityEnclosingRequest) httpRequest).getEntity().getContent();
		LimitedInputStream lis = new LimitedInputStream(is);
		InputStreamReader isr = new InputStreamReader(lis);
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();

		while (line != null || !line.isEmpty()) {
		    sb.append(line);
		    line = br.readLine();
		}
	    }

	} catch (Exception e) {
	    throw new ConnectorHTTPException(HTTPStatusCode.BAD_REQUEST_400, e.getMessage());
	}
    }

}
