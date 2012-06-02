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
    OK_200(200, "200 OK"),
    CREATED_201(201, "201 Created"),
    //Redirection 3xx
    SEE_OTHER_303(303, "303 See Other"),
    // Client Error 4xx
    BAD_REQUEST_400(400, "400 Bad Request"),
    // Server Error 5xx
    INTERNAL_SERVER_ERROR_500(500, "500 Internal Server Error");
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
