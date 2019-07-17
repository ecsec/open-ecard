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

package org.openecard.transport.httpcore;

import java.net.URL;
import org.apache.http.HttpRequest;
import org.openecard.common.AppVersion;


/**
 * Helper with functionality commonly needed when sending HTTP requests over Apache httpcore.
 *
 * @author Tobias Wich
 */
public class HttpRequestHelper {

    /**
     * Modify the given request and add a common set of headers.
     *
     * @param request Request which should be modified.
     * @param host Name of the host to set in the Host header.
     * @return Modified request instance for command chaining.
     */
    public static HttpRequest setDefaultHeader(HttpRequest request, String host) {
	request.setHeader("Connection", "keep-alive");
	request.setHeader("User-Agent", AppVersion.getName() + "/" + AppVersion.getVersion());
	if (host != null && ! host.isEmpty()) {
	    request.setHeader("Host", host);
	}
	return request;
    }

    /**
     * Modify the given request and add a common set of headers.
     *
     * @param request Request which should be modified.
     * @param endpoint URL of the endpoint for the Host header.
     * @return Modified request instance for command chaining.
     */
    public static HttpRequest setDefaultHeader(HttpRequest request, URL endpoint) {
	String host = null;
	if (endpoint != null) {
	    host = endpoint.getHost();
	    host += endpoint.getPort() == -1 ? "" : (":" + endpoint.getPort());
	}
	return setDefaultHeader(request, host);
    }

}
