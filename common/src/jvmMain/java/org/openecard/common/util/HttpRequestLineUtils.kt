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
package org.openecard.common.util

import java.io.UnsupportedEncodingException
import java.net.URLDecoder

/**
 * Utility class to transform various aspects of an HTTP request line into more usable data structures.
 *
 * @author Tobias Wich
 */
object HttpRequestLineUtils {
	/**
	 * Transform query parameters into a java map. The parameters are not decoded, but taken as is. The query string has
	 * the form <pre>key(=value)?&amp;key((=value)?)*</pre>. If a key does not have a value, null is taken as value.
	 *
	 * @param queryStr Query string as found in the HTTP request line.
	 * @return Map with key value pairs of the query parameters.
	 */
	@JvmStatic
	fun transformRaw(queryStr: String?): Map<String, String> {
		val result = HashMap<String, String>()

		if (queryStr != null) {
			val queries = queryStr.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			for (query in queries) {
				// everything in front of the equal sign is the key
				// everything behind the equal sign is the value
				val first = query.indexOf('=')
				if (first == -1) {
					result[query] = ""
				} else {
					val key = query.substring(0, first)
					val value = query.substring(first + 1, query.length)
					result[key] = value
				}
			}
		}

		return result
	}

	/**
	 * Transform query parameters into a java map and URL decode the values.
	 * The parameters are not decoded, but taken as is. The query string has the form
	 * <pre>key(=value)?&amp;key((=value)?)*</pre>. If a key does not have a value, null is taken as value. The resulting
	 * values are encoded according to the given encoding
	 *
	 * @param queryStr Query string as found in the HTTP request line.
	 * @param encoding Encoding used in the [URLDecoder.decode] function.
	 * @return Map with key value pairs of the query parameters.
	 * @throws UnsupportedEncodingException Thrown if the strings decoded from the URL encoded value have a different
	 * encoding than the one defined in this function.

	 * Simplification of [.transform] with the default encoding UTF-8.
	 *
	 * @param queryStr Query string as found in the HTTP request line.
	 * @return Map with key value pairs of the query parameters.
	 * @throws UnsupportedEncodingException Thrown if the strings decoded from the URL encoded value have a different
	 * encoding than UTF-8.
	 */
	fun transform(
		queryStr: String?,
		encoding: String = "UTF-8",
	): Map<String?, String?> {
		// copy the raw strings
		val resultRaw = transformRaw(queryStr)
		val result = HashMap<String?, String?>()
		for (next in resultRaw.entries) {
			var k: String? = next.key
			var v: String? = next.value
			// URL decode both values
			k = decodeValue(k, encoding)
			v = decodeValue(v, encoding)
			result[k] = v
		}

		return result
	}

	private fun decodeValue(
		v: String?,
		encoding: String,
	): String? {
		var v = v
		if (v != null) {
			// handle + special, because the decoder does not perform this
			v = v.replace("+", " ")
			v = URLDecoder.decode(v, encoding)
		}
		return v
	}
}
