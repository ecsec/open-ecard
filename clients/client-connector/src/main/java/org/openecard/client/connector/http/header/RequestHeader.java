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

package org.openecard.client.connector.http.header;


/**
 * Implements a request-header HTTP header field.
 * See RFC 2612, section 5.3 Request Header Fields
 * See http://tools.ietf.org/html/rfc2616#section-5.3
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class RequestHeader extends MessageHeader {

    public enum Field {

	ACCEPT("Accept"),
	ACCEPT_CHARSET("Accept-Charset"),
	ACCEPT_ENCODING("Accept-Encoding"),
	ACCEPT_LANGUAGE("Accept-Language"),
	AUTHORIZATION("Authorization"),
	EXCEPT("Expect"),
	FROM("From"),
	HOST("Host"),
	IF_MATCH("If-Match"),
	IF_MODIFIED_SINCE("If-Modified-Since"),
	IF_NONE_MATCH("If-None-Match"),
	IF_RANGE("If-Range"),
	IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
	MAX_FORWARDS("Max-Forwards"),
	PROXY_AUTHENTICAION("Proxy-Authorization"),
	RANGE("Range"),
	REFERER("Referer"),
	TE("TE"),
	USER_AGENT("User-Agent"),
	// CORS headers. See http://www.w3.org/TR/cors/
	ORIGIN("Origin"),
	ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),
	ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers");
	//
	private String fieldName;

	private Field(String fieldName) {
	    this.fieldName = fieldName;
	}

	/**
	 * Returns the field name.
	 *
	 * @return Field name
	 */
	public String getFieldName() {
	    return fieldName;
	}

    }

    /**
     * Creates a new request-header.
     *
     * @param fieldName Field name
     * @param fieldValue Field value
     */
    public RequestHeader(Field fieldName, String fieldValue) {
	super(fieldName.getFieldName(), fieldValue);
    }

    private RequestHeader(String fieldName, String fieldValue) {
	super(fieldName, fieldValue);
    }

    /**
     * Creates a new request-header.
     *
     * @param line Line
     * @return Request-header
     */
    public static RequestHeader getInstance(String line) {
	final String[] elements = line.split(": ");

	if (elements.length != 2) {
	    throw new IllegalArgumentException();
	}

	for (RequestHeader.Field item : RequestHeader.Field.values()) {
	    if (item.getFieldName().equals(elements[0])) {
		return new RequestHeader(elements[0], elements[1]);
	    }
	}
	return null;
    }

}
