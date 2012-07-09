package org.openecard.client.connector.http;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.openecard.client.connector.http.header.EntityHeader;
import org.openecard.client.connector.http.header.GeneralHeader;
import org.openecard.client.connector.http.header.RequestHeader;
import org.openecard.client.connector.http.header.RequestLine;
import org.openecard.client.connector.http.io.HTTPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    private static final Logger logger = LoggerFactory.getLogger(HTTPRequest.class);
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
     * Sets the input stream of the HTTP request.
     * The HTTP request will be read and parsed.
     *
     * @param inputStream Input stream
     * @throws Exception If the HTTP request is malformed
     */
    public void setInputStream(InputStream inputStream) throws Exception {
	HTTPInputStream input = new HTTPInputStream(inputStream);

	try {
	    // Read request line
	    startLine = RequestLine.getInstance(input.readLine());

	    String line = input.readLine();
	    while (line != null && !line.isEmpty()) {
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

		line = input.readLine();
	    }

	    line = input.readLine();
	    StringBuilder sb = new StringBuilder();
	    while (line != null && !line.isEmpty()) {
		sb.append(line);
		line = input.readLine();
	    }
	    messageBody = sb.toString();

	} catch (Exception e) {
	    // <editor-fold defaultstate="collapsed" desc="log exception">
	    logger.error("Exception", e);
	    // </editor-fold>
	    throw new IllegalArgumentException();
	}
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
    public String getMessageBody() {
	return messageBody;
    }
}
