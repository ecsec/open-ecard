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

package org.openecard.client.gui.definition;

import java.net.MalformedURLException;
import java.net.URL;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Hyperlink implements InputInfoUnit {

    private String text;
    private URL href;

    /**
     * @return the text
     */
    public String getText() {
	return text;
    }

    /**
     * @param text the text to set
     */
    public void setText(String text) {
	this.text = text;
    }

    /**
     * @return the href
     */
    public URL getHref() {
	return href;
    }

    /**
     * @param href the href to set
     */
    public void setHref(URL href) {
	this.href = href;
    }
    /**
     * @param href the href to set
     */
    public void setHref(String href) throws MalformedURLException {
	this.href = new URL(href);
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.HYPERLINK;
    }

}
