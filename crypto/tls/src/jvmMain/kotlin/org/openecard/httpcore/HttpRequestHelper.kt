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
package org.openecard.httpcore

import org.apache.http.HttpRequest
import org.openecard.common.AppVersion.name
import org.openecard.common.AppVersion.version
import java.net.URL

/**
 * Helper with functionality commonly needed when sending HTTP requests over Apache httpcore.
 *
 * @author Tobias Wich
 */
object HttpRequestHelper {
    /**
     * Modify the given request and add a common set of headers.
     *
     * @param request Request which should be modified.
     * @param host Name of the host to set in the Host header.
     * @return Modified request instance for command chaining.
     */
	@JvmStatic
    fun setDefaultHeader(request: HttpRequest, host: String?): HttpRequest {
        request.setHeader("Connection", "keep-alive")
        request.setHeader("User-Agent", "$name/$version")
        if (!host.isNullOrEmpty()) {
            request.setHeader("Host", host)
        }
        return request
    }

    /**
     * Modify the given request and add a common set of headers.
     *
     * @param request Request which should be modified.
     * @param endpoint URL of the endpoint for the Host header.
     * @return Modified request instance for command chaining.
     */
	@JvmStatic
	fun setDefaultHeader(request: HttpRequest, endpoint: URL?): HttpRequest {
        var host: String? = null
        if (endpoint != null) {
            host = endpoint.host
            host += if (endpoint.port == -1) "" else (":" + endpoint.port)
        }
        return setDefaultHeader(request, host)
    }
}
