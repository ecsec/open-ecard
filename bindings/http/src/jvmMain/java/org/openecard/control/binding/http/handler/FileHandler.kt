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
package org.openecard.control.binding.http.handler

import org.apache.http.HttpRequest
import org.apache.http.HttpResponse
import org.apache.http.HttpStatus
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.entity.ContentType
import org.openecard.control.binding.http.common.*
import java.net.URI
import java.net.URL
import java.net.URLDecoder


/**
 *
 * @author Moritz Horsch
 */
class FileHandler(private val documentRoot: DocumentRoot) : ControlCommonHandler("/*") {

	override fun handle(httpRequest: HttpRequest): HttpResponse {
		// Return 404 Not Found in the default case
		val httpResponse = Http11Response(HttpStatus.SC_NOT_FOUND)
		val requestLine = httpRequest.requestLine

		if (requestLine.method == "GET") {
			val requestURI = URI.create(requestLine.uri)

			val filePath = documentRoot.getFile(URLDecoder.decode(requestURI.path, "UTF-8"))
			if (filePath != null) {
				// Handle file
				logger.debug{"Handle file request"}
				handleFile(httpResponse, filePath)
			} else {
				logger.debug{"The DocumentRoot does not contain the URI: ${requestURI.path}"}
			}
		} else {
			// Return 405 Method Not Allowed
			httpResponse.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED)
		}

		return httpResponse
	}

	private fun handleFile(httpResponse: Http11Response, file: URL) {
		val fileName = file.toString()
		val fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1)
		val mimeType: MimeType? = MimeType.fromFilenameExtension(fileExtension)
		val typeName = mimeType?.mimeType ?: MimeType.TEXT_PLAIN.mimeType

		httpResponse.setStatusCode(HttpStatus.SC_OK)
		val entity = BasicHttpEntity()
		entity.content = file.openStream()
		entity.setContentType(ContentType.create(typeName, "UTF-8").toString())
		httpResponse.entity = entity
	}
}
