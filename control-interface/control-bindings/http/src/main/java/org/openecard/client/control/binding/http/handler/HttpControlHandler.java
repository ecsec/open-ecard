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

package org.openecard.client.control.binding.http.handler;

import java.io.IOException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.openecard.client.control.handler.ControlHandler;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class HttpControlHandler extends ControlHandler implements HttpRequestHandler {

    /**
     * Creates a new HttpControlHandler.
     *
     * @param path Path
     */
    public HttpControlHandler(String path) {
	super(path);
    }

    /**
     * Handles a HTTP request.
     *
     * @param request HttpRequest
     * @param response HttpResponse
     * @param context HttpContext
     * @throws HttpException
     * @throws IOException
     */
    @Override
    public abstract void handle(HttpRequest request, HttpResponse response, HttpContext context) 
	throws IOException;

}
