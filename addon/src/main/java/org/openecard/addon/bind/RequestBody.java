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

import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;


/**
 * Request specific Body for use in Plug-Ins and Bindings.
 * Additionally to the base elements, a request contains the requested resource.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class RequestBody extends Body {

    /**
     * Resource path of the request.
     */
    private final String path;

    public RequestBody(String path) throws WSMarshallerException {
	super();
	this.path = path;
    }

    public RequestBody(String path, WSMarshaller m) {
	super(m);
	this.path = path;
    }

    /**
     * Get the resource path of this request.
     *
     * @return A String containing the requests resource path.
     */
    public String getPath() {
	return path;
    }

}
