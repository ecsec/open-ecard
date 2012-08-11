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


/**
 * Implements a general-header HTTP header field.
 * See RFC 2612, section 4.5 General Header Fields
 * See http://tools.ietf.org/html/rfc2616#section-4.5
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public final class GeneralHeader extends MessageHeader {

    public enum Field {

	CACHE_CONTROL("Cache-Control"),
	CONNECTION("Connection"),
	DATE("Date"),
	PRAGMA("Pragma"),
	TRAILER("Trailer"),
	TRANSFER_ENCODING("Transfer-Encoding"),
	UPGRADE("Upgrade"),
	VIA("Via"),
	WARNING("Warning");
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
     * Creates a new general-header.
     *
     * @param fieldName Field name
     * @param fieldValue Field value
     */
    public GeneralHeader(Field fieldName, String fieldValue) {
	super(fieldName.getFieldName(), fieldValue);
    }

    private GeneralHeader(String fieldName, String fieldValue) {
	super(fieldName, fieldValue);
    }

    /**
     * Creates a new general-header.
     *
     * @param line Line
     * @return General-header
     */
    public static GeneralHeader getInstance(String line) {
	final String[] elements = line.split(": ");

	if (elements.length != 2) {
	    throw new IllegalArgumentException();
	}

	for (GeneralHeader.Field item : GeneralHeader.Field.values()) {
	    if (item.getFieldName().equals(elements[0])) {
		return new GeneralHeader(elements[0], elements[1]);
	    }
	}
	return null;
    }

}
