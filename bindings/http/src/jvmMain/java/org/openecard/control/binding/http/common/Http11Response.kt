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
package org.openecard.control.binding.http.common

import org.apache.http.HttpResponse
import org.apache.http.HttpVersion
import org.apache.http.StatusLine
import org.apache.http.message.BasicHttpResponse

/**
 *
 * @author Tobias Wich
 */
class Http11Response constructor(code: Int, reason: String? = null) :
    BasicHttpResponse(HttpVersion.HTTP_1_1, code, reason) {
    constructor(statusline: StatusLine) : this(statusline.statusCode, statusline.reasonPhrase)

    companion object {
        /**
         * Copy the content of a HttpResponse to another instance.
         *
         * @param in HttpResponse
         * @param out HttpResponse
         */
        fun copyHttpResponse(`in`: HttpResponse, out: HttpResponse) {
            // remove and copy headers
            var headIt = out.headerIterator()
            while (headIt.hasNext()) {
                headIt.nextHeader()
                headIt.remove()
            }
            headIt = `in`.headerIterator()
            while (headIt.hasNext()) {
                val next = headIt.nextHeader()
                out.addHeader(next)
            }

            // set entity stuff
            if (`in`.entity != null) {
                val entity = `in`.entity
                out.entity = entity
                if (entity.contentType != null) {
                    out.setHeader(entity.contentType)
                }
                if (entity.contentEncoding != null) {
                    out.setHeader(entity.contentEncoding)
                }
                if (entity.contentLength > 0) {
                    out.setHeader(HeaderTypes.CONTENT_LENGTH.fieldName(), entity.contentLength.toString())
                }
                // TODO: use chunked, repeatable and streaming attribute from entity
            }

            // copy rest
            val l = `in`.locale
            if (l != null) {
                out.locale = l
            }
            out.statusLine = `in`.statusLine
        }
    }
}
