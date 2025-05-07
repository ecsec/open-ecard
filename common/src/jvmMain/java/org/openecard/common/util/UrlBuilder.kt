/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import javax.annotation.Nonnull
import javax.annotation.concurrent.Immutable

/**
 * Simple URL builder class.
 * The URL builders available (e.g. Jersey, Apache httpclient, ...) all add large dependencies. This class provides a
 * limited subset of those URL builders. <br></br>
 * The UrlBuilder is immutable meaning every function yields a new instance and leaves the original instance unmodified.
 *
 * @author Tobias Wich
 */
@Immutable
class UrlBuilder {
	private val encoder: UrlEncoder

	private var scheme: String
	private var userInfo: String?
	private var host: String
	private var port: Int
	private var path: String
	private val queryParams: HashMap<String?, String?>
	private var fragment: String?

	private constructor(
		encoder: UrlEncoder,
		scheme: String,
		userInfo: String,
		host: String,
		port: Int,
		path: String,
		queryParams: HashMap<String?, String?>,
		fragment: String,
	) {
		this.encoder = encoder
		this.scheme = scheme
		this.userInfo = userInfo
		this.host = host
		this.port = port
		this.path = path
		this.queryParams = queryParams
		this.fragment = fragment
	}

	private constructor(other: UrlBuilder) {
		this.encoder = other.encoder
		this.scheme = other.scheme
		this.userInfo = other.userInfo
		this.host = other.host
		this.port = other.port
		this.path = other.path
		this.queryParams = HashMap(other.queryParams)
		this.fragment = other.fragment
	}

	/**
	 * Constructs an URI object based on the values in the builder.
	 *
	 * @return New URI instance resembling the values from the builder.
	 * @throws URISyntaxException Thrown in case the values in the builder do not create a valid URI.
	 */
	fun build(): URI {
		val sb = StringBuilder()
		sb.append(scheme).append("://")
		if (userInfo != null) {
			sb.append(userInfo).append("@")
		}
		sb.append(host)
		if (port != -1) {
			sb.append(":").append(port)
		}
		sb.append(path)
		val it: Iterator<Map.Entry<String?, String?>> = queryParams.entries.iterator()
		if (it.hasNext()) {
			sb.append("?")
			while (it.hasNext()) {
				val element = it.next()
				val key = element.key
				val `val` = element.value
				sb.append(key)
				if (`val` != null) {
					sb.append("=").append(`val`)
				}
				if (it.hasNext()) {
					sb.append("&")
				}
			}
		}
		if (fragment != null) {
			sb.append("#").append(fragment)
		}

		return URI(sb.toString()).normalize()
	}

	/**
	 * Replaces the scheme (protocol) part of the URL.
	 *
	 * @param scheme Value to replace.
	 * @return A copy of the UrlBuilder with the scheme part modified.
	 */
	fun scheme(
		@Nonnull scheme: String,
	): UrlBuilder {
		val b = UrlBuilder(this)
		b.scheme = scheme
		return b
	}

	/**
	 * Replaces the user info part of the URL.
	 *
	 * @param userInfo Value to replace.
	 * @return A copy of the UrlBuilder with the user info part modified.
	 */
	fun userInfo(userInfo: String?): UrlBuilder {
		val b = UrlBuilder(this)
		b.userInfo = userInfo
		return b
	}

	/**
	 * Replaces the host part of the URL.
	 *
	 * @param host Value to replace.
	 * @return A copy of the UrlBuilder with the host part modified.
	 */
	fun host(host: String): UrlBuilder {
		val b = UrlBuilder(this)
		b.host = host
		return b
	}

	/**
	 * Replaces the port part of the URL.
	 *
	 * @param port Value to replace. A value of -1 disables the port component.
	 * @return A copy of the UrlBuilder with the port part modified.
	 */
	fun port(port: Int): UrlBuilder {
		val b = UrlBuilder(this)
		b.port = port
		return b
	}

	/**
	 * Replaces the path part of the URL.
	 *
	 * @param path Value to replace.
	 * @return A copy of the UrlBuilder with the path part modified.
	 */
	fun replacePath(path: String?): UrlBuilder {
		var path = path
		val b = UrlBuilder(this)
		if (path == null) {
			path = "/"
		}
		if (!path.startsWith("/")) {
			path = "/$path"
		}
		b.path = path
		return b
	}

	/**
	 * Adds a path segment to the current path of the URL.
	 * The segment can be composed of several segments. The segment must not start with /, as this is added in between
	 * when needed.
	 *
	 * @param segment The segment to add.
	 * @return A copy of the UrlBuilder with the path part modified.
	 */
	fun addPathSegment(segment: String): UrlBuilder {
		var segment = segment
		val b = UrlBuilder(this)
		// add / if none is currently present
		if (!path.endsWith("/") && !segment.startsWith("/")) {
			segment = "/$segment"
		}
		b.path = path + segment
		return b
	}

	/**
	 * Adds a query parameter to the URL.
	 *
	 * Any previously present value belonging to the given key gets overwritten.
	 *
	 * @param key Key of the parameter.
	 * @param value Value of the parameter.
	 * @return A copy of the UrlBuilder with added query parameter.
	 */
	fun queryParam(
		key: String,
		value: String?,
	): UrlBuilder = queryParam(key, value, true)

	/**
	 * Adds a query parameter to the URL.
	 * The parameter value is URL encoded as in forms (`application/x-www-form-urlencoded`).
	 *
	 * Any previously present value belonging to the given key gets overwritten.
	 *
	 * @param key Key of the parameter.
	 * @param value Value of the parameter.
	 * @return A copy of the UrlBuilder with added query parameter.
	 */
	fun queryParamUrl(
		key: String,
		value: String?,
	): UrlBuilder = queryParamUrl(key, value, true)

	/**
	 * Adds a query parameter to the URL.
	 *
	 * Any previously present value belonging to the given key gets overwritten only when the boolean flag is
	 * set.
	 *
	 * @param key Key of the parameter.
	 * @param value Value of the parameter.
	 * @param overwrite If `true`, overwrite any previously set query parameter with the given key.
	 * @return A copy of the UrlBuilder with added query parameter.
	 */
	fun queryParam(
		key: String,
		value: String?,
		overwrite: Boolean,
	): UrlBuilder {
		val b = UrlBuilder(this)
		if (overwrite || !queryParams.containsKey(key)) {
			b.addQueryInt(key, value)
		}
		return b
	}

	/**
	 * Adds a query parameter to the URL.
	 * The parameter value is URL encoded as in forms (`application/x-www-form-urlencoded`).
	 *
	 * Any previously present value belonging to the given key gets overwritten only when the boolean flag is
	 * set.
	 *
	 * @param key Key of the parameter.
	 * @param value Value of the parameter.
	 * @param overwrite If `true`, overwrite any previously set query parameter with the given key.
	 * @return A copy of the UrlBuilder with added query parameter.
	 */
	fun queryParamUrl(
		key: String,
		value: String?,
		overwrite: Boolean,
	): UrlBuilder {
		val b = UrlBuilder(this)
		if (overwrite || !queryParams.containsKey(key)) {
			b.addQueryInt(key, encoder.urlEncodeUrl(value))
		}
		return b
	}

	/**
	 * Removes all query parameters from the URL.
	 *
	 * @return A copy of the UrlBuilder with added query parameter.
	 */
	fun removeQueryParams(): UrlBuilder {
		val b = UrlBuilder(this)
		b.queryParams.clear()
		return b
	}

	/**
	 * Replaces the fragment part of the URL.
	 *
	 * @param fragment Value to replace.
	 * @return A copy of the UrlBuilder with the fragment part modified.
	 */
	fun fragment(fragment: String?): UrlBuilder {
		val b = UrlBuilder(this)
		b.fragment = encoder.encodeFragment(fragment)
		return b
	}

	private fun addQueryInt(
		k: String,
		v: String?,
	) {
		var k: String? = k
		var v = v
		k = encoder.encodeQueryParam(k)
		v = encoder.encodeQueryParam(v)
		queryParams[k] = v
	}

	companion object {
		/**
		 * Creates a UrlBuilder instance based on the given URI.
		 *
		 * @param baseUri The URI to use as a template for the builder that should be constructed.
		 * @return UriBuilder instance for the given URI.
		 */
		fun fromUrl(baseUri: URI): UrlBuilder {
			val encoder = UrlEncoder()

			val scheme = baseUri.scheme
			val userInfo = baseUri.rawUserInfo
			val host = baseUri.host
			val port = baseUri.port
			var path = baseUri.rawPath
			if (path == null || path.isEmpty()) {
				path = "/"
			}
			val queryParams = HashMap(HttpRequestLineUtils.transformRaw(baseUri.rawQuery))
			val fragment = baseUri.rawFragment

			return UrlBuilder(encoder, scheme, userInfo, host, port, path, queryParams, fragment)
		}

		/**
		 * Creates a UrlBuilder instance based on the given URL.
		 *
		 * @param baseUrl The URL to use as a template for the builder that should be constructed.
		 * @return UriBuilder instance for the given URL.
		 * @throws URISyntaxException Thrown in case the given URL could not be converted to the URI class and therefore is
		 * not a valid URI.
		 */
		fun fromUrl(baseUrl: URL): UrlBuilder = fromUrl(baseUrl.toURI())

		/**
		 * Creates a UrlBuilder instance based on the given URL.
		 *
		 * @param baseUrl The URL to use as a template for the builder that should be constructed.
		 * @return UriBuilder instance for the given URL.
		 * @throws URISyntaxException Thrown in case the given URL could not be converted to the URI class and therefore is
		 * not a valid URI.
		 */
		fun fromUrl(baseUrl: String): UrlBuilder = fromUrl(URI(baseUrl))
	}
}
