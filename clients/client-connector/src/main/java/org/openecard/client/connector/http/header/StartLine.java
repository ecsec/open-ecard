package org.openecard.client.connector.http.header;

/**
 * Implements a Start-Line of a HTTP message.
 * Start-line = Request-Line | Status-Line
 *
 * See RFC 2616, section 4.1 Message Types
 * See http://tools.ietf.org/html/rfc2616#section-4.1
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class StartLine {

    /**
     * Stores the HTTP version.
     */
    protected String version;

    /**
     * Returns the version.
     *
     * @return Version
     */
    public String getVersion() {
	return version;
    }
}
