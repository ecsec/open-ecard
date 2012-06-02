package org.openecard.client.connector.http.header;

/**
 * Implements a HTTP message header.
 * See RFC 2616, section 4.2 Message Headers.
 * See http://tools.ietf.org/html/rfc2616#section-4.2
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class MessageHeader {

    /**
     * The name of the header field.
     */
    protected String fieldName;
    /**
     * The value of the header field.
     */
    protected String fieldValue;

    /**
     * Creates a new message header.
     *
     * @param fieldName Name of the header field
     * @param fieldValue Value of the header field
     */
    protected MessageHeader(String fieldName, String fieldValue) {
	this.fieldName = fieldName;
	this.fieldValue = fieldValue;
    }

    /**
     * Returns the name of the header field.
     *
     * @return Name of the field
     */
    public String getFieldName() {
	return fieldName;
    }

    /**
     * Returns the value of the header field.
     *
     * @return Value of the field
     */
    public String getFieldValue() {
	return fieldValue;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	sb.append(fieldName);
	sb.append(": ");
	sb.append(fieldValue);

	return sb.toString();
    }
}
