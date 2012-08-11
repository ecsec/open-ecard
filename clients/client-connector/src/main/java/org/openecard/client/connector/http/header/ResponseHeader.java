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
 * Implements a response-header HTTP header field.
 * See RFC 2612, section 6.2 Response Header Fields
 * See http://tools.ietf.org/html/rfc2616#section-6.2
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class ResponseHeader extends MessageHeader {

    public enum Field {

	ACCEPT_RANGES("Accept-Ranges"),
	AGE("Age"),
	ETAG("ETag"),
	LOCATION("Location"),
	PROXY_AUTHENTICATE("Proxy-Authenticate"),
	RETRY_AFTER("Retry-After"),
	SERVER("Server"),
	VARY("Vary"),
	WWW_AUTHENTICATE("WWW-Authenticate"),
	// CORS headers. See http://www.w3.org/TR/cors/
	ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
	ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),
	ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),
	ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
	ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
	ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers");
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
     * Creates a new response-header.
     *
     * @param fieldName Field name
     * @param fieldValue Field value
     */
    public ResponseHeader(Field fieldName, String fieldValue) {
	super(fieldName.getFieldName(), fieldValue);
    }

    private ResponseHeader(String fieldName, String fieldValue) {
	super(fieldName, fieldValue);
    }

    /**
     * Creates a new response-header.
     *
     * @param line Line
     * @return Response-header
     */
    public static ResponseHeader getInstance(String line) {
	final String[] elements = line.split(": ");

	if (elements.length != 2) {
	    throw new IllegalArgumentException();
	}

	for (ResponseHeader.Field item : ResponseHeader.Field.values()) {
	    if (item.getFieldName().equals(elements[0])) {
		return new ResponseHeader(elements[0], elements[1]);
	    }
	}
	return null;
    }

}
