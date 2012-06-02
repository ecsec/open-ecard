package org.openecard.client.connector.http.header;

import java.net.URI;
import org.openecard.client.common.logging.LoggingConstants;
import org.openecard.client.connector.http.HTTPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implements a Request-Line of a HTTP request.
 * Request-Line = Method SP Request-URI SP HTTP-Version CRLF
 *
 * See RFC 2616, section 5.1 Request-Line
 * See http://tools.ietf.org/html/rfc2616#section-5.1
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class RequestLine extends StartLine {

    private static final Logger logger = LoggerFactory.getLogger(RequestLine.class);
    private String method;
    private URI requestURI;

    public enum Methode {

	OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT;
    }

    private RequestLine(String methode, URI requestURI) {
	this(methode, requestURI, HTTPConstants.VERSION);
    }

    private RequestLine(String methode, URI requestURI, String version) {
	this.method = methode;
	this.requestURI = requestURI;
	this.version = version;
    }

    /**
     * Creates a new request-line for a HTTP request.
     *
     * @param line Line
     * @return Request-line
     */
    public static RequestLine getInstance(String line) {
	final String[] elements = line.split(" ");

	if (elements.length != 3) {
	    throw new IllegalArgumentException();
	}

	try {
	    // Read methode
	    String elem1 = Methode.valueOf(elements[0]).name();
	    // Read request uri
	    URI elem2 = new URI(elements[1]);
	    // Read version
	    String elem3 = elements[2];
	    if (!elem3.equals(HTTPConstants.VERSION)) {
		throw new IllegalArgumentException();
	    }
	    return new RequestLine(elem1, elem2, elem3);
	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.info(LoggingConstants.THROWING, "Exception", e);
	    // </editor-fold>
	    throw new IllegalArgumentException();
	}
    }

    /**
     * Returns the method.
     *
     * @return Method
     */
    public String getMethod() {
	return method;
    }

    /**
     * Returns the request-URI.
     *
     * @return Request-URI
     */
    public URI getRequestURI() {
	return requestURI;
    }

    @Override
    public String toString() {
	final StringBuilder sb = new StringBuilder();
	sb.append(method);
	sb.append(" ");
	sb.append(requestURI.toString());
	sb.append(" ");
	sb.append(version);

	return sb.toString();
    }
}
