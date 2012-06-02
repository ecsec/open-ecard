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
	USER_AGENT("User-Agent");
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
