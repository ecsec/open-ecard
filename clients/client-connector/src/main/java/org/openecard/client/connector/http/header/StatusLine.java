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

import org.openecard.client.connector.http.HTTPConstants;
import org.openecard.client.connector.http.HTTPStatusCode;


/**
 * Implements a Status-Line of a HTTP request.
 * Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
 *
 * See RFC 2616, section 6.1 Status-Line
 * See http://tools.ietf.org/html/rfc2616#section-6.1
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class StatusLine extends StartLine {

    private HTTPStatusCode statusCode;

    /**
     * Creates a new status-line.
     *
     * @param statusCode Status-code
     */
    public StatusLine(HTTPStatusCode statusCode) {
	this(HTTPConstants.VERSION, statusCode);
    }

    /**
     * Creates a new status-line.
     *
     * @param version Version
     * @param statusCode Status code
     */
    public StatusLine(String version, HTTPStatusCode statusCode) {
	this.version = version;
	this.statusCode = statusCode;
    }

    /**
     * Returns the status code.
     *
     * @return Status code
     */
    public HTTPStatusCode getStatusCode() {
	return statusCode;
    }

    @Override
    public String toString() {
	final StringBuilder sb = new StringBuilder();
	sb.append(version);
	sb.append(" ");
	sb.append(statusCode);

	return sb.toString();
    }

}
