/****************************************************************************
 * Copyright (C) 2015-2018 ecsec GmbH.
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

package org.openecard.control.binding.http.handler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.apache.http.Header;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpStatus;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.control.binding.http.common.Http11Method;
import org.openecard.control.binding.http.common.Http11Response;


/**
 * Class adding functionality to answer CORS preflight requests and to add the relevant CORS headers.
 *
 * @author Tobias Wich
 */
public class CORSFilter {

    private static final Collection<String> NO_CORS_PATHS;

    static {
	NO_CORS_PATHS = new LinkedList<>();
	NO_CORS_PATHS.add("/eID-Client?ShowUI");
	NO_CORS_PATHS.add("/eID-Client?tcTokenURL");
    }

    public HttpResponse preProcess(HttpRequest httpRequest, HttpContext context) {
	URI origin = getOrigin(httpRequest);

	// check if we are dealing with a CORS request
	if (origin != null) {
	    if (isPreflight(httpRequest)) {
		// preflight response
		String method = getMethod(httpRequest);
		if (method != null) {
		    HttpResponse res = new Http11Response(HttpStatus.SC_OK);
		    if (OriginsList.isValidOrigin(origin)) {
			postProcess(httpRequest, res, context);
		    }
		    return res;
		}
	    }
	}

	// no CORS, just continue
	return null;
    }

    public void postProcess(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext context) {
	// only process if this is an allowed resource for CORS
	if (isNoCorsPath(httpRequest.getRequestLine().getUri())) {
	    return;
	}

	// only do this when client sent a CORS request
	URI origin = getOrigin(httpRequest);
	if (origin != null) {
	    // add some common headers
	    httpResponse.addHeader("Vary", "Origin");

	    // add CORS Headers
	    httpResponse.addHeader("Access-Control-Allow-Origin", origin.toString());
	    httpResponse.addHeader("Access-Control-Allow-Credentials", "true");

	    // preflight stuff
	    if (isPreflight(httpRequest)) {
		String method = getMethod(httpRequest);
		if (method != null) {
		    httpResponse.addHeader("Access-Control-Allow-Methods", method);
		}
		// TODO: figure out if we need this header stuff
		//httpResponse.addHeader("Access-Control-Allow-Headers", headers);
	    }
	}
    }

    @Nullable
    private URI getOrigin(@Nonnull HttpRequest httpRequest) {
	try {
	    Header origin = httpRequest.getFirstHeader("Origin");
	    if (origin != null) {
		String origStr = origin.getValue();
		if (origStr == null) {
		    origStr = "";
		}
		return new URI(origStr);
	    }
	} catch (URISyntaxException ex) {
	    // no or invalid URI given
	}
	return null;
    }

    @Nullable
    private String getMethod(@Nonnull HttpRequest httpRequest) {
	Header acrm = httpRequest.getFirstHeader("Access-Control-Request-Method");
	String acrmStr = null;
	if (acrm != null) {
	    acrmStr = acrm.getValue();
	    if (acrmStr != null && acrmStr.isEmpty()) {
		acrmStr = null;
	    }
	}
	return acrmStr;
    }

    private boolean isNoCorsPath(String reqLineUri) {
	for (String nextPath : NO_CORS_PATHS) {
	    if (reqLineUri.startsWith(nextPath)) {
		return true;
	    }
	}

	return false;
    }

    private boolean isPreflight(HttpRequest req) {
	String method = req.getRequestLine().getMethod();
	return Http11Method.OPTIONS.getMethodString().equals(method);
    }

}
