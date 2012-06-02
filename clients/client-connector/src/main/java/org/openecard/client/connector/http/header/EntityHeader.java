package org.openecard.client.connector.http.header;

/**
 * Implements a entity-header HTTP header field.
 * See RFC 2612, section 7.1 Entity Header Fields
 * See http://tools.ietf.org/html/rfc2616#section-7.1
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class EntityHeader extends MessageHeader {

    public enum Field {

	ALLOW("Allow"),
	CONTENT_ENCODING("Content-Encoding"),
	CONTENT_LANGUAGE("Content-Language"),
	CONTENT_LENGTH("Content-Length"),
	CONTENT_LOCATION("Content-Location"),
	CONTENT_MD5("Content-MD5"),
	CONTENT_RANGE("Content-Range"),
	CONTENT_TYPE("Content-Type"),
	EXPIRES("Expires"),
	LAST_MODIFIED("Last-Modified");
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
     * Creates a new entity-header.
     *
     * @param fieldName Field name
     * @param fieldValue Field value
     */
    public EntityHeader(Field fieldName, String fieldValue) {
	super(fieldName.getFieldName(), fieldValue);
    }

    private EntityHeader(String field, String value) {
	super(field, value);
    }

    /**
     * Creates a new entity-header.
     *
     * @param line Line
     * @return Entity-header
     */
    public static EntityHeader getInstance(String line) {
	final String[] elements = line.split(": ");

	if (elements.length != 2) {
	    throw new IllegalArgumentException();
	}

	for (EntityHeader.Field item : EntityHeader.Field.values()) {
	    if (item.getFieldName().equals(elements[0])) {
		return new EntityHeader(elements[0], elements[1]);
	    }
	}
	return null;
    }
}
