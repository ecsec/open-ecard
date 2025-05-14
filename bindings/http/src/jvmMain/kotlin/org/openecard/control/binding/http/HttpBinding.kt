/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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
package org.openecard.control.binding.http

import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponseInterceptor
import org.openecard.addon.AddonManager
import org.openecard.control.binding.http.common.DocumentRoot
import org.openecard.control.binding.http.handler.HttpAppPluginActionHandler
import org.openecard.control.binding.http.interceptor.CacheControlHeaderResponseInterceptor
import org.openecard.control.binding.http.interceptor.ErrorResponseInterceptor
import org.openecard.control.binding.http.interceptor.SecurityHeaderResponseInterceptor
import org.openecard.control.binding.http.interceptor.ServerHeaderResponseInterceptor
import org.openecard.control.binding.http.interceptor.StatusLineResponseInterceptor

/**
 * Implements a HTTP binding for the control interface.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
class HttpBinding(
	private val _port: Int,
	documentRootPath: String = "/www",
	listFile: String = "/www-files",
) {
	// Create document root
	private val documentRoot = DocumentRoot(documentRootPath, listFile)
	private var reqInterceptors: List<HttpRequestInterceptor>? = null
	private var respInterceptors: List<HttpResponseInterceptor>? = null
	private var service: HttpService? = null
	private var addonManager: AddonManager? = null

	fun setAddonManager(addonManager: AddonManager) {
		this.addonManager = addonManager
	}

	/**
	 * Creates a new HTTPBinding using the given port and document root.
	 *
	 * @param _port Port used for the binding. If the port is 0, then chose a port randomly.
	 * @param documentRootPath Path of the document root
	 * @param listFile
	 * @throws java.io.IOException If the document root cannot be read
	 * @throws Exception
	 */
	fun setRequestInterceptors(reqInterceptors: List<HttpRequestInterceptor>?) {
		this.reqInterceptors = reqInterceptors
	}

	fun setResponseInterceptors(respInterceptors: List<HttpResponseInterceptor>?) {
		this.respInterceptors = respInterceptors
	}

	@Throws(Exception::class)
	fun start() {
		val actualRequestInterceptor: List<HttpRequestInterceptor> = reqInterceptors ?: emptyList()
		reqInterceptors = actualRequestInterceptor

		val actualResponseInterceptor: List<HttpResponseInterceptor> =
			respInterceptors ?: listOf(
				StatusLineResponseInterceptor(),
				ErrorResponseInterceptor(documentRoot, "/templates/error.html"),
				ServerHeaderResponseInterceptor(),
				SecurityHeaderResponseInterceptor(),
				CacheControlHeaderResponseInterceptor(),
			)

		val currentAddonManager = addonManager
		if (currentAddonManager == null) {
			throw HttpServiceError("Trying to use uninitialized HttpBinding instance.")
		} else {
			val handler = HttpAppPluginActionHandler(currentAddonManager)
			service = HttpService(_port, handler, actualRequestInterceptor, actualResponseInterceptor)
			service!!.start()
		}
	}

	@Throws(Exception::class)
	fun stop() {
		service?.interrupt()
	}

	/**
	 * Returns the port number on which the HTTP binding is listening.
	 *
	 * @return Port
	 */
	val port: Int
		get() {
			return service?.port ?: _port
		}
}
