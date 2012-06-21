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
	ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
	ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods");
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
