/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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
 */
package org.openecard.control.binding.http.interceptor

import org.apache.http.HttpException
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseInterceptor
import org.apache.http.protocol.HttpContext
import org.openecard.common.AppVersion.name
import org.openecard.common.AppVersion.specName
import org.openecard.common.AppVersion.specVersions
import org.openecard.common.AppVersion.version
import java.io.IOException

/**
 * HttpResponseInterceptor implementation which adds the `Server` header to all responses sent by the HTTP Binding.
 *
 * @author Hans-Martin Haase
 */
class ServerHeaderResponseInterceptor : HttpResponseInterceptor {
    @Throws(HttpException::class, IOException::class)
    override fun process(hr: HttpResponse, hc: HttpContext) {
        hr.addHeader("Server", buildServerHeaderValue())
    }

    /**
     * Creates the value of the `Server` header according to BSI-TR-03124-1 v1.2 section 2.2.2.1.
     *
     * @return A string containing the `Server` header value.
     */
    fun buildServerHeaderValue(): String {
        val builder = StringBuilder()

        builder.append(name)
        builder.append("/")
        builder.append(version)

        builder.append(" (")
        var firstSpec = true
        for (version in specVersions) {
            if (!firstSpec) {
                builder.append(" ")
            } else {
                firstSpec = false
            }
            builder.append(specName)
            builder.append("/")
            builder.append(version)
        }
        builder.append(")")

        return builder.toString()
    }
}
