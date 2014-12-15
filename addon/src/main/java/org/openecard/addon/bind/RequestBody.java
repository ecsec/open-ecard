/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import org.w3c.dom.Node;


/**
 * This class extends the Body class by a path parameter.
 *
 * @author Hans-Martin Haase
 */
public class RequestBody extends Body {

    /**
     * Full resource path of the incoming request.
     */
    private final String path;

    /**
     * Creates a new RequestBody object.
     *
     * @param path Full resource name of the in coming request.
     * @param value Body of the in coming request as Node object.
     * @param mimeType MimeType of {@code value}.
     */
    public RequestBody(String path, Node value, String mimeType) {
	super(value, mimeType);
	this.path = path;
    }

    /**
     * Get the full resource path the in coming request.
     *
     * @return A String containing the full resource path.
     */
    public String getPath() {
	return path;
    }
}
