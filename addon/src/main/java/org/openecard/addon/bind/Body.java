/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.transform.TransformerException;
import org.bouncycastle.util.encoders.Base64;
import org.openecard.ws.marshal.MarshallingTypeException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;


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

    private String value;
    private String mimeType;
    private boolean base64Encoded;
    private WSMarshaller m;

    /**
     * Creates a body without any content and the default marshaller.
     *
     * @throws WSMarshallerException Thrown in case the default marshaller could not be loaded.
     */
    protected Body() throws WSMarshallerException {
	this(loadEmptyMarshaller());
    }

    /**
     * Creates a body without any content and the given marshaller.
     *
     * @param m The marshaller which can be used by this instance to serialize and marshal XML. {@code null} values are
     *   permitted, but then each XML based function throws a {@link NullPointerException} when used.
     */
    protected Body(@Nullable WSMarshaller m) {
	this(null, null, false, m);
    }

    /**
     * Creates a body with the given content and the default marshaller.
     * The marshaller is not initialized with any classes for JAXB serialization to speed up the creation process.
     *
     * @param value Value to use as the bodies content, or {@code null} if the body should be empty.
     * @param mimeType MIME type of the value or, {@code null} if the value is {@code null}.
     * @param base64Encoded Boolean indicating if the content is BASE64 encoded.
     * @throws WSMarshallerException Thrown in case the default marshaller could not be loaded.
     */
    protected Body(@Nullable String value, @Nullable String mimeType, boolean base64Encoded)
	    throws WSMarshallerException {
	this(value, mimeType, base64Encoded, loadEmptyMarshaller());
    }

    /**
     * Creates a body with the given content and the given marshaller.
     *
     * @param value Value to use as the bodies content, or {@code null} if the body should be empty.
     * @param mimeType MIME type of the value or, {@code null} if the value is {@code null}.
     * @param base64Encoded Boolean indicating if the content is BASE64 encoded.
     * @param m The marshaller which can be used by this instance to serialize and marshal XML. {@code null} values are
     *   permitted, but then each XML based function throws a {@link NullPointerException} when used.
     */
    protected Body(@Nullable String value, @Nullable String mimeType, boolean base64Encoded, @Nullable WSMarshaller m) {
	this.value = value;
	this.mimeType = mimeType;
	this.base64Encoded = base64Encoded;
	this.m = m;
    }

    private static WSMarshaller loadEmptyMarshaller() throws WSMarshallerException {
	WSMarshaller m = WSMarshallerFactory.createInstance();
	m.removeAllTypeClasses();
	return m;
    }

    /**
     * Checks whether this instance contains a body value or not.
     *
     * @return {@code true} if the instance contains a value, {@code false} otherwise.
     */
    public boolean hasValue() {
	return value != null;
    }

    /**
     * Gets the value of this body instance.
     *
     * @return The value, or {@code null} if no value is set.
     */
    @Nullable
    public String getValue() {
	return value;
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
     * Gets whether the value of this body instance is BASE64 encoded, or not.
     *
     * @return {@code true} if the body's value is BASE64 encoded, {@code false} otherwise.
     */
    public boolean isBase64() {
	return base64Encoded;
    }

    /**
     * Gets the marshaller managed by this instance.
     *
     * @return The marshaller used for this instance. {@code null} if no marshaller has been initialized.
     */
    @Nullable
    protected WSMarshaller getMarshaller() {
	return m;
    }

    /**
     * Sets the value of the body.
     * The MIME type and BASE64 encoding flag get corrected accordingly to the value. Note that no detection of the MIME
     * type is performed.
     *
     * @param value The value to be set in the body. {@code null} values are permitted.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     * @param base64Encoded {@code true} if the value is BASE64 encoded, false otherwise.
     */
    public void setValue(@Nullable String value, @Nullable String mimeType, boolean base64Encoded) {
	// use default for mime type if the value is omitted
	if (value == null) {
	    mimeType = null;
	} else if (mimeType == null || "".equals(mimeType)) {
	    LOG.warn("No MIME type specified, falling back to 'text/plain'.");
	    mimeType = "text/plain";
	}
	// preemptively correct base64 if no value is supplied
	if (value == null) {
	    base64Encoded = false;
	}

	this.value = value;
	this.mimeType = mimeType;
	this.base64Encoded = base64Encoded;
    }

    /**
     * Sets the value of the body.
     * A MIME type of {@code text/plain} is assumed.
     *
     * @param value The value to be set in the body. {@code null} values are permitted.
     */
    public void setValue(@Nullable String value) {
	setValue(value, "text/plain");
    }

    /**
     * Sets the value of the body.
     *
     * @param value The value to be set in the body. {@code null} values are permitted.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     */
    public void setValue(@Nullable String value, @Nullable String mimeType) {
	setValue(value, mimeType, false);
    }

    /**
     * Sets the value of the body.
     * The given array gets BASE64 encoded before it is saved.
     *
     * @param value The value to be set in the body.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     * @throws NullPointerException Thrown in case the value or the marshaller is {@code null}.
     */
    public void setValue(@Nonnull byte[] value, @Nullable String mimeType) {
	if (value == null) {
	    throw new NullPointerException("The supplied value is null.");
	}
	String encVal = Base64.toBase64String(value);
	setValue(encVal, mimeType, true);
    }

    /**
     * Sets the value of the body.
     * The given DOM node is serialized with the marshaller of this instance. The MIME type is set to
     * {@code application/xml}.
     *
     * @param domNode The value to be set in the body.
     * @throws TransformerException Thrown in case the node could not be serialized.
     * @throws NullPointerException Thrown in case the node or the marshaller is {@code null}.
     */
    @SuppressWarnings("null")
    public void setValue(@Nonnull Node domNode) throws TransformerException {
	setValue(domNode, null);
    }

    /**
     * Sets the value of the body.
     * The given DOM node is serialized with the marshaller of this instance.
     *
     * @param domNode The value to be set in the body.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     * @throws TransformerException Thrown in case the node could not be serialized.
     * @throws NullPointerException Thrown in case the node or the marshaller is {@code null}.
     */
    @SuppressWarnings("null")
    public void setValue(@Nonnull Node domNode, @Nullable String mimeType) throws TransformerException {
	String nodeVal = getMarshaller().doc2str(domNode);
	if (mimeType == null || "".equals(mimeType)) {
	    setValue(nodeVal, "application/xml");
	} else {
	    setValue(nodeVal, mimeType);
	}
    }

    /**
     * Sets the value of the body.
     * The given JAXB object is serialized with the marshaller of this instance. The MIME type is set to
     * {@code application/xml} if no value is provided.
     *
     * @param jaxbObj The JAXB object to be set in the body.
     * @throws MarshallingTypeException Thrown in case the object could not be marshalled.
     * @throws TransformerException Thrown in case the node could not be serialized.
     * @throws NullPointerException Thrown in case the JAXB element or the marshaller is {@code null}.
     */
    @SuppressWarnings("null")
    public void setJAXBObjectValue(@Nonnull Object jaxbObj) throws MarshallingTypeException, TransformerException {
	setJAXBObjectValue(jaxbObj, null);
    }

    /**
     * Sets the value of the body.
     * The given JAXB object is serialized with the marshaller of this instance. The MIME type is set to
     * {@code application/xml}.
     *
     * @param jaxbObj The JAXB object to be set in the body.
     * @param mimeType The MIME type of the value.  {@code null} values are permitted.
     * @throws MarshallingTypeException Thrown in case the object could not be marshalled.
     * @throws TransformerException Thrown in case the node could not be serialized.
     * @throws NullPointerException Thrown in case the JAXB element or the marshaller is {@code null}.
     */
    @SuppressWarnings("null")
    public void setJAXBObjectValue(@Nonnull Object jaxbObj, @Nullable String mimeType) throws MarshallingTypeException,
	    TransformerException {
	Node nodeVal = getMarshaller().marshal(jaxbObj);
	setValue(nodeVal, mimeType);
    }

}
