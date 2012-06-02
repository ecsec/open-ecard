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
     * @param statusCode Status-code
     */
    public StatusLine(String version, HTTPStatusCode statusCode) {
	this.version = version;
	this.statusCode = statusCode;
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
