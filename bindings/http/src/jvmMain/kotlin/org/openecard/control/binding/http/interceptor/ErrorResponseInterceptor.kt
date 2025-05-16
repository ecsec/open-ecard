/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseInterceptor
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.protocol.HttpContext
import org.openecard.common.util.HTMLUtils
import org.openecard.control.binding.http.common.DocumentRoot
import org.openecard.control.binding.http.common.HTTPTemplate
import org.openecard.control.binding.http.common.HeaderTypes
import org.openecard.control.binding.http.common.MimeType
import org.openecard.i18n.I18N
import java.io.ByteArrayOutputStream

private val logger = KotlinLogging.logger {}

/**
 * An HttpResponseInterceptor implementation for errors.
 * <br></br>
 * <br></br>
 * The interceptor handles just messages with defined HTTP status codes. If such a message is received than the content
 * will be modified by using a given HTML template.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class ErrorResponseInterceptor(
	documentRoot: DocumentRoot,
	template: String,
	private val errorCodes: List<Int> = generateErrorCodes(),
) : HttpResponseInterceptor {
	private val template = HTTPTemplate(documentRoot, template)

	/**
	 * Create a new ErrorInterceptor form the given `documentRoot`, the `template` and the given `errorCodes`.
	 *
	 * @param documentRoot Document root
	 * @param template HTML template used to render the message content.
	 * @param errorCodes List of HTTP error status codes which shall be handled by this interceptor.
	 */

	override fun process(
		httpResponse: HttpResponse,
		httpContext: HttpContext,
	) {
		val statusLine = httpResponse.statusLine
		val statusCode = statusLine.statusCode

		if (errorCodes.contains(statusCode)) {
			logger.debug { "HTTP response intercepted" }
			val contentType = httpResponse.getFirstHeader(HeaderTypes.CONTENT_TYPE.fieldName())
			if (contentType != null) {
				// Intercept response with the content type "text/plain"
				if (contentType.value.contains(MimeType.TEXT_PLAIN.mimeType)) {
					// Remove old headers
					httpResponse.removeHeaders(HeaderTypes.CONTENT_TYPE.fieldName())
					httpResponse.removeHeaders(HeaderTypes.CONTENT_LENGTH.fieldName())

					// Read message body
					var content: String = readEntity(httpResponse.entity)
					// escape string to prevent script content to be injected into the template (XSS)
					content = HTMLUtils.escapeHtml(content) ?: content

					template.setProperty("%%%MESSAGE%%%", content)
				}
			} else {
				template.setProperty(
					"%%%MESSAGE%%%",
					when (statusCode) {
						400 -> I18N.strings.http_400.localized()
						401 -> I18N.strings.http_401.localized()
						403 -> I18N.strings.http_403.localized()
						404 -> I18N.strings.http_404.localized()
						405 -> I18N.strings.http_405.localized()
						415 -> I18N.strings.http_415.localized()
						500 -> I18N.strings.http_500.localized()
						else -> "no message"
					},
				)
			}

			template.setProperty("%%%TITLE%%%", "Error")
			val reason = statusLine.reasonPhrase
			template.setProperty("%%%HEADLINE%%%", reason)

			// Add new content
			httpResponse.entity = StringEntity(template.toString(), "UTF-8")
			httpResponse.addHeader(
				HeaderTypes.CONTENT_TYPE.fieldName(),
				MimeType.TEXT_HTML.mimeType + "; charset=utf-8",
			)
			httpResponse.addHeader(HeaderTypes.CONTENT_LENGTH.fieldName(), template.bytes.size.toString())
		}
	}

	private fun readEntity(httpEntity: HttpEntity): String {
		val baos = ByteArrayOutputStream()
		httpEntity.writeTo(baos)

		val type = ContentType.getOrDefault(httpEntity)
		return String(baos.toByteArray(), type.charset)
	}

	companion object {
		private fun generateErrorCodes(): List<Int> {
			val result = mutableListOf<Int>()
			for (i in 400..417) {
				result.add(i)
			}

			// additional codes used by the HttpAppPluginActionHandler
			result.add(423) // Locked
			result.add(429) // Too many requests

			for (i in 500..505) {
				result.add(i)
			}
			return result
		}
	}
}
