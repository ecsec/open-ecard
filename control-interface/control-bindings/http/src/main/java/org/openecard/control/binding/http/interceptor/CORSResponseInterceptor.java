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

package org.openecard.control.binding.http.interceptor;

import java.io.IOException;
import org.openecard.apache.http.Header;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpResponseInterceptor;
import org.openecard.apache.http.HttpStatus;
import org.openecard.apache.http.entity.StringEntity;
import org.openecard.apache.http.message.BasicHttpRequest;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.control.binding.http.common.HeaderTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public class CORSResponseInterceptor implements HttpResponseInterceptor {

    private static final Logger _logger = LoggerFactory.getLogger(CORSResponseInterceptor.class);

    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
	// enable CORS for all types of HTTP responses
	httpResponse.setHeader(HeaderTypes.ACCESS_CONTROL_ALLOW_ORIGIN.fieldName(), "*");

	if (((BasicHttpRequest) httpContext.getAttribute("http.request")).getParams().isParameterTrue("disable-CORS-redirect")
		&& httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_SEE_OTHER) {
	    _logger.debug("CORS redirect not supported");

	    Header locationHeader = httpResponse.getLastHeader(HeaderTypes.LOCATION.fieldName());

	    if (locationHeader != null && locationHeader.getValue() != null) {
		httpResponse.setEntity(new StringEntity(locationHeader.getValue()));
		httpResponse.removeHeader(locationHeader);
		httpResponse.setStatusLine(httpResponse.getStatusLine().getProtocolVersion(), HttpStatus.SC_OK);
	    }
	}
    }

}
