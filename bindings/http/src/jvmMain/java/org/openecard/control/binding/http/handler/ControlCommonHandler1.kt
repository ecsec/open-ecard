/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
import org.apache.http.entity.StringEntity
import org.apache.http.protocol.HttpContext
import org.openecard.control.binding.http.HttpException
import org.openecard.control.binding.http.common.Http11Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * @author Moritz Horsch
 */
abstract class ControlCommonHandler : HttpControlHandler {
    /**
     * Creates a new new ControlCommonHandler.
     */
    protected constructor() : super("*")

    /**
     * Creates a new new ControlCommonHandler.
     *
     * @param path Path
     */
    protected constructor(path: String) : super(path)

    /**
     * Handles a HTTP request.
     *
     * @param httpRequest HTTPRequest
     * @return HTTPResponse
     * @throws HttpException
     * @throws Exception
     */
    @Throws(org.apache.http.HttpException::class, Exception::class)
    abstract fun handle(httpRequest: HttpRequest): HttpResponse

    /**
     * Handles a HTTP request.
     *
     * @param request HttpRequest
     * @param response HttpResponse
     * @param context HttpContext
     * @throws HttpException
     * @throws IOException
     */
    @Throws(org.apache.http.HttpException::class, IOException::class)
    override fun handle(request: HttpRequest, response: HttpResponse, context: HttpContext) {
        _logger.debug("HTTP request: {}", request.toString())
        var httpResponse: HttpResponse? = null

        try {
            // Forward request parameters to response parameters
            response.params = request.params

            httpResponse = handle(request)
        } catch (e: HttpException) {
            httpResponse = Http11Response(HttpStatus.SC_BAD_REQUEST)
            httpResponse.setEntity(StringEntity(e.message, "UTF-8"))

            if (e.message != null && !e.message!!.isEmpty()) {
                httpResponse.setEntity(StringEntity(e.message, "UTF-8"))
            }

            httpResponse.setStatusCode(e.httpStatusCode)
        } catch (e: Exception) {
            httpResponse = Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            _logger.error(e.message, e)
        } finally {
            Http11Response.Companion.copyHttpResponse(httpResponse!!, response)
            _logger.debug("HTTP response: {}", response)
            _logger.debug("HTTP request handled by: {}", javaClass.name)
        }
    }

    companion object {
        private val _logger: Logger = LoggerFactory.getLogger(ControlCommonHandler::class.java)
    }
}
