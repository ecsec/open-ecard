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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.openecard.client.connector.http.header.EntityHeader;
import org.openecard.client.connector.http.header.GeneralHeader;
import org.openecard.client.connector.http.header.ResponseHeader;
import org.openecard.client.connector.http.header.StatusLine;
import org.openecard.client.connector.http.io.HTTPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a HTTP response.
 * See RFC 2616, section 6 Response.
 * See http://tools.ietf.org/html/rfc2616#section-6
 *
 * Response = Status-Line
 * (( general-header
 * | response-header
 * | entity-header ) CRLF)
 * CRLF
 * [ message-body ]
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class HTTPResponse extends HTTPMessage {

    private static final Logger logger = LoggerFactory.getLogger(HTTPResponse.class);
    /** Stores the response-headers of a HTTP message. */
    private List<ResponseHeader> responseHeaders = new ArrayList<ResponseHeader>();

    /**
     * Creates a new HTTP response.
     */
    public HTTPResponse() {
	// all HTTPResponses support CORS
	addResponseHeaders(new ResponseHeader(ResponseHeader.Field.ACCESS_CONTROL_ALLOW_ORIGIN, "*"));
	addResponseHeaders(new ResponseHeader(ResponseHeader.Field.ACCESS_CONTROL_ALLOW_METHODS, "GET"));
    }

    /**
     * Sets the output stream to write the HTTP response to the socket.
     *
     * @param outputStream Output stream
     * @throws Exception
     */
    public void setOutputStream(OutputStream outputStream) throws Exception {
	try {
	    HTTPOutputStream output = new HTTPOutputStream(outputStream);

	    output.writeln(startLine.toString());
	    for (GeneralHeader generalHeader : generalHeaders) {
		output.writeln(generalHeader.toString());
	    }
	    for (ResponseHeader responseHeader : responseHeaders) {
		output.writeln(responseHeader.toString());
	    }
	    for (EntityHeader entityHeader : entityHeaders) {
		output.writeln(entityHeader.toString());
	    }

	    if (messageBody != null && !messageBody.isEmpty()) {
		byte[] messageBytes = messageBody.getBytes(HTTPConstants.CHATSET);
		String length = String.valueOf(messageBytes.length);

		output.writeln(new EntityHeader(EntityHeader.Field.CONTENT_LENGTH, length).toString());
		output.writeln();
		output.write(messageBytes);
	    } else {
		output.writeln();
	    }

	    output.close();
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error("Exception", e);
	    // </editor-fold>
	    outputStream.write(new StatusLine(HTTPStatusCode.INTERNAL_SERVER_ERROR_500).toString().getBytes());
	    outputStream.write(HTTPConstants.CRLF);
	    outputStream.write(HTTPConstants.CRLF);
	} finally {
	    outputStream.close();
	}
    }

    /**
     * Sets the status-line of the HTTP response.
     *
     * @param statusLine Status line
     */
    public void setStatusLine(StatusLine statusLine) {
	this.startLine = statusLine;
    }

    /**
     * Adds a general-header to the HTTP response.
     *
     * @param generalHeader General-header
     */
    public void addGeneralHeader(GeneralHeader generalHeader) {
	this.generalHeaders.add(generalHeader);
    }

    /**
     * Sets the general-headers to the HTTP response.
     *
     * @param generalHeaders General-headers
     */
    public void setGeneralHeaders(List<GeneralHeader> generalHeaders) {
	this.generalHeaders = generalHeaders;
    }

    /**
     * Adds a response-header to the HTTP response.
     *
     * @param responseHeader Response-header
     */
    public void addResponseHeaders(ResponseHeader responseHeader) {
	this.responseHeaders.add(responseHeader);
    }

    /**
     * Sets the response-headers of the HTTP response.
     *
     * @param responseHeaders Response-headers
     */
    public void setResponseHeaders(List<ResponseHeader> responseHeaders) {
	this.responseHeaders = responseHeaders;
    }

    /**
     * Adds a entity-header to the HTTP response.
     *
     * @param entityHeader Entity-header
     */
    public void addEntityHeaders(EntityHeader entityHeader) {
	this.entityHeaders.add(entityHeader);
    }

    /**
     * Sets the entity-headers of the HTTP response.
     *
     * @param entityHeaders Entity-headers
     */
    public void setEntityHeaders(List<EntityHeader> entityHeaders) {
	this.entityHeaders = entityHeaders;
    }

    /**
     * Sets the message body of the HTTP response.
     *
     * @param messageBody Message body
     */
    public void setMessageBody(String messageBody) {
	this.messageBody = messageBody;
    }

}
