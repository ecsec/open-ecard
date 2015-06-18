/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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

package org.openecard.gui.definition;

import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Definition class for simple text elements.
 * The Text element is a text displaying an information to the user.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
public final class Text extends IDTrait implements InputInfoUnit {

    private static final Logger _logger = LoggerFactory.getLogger(Text.class);

    private Document document;

    /**
     * Creates a new empty instance.
     */
    public Text() {
    }

    /**
     * Creates a new instance from the given String {@code text}.
     *
     * @param text The text which shall be displayed.
     */
    public Text(String text) {
	this(text.getBytes(Charset.forName("UTF-8")), "text/plain");
    }

    private Text(byte[] value, String mimeType) {
	document = new Document();
	document.setMimeType(mimeType);
	document.setValue(value);
    }

    /**
     * Creates a new instance from the given {@link Document} {@code doc}.
     * <br>
     * <br>
     * Note: The {@link Document} type allows every mime type. The ability to render {@link Document}s of types other
     * than text/plain depends on the GUI implementation and is not granted.
     *
     * @param doc {@link Document} to set for this Text.
     */
    public Text(Document doc) {
	document = doc;
    }

    /**
     * Gets the text set for this instance.
     *
     * @return The text of this instance or an empty string if the underlying {@link Document} is {@code NULL} or the
     * value of the {@link Document} or the MimeType of the underlying {@link Document} is {@code NULL} or does not start
     * with {@code text/}.
     */
    public String getText() {
	if (document == null || document.getValue() == null || document.getValue().length == 0) {
	    return "";
	}

	if (document.getMimeType() != null && document.getMimeType().startsWith("text/")) {
	    return new String(document.getValue(), Charset.forName("UTF-8"));
	} else {
	    return "";
	}
    }
    
    /**
     * Sets the text for this instance.
     *
     * @param text The text to set for this instance.
     */
    public void setText(String text) {
	if (document == null) {
	    document = new Document();
	}

	document.setMimeType("text/plain");
	document.setValue(text.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Sets the Document of this Text instance.
     * <br>
     * <br>
     * Note: The {@link Document} type allows every mime type. The ability to render {@link Document}s of types other
     * than text/plain depends on the GUI implementation and is not granted.
     *
     * @param doc {@link Document} to set for this Text.
     */
    public void setDocument(Document doc) {
	document = doc;
    }

    /**
     * Get the underlying Document of this Text instance.
     *
     * @return The {@link Document} used by this instance or null if there is currently no such document.
     */
    public Document getDocument() {
	return document;
    }

    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.TEXT;
    }


    @Override
    public void copyContentFrom(InfoUnit origin) {
	if (!(this.getClass().equals(origin.getClass()))) {
	    _logger.warn("Trying to copy content from type {} to type {}.", origin.getClass(), this.getClass());
	    return;
	}
	Text other = (Text) origin;
	// do copy
	if (other.document != null) {
	    Document doc = new Document();
	    if (other.document.getMimeType() != null) {
		doc.setMimeType(other.document.getMimeType());
	    }

	    if (other.document.getValue() != null) {
		byte[] contentBytes = new byte[other.document.getValue().length];
		System.arraycopy(other.document.getValue(), 0, contentBytes, 0, other.document.getValue().length);
		doc.setValue(contentBytes);
	    }
	    this.setDocument(doc);
	}
    }

}
