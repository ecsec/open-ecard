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
 */
package org.openecard.control.binding.http.interceptor

import org.apache.http.HttpException
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseInterceptor
import org.apache.http.impl.EnglishReasonPhraseCatalog
import org.apache.http.message.BasicStatusLine
import org.apache.http.protocol.HttpContext
import java.io.IOException
import java.util.*

/**
 * Interceptor to correct incomplete HTTP status line.
 *
 * @author Tobias Wich
 */
class StatusLineResponseInterceptor : HttpResponseInterceptor {
    @Throws(HttpException::class, IOException::class)
    override fun process(hr: HttpResponse, hc: HttpContext) {
        val statusLine = hr.statusLine
        val statusCode = statusLine.statusCode
        val locale = hr.locale
        var reason = statusLine.reasonPhrase
        reason = reason ?: reasonForCode(statusCode, locale)
        hr.statusLine = BasicStatusLine(statusLine.protocolVersion, statusCode, reason)
    }

    companion object {
        /**
         * Get reason phrase for HTTP status code.
         *
         * @param code HTTP status code
         * @param locale Langue the reason should be written in, or null for ENGLISH.
         * @return Reason phrase, or "Extension Code" if code is not defined in the RFC.
         */
        private fun reasonForCode(code: Int, locale: Locale?): String {
            var locale = locale
            locale = locale ?: Locale.ENGLISH
            val reason = EnglishReasonPhraseCatalog.INSTANCE.getReason(code, locale)
            return reason ?: "Extension Code"
        }
    }
}
