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

package org.openecard.client.connector.interceptor.cors;

import java.io.IOException;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.openecard.client.connector.http.header.ResponseHeader;
import org.openecard.client.connector.interceptor.ConnectorResponseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class CORSResponseInterceptor extends ConnectorResponseInterceptor {

    private static final Logger _logger = LoggerFactory.getLogger(CORSResponseInterceptor.class);

    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
	String cors = (String) httpResponse.getParams().getParameter(CORSResponseInterceptor.class.getName());

	if (cors != null && cors.equals("required")) {
	    // CORS required
	    _logger.debug("CORS required");

	    httpResponse.addHeader(ResponseHeader.Field.ACCESS_CONTROL_ALLOW_ORIGIN.getFieldName(), "*");
	    httpResponse.addHeader(ResponseHeader.Field.ACCESS_CONTROL_ALLOW_METHODS.getFieldName(), "GET");
	}
    }

}
