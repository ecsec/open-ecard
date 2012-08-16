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

package org.openecard.client.connector.interceptor;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CORSRequestInterceptor implements HttpRequestInterceptor {

    private static final Logger _logger = LoggerFactory.getLogger(CORSRequestInterceptor.class);

    private static ArrayList<String> userAgents = new ArrayList<String>() {
	{
	    add("TODO");
	}
    };

    @Override
    public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
	String userAgent = httpRequest.getFirstHeader("User-Agent").getValue();

	if (userAgents.contains(userAgent)) {
	    _logger.debug("CORS required");
	    httpContext.setAttribute(CORSRequestInterceptor.class.getName(), "required");
	}
    }

}
