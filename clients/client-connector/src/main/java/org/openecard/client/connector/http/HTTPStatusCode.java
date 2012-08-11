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


/**
 * Implements a HTTP status code.
 * See RFC 2616, section 10 Status Code Definitions
 * See http://tools.ietf.org/html/rfc2616#section-10
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public enum HTTPStatusCode {

    // Successful 2xx
    OK_200(200, "OK"),
    CREATED_201(201, "Created"),
    //Redirection 3xx
    SEE_OTHER_303(303, "See Other"),
    // Client Error 4xx
    BAD_REQUEST_400(400, "Bad Request"),
    UNAUTHORIZED_401(401, "Unauthorized"),
    PAYMENT_REQUIRED_402(402, "Payment Required"),
    FORBIDDEN_403(403, "Forbidden"),
    NOT_FOUND_404(404, "Not Found"),
    METHOD_NOT_ALLOWED_405(405, "Method Not Allowed"),
    // Server Error 5xx
    INTERNAL_SERVER_ERROR_500(500, "Internal Server Error");
    //
    private int statusCode;
    private String reasonPhrase;

    private HTTPStatusCode(int statusCode, String reasonPhrase) {
	this.statusCode = statusCode;
	this.reasonPhrase = reasonPhrase;
    }

    /**
     * Returns the status code.
     *
     * @return Status code
     */
    public int getStatusCode() {
	return statusCode;
    }

    /**
     * Returns the reason phrase of the HTTP status code.
     *
     * @return Reason phrase
     */
    public String getReasonPhrase() {
	return reasonPhrase;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(statusCode);
	sb.append(" ");
	sb.append(reasonPhrase);

	return sb.toString();
    }

}
