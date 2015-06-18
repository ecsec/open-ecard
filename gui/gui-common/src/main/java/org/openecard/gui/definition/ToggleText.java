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
 * Definition class for a text element which can fold its content.
 * The ToggleText has a title which is always displayed and a text which can be folded.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
public final class ToggleText extends IDTrait implements InputInfoUnit {

    private static final Logger logger = LoggerFactory.getLogger(Text.class);

    private String title;
    private Document document;
    private boolean collapsed;


    /**
     * Gets whether the text is collapsed or not.
     * In the collapsed state, the element's text is not visible.
     *
     * @return {@code true} if the text is collapsed, {@code false} otherwise.
     */
    public boolean isCollapsed() {
	return collapsed;
    }
    /**
     * Sets whether the text is collapsed or not.
     * In the collapsed state, the element's text is not visible.
     *
     * @param collapsed {@code true} if the text is collapsed, {@code false} otherwise.
     */
    public void setCollapsed(boolean collapsed) {
	this.collapsed = collapsed;
    }

    /**
     * Gets the title of this instance.
     *
     * @return The title of this instance.
     */
    public String getTitle() {
	return title;
    }
    /**
     * Sets the title of this instance.
     *
     * @param title The title of this instance.
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * Gets the text of this instance.
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
     * Sets the text of this instance.
     *
     * @param text The text of this instance.
     */
    public void setText(String text) {
	if (document == null) {
	    document = new Document();
	}

	document.setMimeType("text/plain");
	document.setValue(text.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Sets the Document of this ToggleText instance.
     * <br>
     * <br>
     * Note: The {@link Document} type allows every mime type. The ability to render {@link Document}s of types other
     * than text/plain depends on the GUI implementation and is not granted.
     *
     * @param doc {@link Document} to set for this ToggleText.
     */
    public void setDocument(Document doc) {
	document = doc;
    }

    /**
     * Get the underlying Document of this ToggleText instance.
     *
     * @return The {@link Document} used by this instance or null if there is currently no such document.
     */
    public Document getDocument() {
	return document;
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.TOGGLE_TEXT;
    }

    @Override
    public void copyContentFrom(InfoUnit origin) {
	if (! (this.getClass().equals(origin.getClass()))) {
	    logger.warn("Trying to copy content from type {} to type {}.", origin.getClass(), this.getClass());
	    return;
	}
	ToggleText other = (ToggleText) origin;
	// do copy
	this.title = other.title;
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
	this.collapsed = other.collapsed;
    }

}
