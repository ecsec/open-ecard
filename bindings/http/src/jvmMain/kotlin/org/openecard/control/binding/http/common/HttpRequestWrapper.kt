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
package org.openecard.control.binding.http.common

import org.apache.http.HttpEntityEnclosingRequest
import org.apache.http.HttpRequest
import org.apache.http.util.EntityUtils
import org.openecard.common.util.HttpRequestLineUtils
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URI

/**
 * HttpRequest wrapper to parse request parameters.
 *
 * @author Benedikt Biallowons
 */
class HttpRequestWrapper(
	private val request: HttpRequest,
) {
	private val parameterMap: MutableMap<String, MutableList<String>> =
		mutableMapOf()

	/**
	 * Create HttpRequestWrapper instance.
	 *
	 * @param request HttpRequest
	 */
	init {
		parseRequestParameters()
	}

	val requestParameters: Map<String, MutableList<String>>?
		/**
		 * Returns a key value map of available parameters, null otherwise.
		 *
		 * @return parameter map
		 */
		get() {
			if (parameterMap.isNotEmpty()) {
				return this.parameterMap
			}

			return null
		}

	/**
	 * Returns a list of parameter values if the given parameter name is found,
	 * null otherwise.
	 *
	 * @param parameterName the request parameter name
	 * @return list of parameter values or null
	 */
	fun getRequestParameter(parameterName: String): List<String>? {
		if (parameterMap.containsKey(parameterName)) {
			return parameterMap[parameterName]
		}

		return null
	}

	/**
	 * Returns true if the given parameter name is found, false otherwise.
	 *
	 * @param parameterName parameter name
	 * @return true or false
	 */
	fun hasRequestParameter(parameterName: String): Boolean = parameterMap.containsKey(parameterName)

	/**
	 * Returns a key value map of available parameters. The map can be empty but
	 * never null.
	 *
	 * @return a parameter map
	 */
	private fun parseRequestParameters(): Map<String, MutableList<String>> {
		val method = request.requestLine.method

		if (method == Http11Method.GET.methodString) {
			// decoded query string
			val query = URI.create(request.requestLine.uri).rawQuery ?: return parameterMap
			var queries =
				try {
					HttpRequestLineUtils.transform(query)
				} catch (ex: UnsupportedEncodingException) {
					HttpRequestLineUtils.transformRaw(query)
				}

			for ((name, value) in queries) {
				if (parameterMap.containsKey(name)) {
					parameterMap[name]!!.add(value)
				} else {
					val values: MutableList<String> = mutableListOf()
					values.add(value)
					parameterMap[name] = values
				}
			}
		} else if (method == Http11Method.POST.methodString &&
			request is HttpEntityEnclosingRequest
		) {
			val entity = request.entity
			val entityContent: String
			try {
				entityContent = EntityUtils.toString(entity, "UTF-8")
				// TODO: implement POST request parameter parsing
			} catch (e: IOException) {
			}
		}

		return parameterMap
	}
}
