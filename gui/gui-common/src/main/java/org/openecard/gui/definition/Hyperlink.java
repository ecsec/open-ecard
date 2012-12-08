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

package org.openecard.gui.definition;

import java.net.MalformedURLException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Hyperlink element for user consents.
 * This element should oben the address in the default browser when clicked.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public final class Hyperlink extends IDTrait implements InputInfoUnit {

    private static final Logger _logger = LoggerFactory.getLogger(Hyperlink.class);

    private String text;
    private URL href;

    /**
     * Gets the text displayed of the hyperlink.
     * In an anchor tag, this would be the element text.
     *
     * @return The displayed text of the link.
     */
    public String getText() {
	return text;
    }
    /**
     * Sets the text displayed of the hyperlink.
     * In an anchor tag, this would be the element text.
     *
     * @param text The displayed text of the link.
     */
    public void setText(String text) {
	this.text = text;
    }

    /**
     * Gets the address part of the hyperlink.
     * In an anchor tag, this would be the href attribute.
     *
     * @return The address part of the link.
     */
    public URL getHref() {
	return href;
    }
    /**
     * Sets the address part of the hyperlink.
     * In an anchor tag, this would be the href attribute.
     *
     * @param href The address part of the link.
     */
    public void setHref(URL href) {
	this.href = href;
    }
    /**
     * Sets the address part of the hyperlink.
     * In an anchor tag, this would be the href attribute. This method converts the String instance to a URL instance.
     * 
     * @param href The address part of the link.
     * @throws MalformedURLException Thrown if the given string is not a valid URL.
     */
    public void setHref(String href) throws MalformedURLException {
	this.href = new URL(href);
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.HYPERLINK;
    }


    @Override
    public void copyContentFrom(InfoUnit origin) {
	if (!(this.getClass().equals(origin.getClass()))) {
	    _logger.warn("Trying to copy content from type {} to type {}.", origin.getClass(), this.getClass());
	    return;
	}
	Hyperlink other = (Hyperlink) origin;
	// do copy
	this.href = other.href;
	this.text = other.text;
    }

}
