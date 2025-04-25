/****************************************************************************
 * Copyright (C) 2013-2022 HS Coburg.
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

import org.apache.http.*
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.protocol.HttpContext
import org.openecard.addon.AddonManager
import org.openecard.addon.AddonNotFoundException
import org.openecard.addon.AddonSelector
import org.openecard.addon.bind.*
import org.openecard.common.OpenecardProperties
import org.openecard.common.util.FileUtils.toByteArray
import org.openecard.common.util.HttpRequestLineUtils
import org.openecard.control.binding.http.common.DocumentRoot
import org.openecard.control.binding.http.common.HeaderTypes
import org.openecard.control.binding.http.common.Http11Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.*
import javax.annotation.Nonnull

/**
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class HttpAppPluginActionHandler(@Nonnull addonManager: AddonManager?) : HttpControlHandler("*") {
    private val selector = AddonSelector(addonManager)

    @Throws(HttpException::class, IOException::class)
    override fun handle(httpRequest: HttpRequest, httpResponse: HttpResponse, context: HttpContext) {
        LOG.debug("HTTP request: {}", httpRequest.toString())

        val corsFilter = CORSFilter()
        val corsResp = corsFilter.preProcess(httpRequest, context)
        if (corsResp != null) {
            // CORS Response created, return it to the caller
            // This is either a preflight response, or a block, because the Origin mismatched
            LOG.debug("HTTP response: {}", corsResp)
            Http11Response.Companion.copyHttpResponse(corsResp, httpResponse)
            return
        }

        // deconstruct request uri
        val uri = httpRequest.requestLine.uri
        val requestURI = URI.create(uri)
        val path = requestURI.path
        val resourceName = path.substring(1, path.length) // remove leading '/'

        // find suitable addon
        var action: AppPluginAction? = null
        try {
            action = selector.getAppPluginAction(resourceName)

            val rawQuery = requestURI.rawQuery
            val queries = createQueryMap()
            if (rawQuery != null) {
                queries.putAll(HttpRequestLineUtils.transform(rawQuery))
            }

            var body: RequestBody? = null
            if (httpRequest is HttpEntityEnclosingRequest) {
                LOG.debug("Request contains an entity.")
                body = getRequestBody(httpRequest, resourceName)
            }

            val headers = readReqHeaders(httpRequest)
            // and add some special values to the header section
            headers.setHeader(METHOD_HDR, httpRequest.requestLine.method)

            val bindingResult = action.execute(body, queries, headers, null, null)

            val response = createHTTPResponseFromBindingResult(bindingResult)
            response.params = httpRequest.params
            LOG.debug("HTTP response: {}", response)
            Http11Response.Companion.copyHttpResponse(response, httpResponse)

            // CORS post processing
            corsFilter.postProcess(httpRequest, httpResponse, context)
        } catch (ex: AddonNotFoundException) {
            if (path == "/") {
                IndexHandler().handle(httpRequest, httpResponse, context)
            } else if (path.startsWith("/")) {
                FileHandler(DocumentRoot("/www", "/www-files")).handle(httpRequest, httpResponse, context)
            } else {
                DefaultHandler().handle(httpRequest, httpResponse, context)
            }
        } finally {
            if (action != null) {
                selector.returnAppPluginAction(action)
            }
        }
    }


    private fun readReqHeaders(httpRequest: HttpRequest): Headers {
        val headers = Headers()

        // loop over all headers in the request
        val it = httpRequest.headerIterator()
        while (it.hasNext()) {
            val next = it.nextHeader()
            val name = next.name
            val value = next.value

            if (isMultiValueHeaderType(name)) {
                for (part in value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                    headers.addHeader(name, part.trim { it <= ' ' })
                }
            } else {
                headers.addHeader(name, value)
            }
        }

        return headers
    }

    private fun isMultiValueHeaderType(@Nonnull name: String): Boolean {
        // TODO: add further header types
        return when (name) {
            "Accept", "Accept-Language", "Accept-Encoding" -> true
            else -> false
        }
    }


    private fun addHTTPEntity(response: HttpResponse, bindingResult: BindingResult) {
        val responseBody = bindingResult.body
        if (responseBody != null && responseBody.hasValue()) {
            LOG.debug("BindingResult contains a body.")
            // determine content type
            val ct = ContentType.create(responseBody.mimeType, responseBody.encoding)

            val entity = ByteArrayEntity(responseBody.value, ct)
            response.entity = entity
        } else {
            LOG.debug("BindingResult contains no body.")
            if (bindingResult.resultMessage != null) {
                val ct = ContentType.create("text/plain", Charset.forName("UTF-8"))
                val entity = StringEntity(bindingResult.resultMessage, ct)
                response.entity = entity
            }
        }
    }

    private fun createHTTPResponseFromBindingResult(bindingResult: BindingResult): HttpResponse {
        val resultCode = bindingResult.resultCode
        LOG.debug("Recieved BindingResult with ResultCode {}", resultCode)
        var response: HttpResponse
        when (resultCode) {
            BindingResultCode.OK -> response = Http11Response(HttpStatus.SC_OK)
            BindingResultCode.REDIRECT -> {
                response = Http11Response(HttpStatus.SC_SEE_OTHER)
                val location = bindingResult.auxResultData[AuxDataKeys.REDIRECT_LOCATION]
                if (location != null && !location.isEmpty()) {
                    response.addHeader(HeaderTypes.LOCATION.fieldName(), location)
                } else {
                    // redirect requires a location field
                    LOG.error("No redirect address available in given BindingResult instance.")
                    response = Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                }
            }

            BindingResultCode.WRONG_PARAMETER, BindingResultCode.MISSING_PARAMETER -> response = Http11Response(
                HttpStatus.SC_BAD_REQUEST
            )

            BindingResultCode.INTERNAL_ERROR, BindingResultCode.INTERRUPTED -> response =
                Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR)

            BindingResultCode.RESOURCE_UNAVAILABLE, BindingResultCode.DEPENDING_HOST_UNREACHABLE -> response =
                Http11Response(
                    HttpStatus.SC_NOT_FOUND
                )

            BindingResultCode.RESOURCE_LOCKED -> response = Http11Response(HttpStatus.SC_LOCKED)
            BindingResultCode.TIMEOUT -> response = Http11Response(HttpStatus.SC_GATEWAY_TIMEOUT)
            BindingResultCode.TOO_MANY_REQUESTS ->        // Code for TOO MANY REQUESTS is 429 according to RFC 6585
                response = Http11Response(429)

            else -> {
                LOG.error("Untreated result code: {}", resultCode)
                response = Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            }
        }

        addHTTPEntity(response, bindingResult)
        return response
    }

    @Throws(IOException::class)
    private fun getRequestBody(entityRequest: HttpEntityEnclosingRequest, resourceName: String): RequestBody? {
        try {
            val entity = entityRequest.entity
            val `is` = entity.content

            val ct = ContentType.get(entity)
            val value = toByteArray(`is`)
            val mimeType = ct.mimeType
            val cs = ct.charset

            val body = RequestBody(resourceName)
            body.setValue(value, cs, mimeType)
            return body
        } catch (e: UnsupportedCharsetException) {
            LOG.error("Failed to create request body.", e)
        } catch (e: ParseException) {
            LOG.error("Failed to create request body.", e)
        }

        return null
    }

    private fun createQueryMap(): MutableMap<String, String> {
        val caseInsensitivePath = OpenecardProperties.getProperty("legacy.case_insensitive_path").toBoolean()
        return if (!caseInsensitivePath) {
            HashMap(0)
        } else {
            TreeMap { o1, o2 ->
                o1.compareTo(
                    o2,
                    ignoreCase = true
                )
            }
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(HttpAppPluginActionHandler::class.java)

        const val METHOD_HDR: String = "X-OeC-Method"
    }
}
