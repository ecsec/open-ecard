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
 */
package org.openecard.control.binding.http.handler

import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.protocol.HttpContext
import org.openecard.control.binding.http.common.Http11Method
import org.openecard.control.binding.http.common.Http11Response
import java.net.URI
import java.net.URISyntaxException
import javax.annotation.Nonnull

/**
 * Class adding functionality to answer CORS preflight requests and to add the relevant CORS headers.
 *
 * @author Tobias Wich
 */
class CORSFilter {
    fun preProcess(httpRequest: HttpRequest, context: HttpContext?): HttpResponse? {
        val origin = getOrigin(httpRequest)

        // check if we are dealing with a CORS request
        if (origin != null) {
            if (isPreflight(httpRequest)) {
                // preflight response
                val method = getMethod(httpRequest)
                if (method != null) {
                    val res: HttpResponse = Http11Response(HttpStatus.SC_OK)
                    if (OriginsList.isValidOrigin(origin)) {
                        postProcess(httpRequest, res, context)
                    }
                    return res
                }
            }
        }

        // no CORS, just continue
        return null
    }

    fun postProcess(httpRequest: HttpRequest, httpResponse: HttpResponse, context: HttpContext?) {
        // only process if this is an allowed resource for CORS
        if (isNoCorsPath(httpRequest.requestLine.uri)) {
            return
        }

        // only do this when client sent a CORS request
        val origin = getOrigin(httpRequest)
        if (origin != null) {
            // add some common headers
            httpResponse.addHeader("Vary", "Origin")

            // add CORS Headers
            httpResponse.addHeader("Access-Control-Allow-Origin", origin.toString())
            httpResponse.addHeader("Access-Control-Allow-Credentials", "true")

            // preflight stuff
            if (isPreflight(httpRequest)) {
                val method = getMethod(httpRequest)
                if (method != null) {
                    httpResponse.addHeader("Access-Control-Allow-Methods", method)
                }
                // TODO: figure out if we need this header stuff
                //httpResponse.addHeader("Access-Control-Allow-Headers", headers);
            }
        }
    }

    private fun getOrigin(@Nonnull httpRequest: HttpRequest): URI? {
        try {
            val origin = httpRequest.getFirstHeader("Origin")
            if (origin != null) {
                var origStr = origin.value
                if (origStr == null) {
                    origStr = ""
                }
                return URI(origStr)
            }
        } catch (ex: URISyntaxException) {
            // no or invalid URI given
        }
        return null
    }

    private fun getMethod(@Nonnull httpRequest: HttpRequest): String? {
        val acrm = httpRequest.getFirstHeader("Access-Control-Request-Method")
        var acrmStr: String? = null
        if (acrm != null) {
            acrmStr = acrm.value
            if (acrmStr != null && acrmStr.isEmpty()) {
                acrmStr = null
            }
        }
        return acrmStr
    }

    private fun isNoCorsPath(reqLineUri: String): Boolean {
        for (nextPath in NO_CORS_PATHS) {
            if (reqLineUri.startsWith(nextPath)) {
                return true
            }
        }

        return false
    }

    private fun isPreflight(req: HttpRequest): Boolean {
        val method = req.requestLine.method
        return Http11Method.OPTIONS.methodString == method
    }

    companion object {
        private val NO_CORS_PATHS: List<String> = listOf(
			"/eID-Client?ShowUI",
			"/eID-Client?tcTokenURL"
		)
	}
}
