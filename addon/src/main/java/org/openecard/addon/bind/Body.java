/****************************************************************************
 * Copyright (C) 2013-2019 ecsec GmbH.
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

package org.openecard.addon.bind;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base for different Body types.
 * A body instance contains a value and a MIME type. Request or response specific values are added by the respective
 * subclasses.
 *
 * @author Tobias Wich
 * @author Dirk Petrautzki
 */
public abstract class Body {

    private static final Logger LOG = LoggerFactory.getLogger(Body.class);

    private byte[] value;
    private Charset encoding;
    private String mimeType;

    protected Body() {
	this((byte[]) null, null, null);
    }

    protected Body(String value, Charset encoding, String mimeType) {
	this(value.getBytes(encoding), encoding, mimeType);
    }

    /**
     * Creates a body with the given content.
     *
     * @param value Value to use as the bodies content, or {@code null} if the body should be empty.
     * @param encoding Encoding of the value if applicable.
     * @param mimeType MIME type of the value or, {@code null} if the value is {@code null}.
     */
    protected Body(@Nullable byte[] value, @Nullable Charset encoding, @Nullable String mimeType) {
	this.value = value;
	this.encoding = encoding;
	this.mimeType = mimeType;
    }

    /**
     * Checks whether this instance contains a body value or not.
     *
     * @return {@code true} if the instance contains a value, {@code false} otherwise.
     */
    public boolean hasValue() {
	return value != null;
    }

    public boolean hasStringValue() {
	return value != null && encoding != null;
    }

    /**
     * Gets the value of this body instance.
     *
     * @return The value, or {@code null} if no value is set.
     */
    @Nullable
    public byte[] getValue() {
	return value;
    }

    /**
     * Gets the value of the nbody as a string.
     * This method only returns a value if this body is representable by a string.
     *
     * @return The value of the string if it is set, {@code null} otherwise.
     */
    @Nullable
    public String getValueString() {
	if (hasStringValue()) {
	    return new String(value, encoding);
	} else {
	    return null;
	}
    }

    /**
     * Gets the encoding of the
     * @return
     */
    @Nullable
    public Charset getEncoding() {
	return encoding;
    }

    /**
     * Gets the MIME type of this body's value.
     *
     * @return The MIME type, or {@code null} if no value and thus no MIME type is set.
     */
    @Nullable
    public String getMimeType() {
	return mimeType;
    }

    /**
     * Sets the value of the body.
     *
     * @param value The value to be set in the body. {@code null} values are permitted.
     * @param encoding The encoding of the value, or {@code null} if not used.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     */
    public void setValue(@Nullable byte[] value, @Nullable Charset encoding, @Nullable String mimeType) {
	if (value == null) {
	    mimeType = null;
	    encoding = null;
	}

	this.value = value;
	this.encoding = encoding;
	this.mimeType = mimeType;
    }

    /**
     * Sets the value of the body.
     *
     * @param value The value to be set in the body. {@code null} values are permitted.
     * @param encoding The encoding of the value, or {@code null} if not used.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     */
    public void setValue(@Nullable String value, @Nullable Charset encoding, @Nullable String mimeType) {
	// use default for mime type if the value is omitted
	if (value == null) {
	    mimeType = null;
	    encoding = null;
	} else {
	    if (mimeType == null || "".equals(mimeType)) {
		LOG.warn("No MIME type specified, falling back to 'text/plain'.");
		mimeType = "text/plain";
	    }
	    if (encoding == null) {
		LOG.warn("No encoding specified, using UTF-8.");
		encoding = StandardCharsets.UTF_8;
	    }
	}

	setValue(value != null ? value.getBytes(encoding) : null, encoding, mimeType);
    }

    /**
     * Sets the value of the body.
     * A MIME type of {@code text/plain} is assumed.
     *
     * @param value The value to be set in the body. {@code null} values are permitted.
     */
    public void setValue(@Nullable String value) {
	setValue(value, StandardCharsets.UTF_8, "text/plain");
    }

    /**
     * Sets the value of the body.
     *
     * @param value The value to be set in the body. {@code null} values are permitted.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     */
    public void setValue(@Nullable String value, @Nullable String mimeType) {
	setValue(value, StandardCharsets.UTF_8, mimeType);
    }

    /**
     * Sets the value of the body.
     *
     * @param value The value to be set in the body.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     */
    public void setValue(@Nullable byte[] value, @Nullable String mimeType) {
	setValue(value, null, mimeType);
    }

}
