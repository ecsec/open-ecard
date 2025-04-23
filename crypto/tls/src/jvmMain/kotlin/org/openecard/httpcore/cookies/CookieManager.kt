/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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
 ***************************************************************************/
package org.openecard.httpcore.cookies

import io.github.oshai.kotlinlogging.KotlinLogging
import java.net.HttpCookie
import java.net.MalformedURLException
import java.net.URI

private val LOG = KotlinLogging.logger { }

/**
 * The class represents a Cookie manager used to manage cookies according to RFC 6265.
 * <br></br>
 * <br></br>
 * This implementation provides an in memory cookie storage but may be also used for on disk cookie storage with the
 * [CookieManager.CookieManager] constructor which may consume a custom map implementation which
 * may be capable for the on disk storage.
 * <br></br>
 * <br></br>
 * The implementation is protected against cookie overflow. The implementation is restricted to store 3000 cookies at all
 * and 50 cookies per domain.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
class CookieManager(
	private val cookieMap: MutableMap<String, MutableList<HttpCookie>> = mutableMapOf(),
) {
	private val maxCookiesPerDomain = 50
	private val maxCookies = 3000

	private var currentCookieCount = 0

	/**
	 * Adds a cookie to the manager instance.
	 *
	 * @param domain Address of the caller which want to set the cookie.
	 * @param cookieHeaderValue The Set-Cookie header.
	 * @throws CookieException If the cookie can't be added to the storage.
	 */
	@Throws(CookieException::class)
	fun addCookie(
		domain: String,
		cookieHeaderValue: String,
	) {
		val cookies = HttpCookie.parse(cookieHeaderValue)
		if ((cookies.size + currentCookieCount) <= maxCookies) {
			for (cookie in cookies) {
				try {
					val domainKey = createDomainKey(domain, cookie)
					addCookie(domainKey, cookie)
				} catch (ex: MalformedURLException) {
					var msg = "Invalid value (%s) in the \"domain\" parameter received."
					msg = String.format(msg, domain)
					LOG.error(ex) { msg }
					throw CookieException(msg, ex)
				}
			}
		} else {
			val msg = "The cookie storage is full."
			LOG.error { msg }
			throw CookieException(msg)
		}
	}

	@Throws(CookieException::class)
	private fun addCookie(
		domainKey: String,
		cookie: HttpCookie,
	) {
		if (!update(domainKey, cookie)) {
			var cookies = cookieMap[domainKey] ?: mutableListOf()

			if (cookies.size == maxCookiesPerDomain) {
				val msg = "Maximal number of cookies per domain reached."
				LOG.error { msg }
				throw CookieException(msg)
			}

			if (LOG.isDebugEnabled()) {
				var msg = "Setting cookie %s for domain %s."
				msg = String.format(msg, cookie, domainKey)
				LOG.debug { msg }
			}
			cookies.add(cookie)
			cookieMap.put(domainKey, cookies)
			currentCookieCount++
		}
	}

	/**
	 * Deletes all cookies from the store.
	 */
	fun deleteAllCookies() {
		cookieMap.clear()
		currentCookieCount = 0
	}

	/**
	 * Deletes the specified cookie from the store.
	 *
	 * @param domain URL which identifies the cookies of a domain in the storage.
	 * @param name Name of the cookie.
	 * @throws CookieException If the given `domain` is no valid URL.
	 */
	@Throws(CookieException::class)
	fun deleteCookie(
		domain: String,
		name: String,
	) {
		val url =
			try {
				URI(domain)
			} catch (ex: MalformedURLException) {
				val msg = "The \"domain\" parameter contains an invalid URL."
				throw CookieException(msg, ex)
			}

		val cookies = cookieMap[url.host]
		if (cookies != null && !cookies.isEmpty()) {
			for (i in cookies.indices) {
				val c = cookies[i]
				if (c.name == name) {
					cookies.removeAt(i)
					currentCookieCount--
					break
				}
			}
		}
	}

	/**
	 * Creates a string containing all the cookies set by the given domain.
	 *
	 * @param domain The domain for which the cookies shall be returned.
	 * @return A String containing all cookies (according to RFC 6265) registered for `domain` or `NULL` if
	 * no cookies exist for `domain`.
	 * @throws CookieException If the given `domain` parameter is an invalid URL.
	 */
	@Throws(CookieException::class)
	fun getCookieHeaderValue(domain: String): String? {
		try {
			val keySet = cookieMap.keys
			val usableCookies = mutableListOf<HttpCookie>()
			val domAsURL = URI(domain)

			for (key in keySet) {
				if (domAsURL.host.endsWith(key)) {
					val domainCookies = cookieMap[key]
					val cleanList = mutableListOf<HttpCookie>()
					if (domainCookies != null && domainCookies.isNotEmpty()) {
						for (c in domainCookies) {
							// according to RFC 6265 it is not allowed to return the cookie to a subdomain or so in case
							// there is no Domain attribute in the cookie. This mean if we got a cookie from example.com it
							// is not allowed to return the cookie to www.example.com or foo.example.com just to example.com.
							if (c.domain == null || c.domain.isEmpty()) {
								if (key == domAsURL.host) {
									if (c.path == null || c.path.isEmpty()) {
										usableCookies.add(c)
										cleanList.add(c)
									} else {
										if (domAsURL.path.startsWith(c.path)) {
											usableCookies.add(c)
											cleanList.add(c)
										}
									}
								}
							} else {
								if (c.path == null || c.path.isEmpty() || domAsURL.path.startsWith(c.path)) {
									usableCookies.add(c)
									cleanList.add(c)
								}
							}
						}
					}

					for (c in cleanList) {
						clean(key, c)
					}
				}
			}

			val headerValue = StringBuilder()
			for (c in usableCookies) {
				headerValue.append(c.name)
				headerValue.append("=")
				headerValue.append(c.value)
				headerValue.append("; ")
			}

			if (headerValue.isNotEmpty()) {
				val lastSemicolon = headerValue.lastIndexOf("; ")
				headerValue.delete(lastSemicolon, lastSemicolon + 2)
				return headerValue.toString()
			}
		} catch (ex: MalformedURLException) {
			var msg = "The given value (%s) of the \"domain\" parameter is not valid URL."
			msg = String.format(msg, domain)
			LOG.error(ex) { msg }
			throw CookieException(msg, ex)
		}

		return null
	}

	/**
	 * Creates the key which is used to store the list of cookies for a domain.
	 *
	 * @param domain Full server name.
	 * @param cookie A cookie which may contain a `Domain` attribute.
	 * @return `domain` in case the cookie does not contain a `Domain` attribute or the content of the
	 * `Domain` attribute in case it is set.
	 * @throws MalformedURLException if the domain parameter is no valid URI.
	 */
	@Throws(MalformedURLException::class)
	private fun createDomainKey(
		domain: String,
		cookie: HttpCookie,
	): String {
		// just to check whether we have a valid url.
		val url = URI(domain)
		var domainAttr = cookie.domain
		if (!(domainAttr == null || domainAttr.isEmpty())) {
			if (domainAttr.startsWith(".")) {
				domainAttr = domainAttr.substring(1)
			}
			return domainAttr
		} else {
			return url.host
		}
	}

	/**
	 * Removes the specified cookie from the storage.
	 *
	 * If the cookie was the last one for a key than the map entry for the domain is removed.
	 *
	 * @param key Domain key which identifies the list of cookies containing `c`.
	 * @param c [Cookie] object to delete.
	 */
	private fun clean(
		key: String,
		c: HttpCookie,
	) {
		if (c.hasExpired()) {
			cookieMap[key]?.let { domainCookies ->
				domainCookies.remove(c)
				currentCookieCount--
				if (domainCookies.isEmpty()) {
					cookieMap.remove(key)
				}
			}
		}
	}

	/**
	 * Updates the cookie.
	 * <br></br>
	 * This means expiration time update.
	 *
	 * @param domainKey The key which addresses the cookie, may be a full server address or just a domain.
	 * @param cookie The new cookie which may replace an old one.
	 * @return `TRUE` if an update was performed else `FALSE`.
	 */
	private fun update(
		domainKey: String,
		cookie: HttpCookie,
	): Boolean {
		val cookies = cookieMap[domainKey]
		if (cookies == null || cookies.isEmpty()) {
			return false
		}

		for (c in cookies) {
			if (c == cookie) {
				clean(domainKey, c)
				if (!cookie.hasExpired()) {
					cookies.add(cookie)
				}
				return true
			}
		}

		return false
	}
}
