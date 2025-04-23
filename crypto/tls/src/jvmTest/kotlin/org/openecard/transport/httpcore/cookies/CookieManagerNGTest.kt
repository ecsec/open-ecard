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
package org.openecard.transport.httpcore.cookies

import org.openecard.httpcore.cookies.CookieManager
import org.testng.Assert
import org.testng.annotations.Test
import java.net.HttpCookie

/**
 *
 * @author Hans-Martin Haase
 */
class CookieManagerNGTest {
	private val manager = CookieManager()

	@Test
	fun testCookie1() {
		val cookies = HttpCookie.parse(COOKIE1)
		Assert.assertEquals(cookies.size, 1)
		val c = cookies[0]
		val name = c.name
		val value = c.value
		val domain = c.domain
		val maxage = c.maxAge
		val path = c.path
		Assert.assertEquals(name, "JSESSIONID")
		Assert.assertEquals(value, "123_84fsggasd")
		Assert.assertEquals(domain, "example.com")
		Assert.assertEquals(maxage, 30)
		Assert.assertEquals(path, "/")
	}

	@Test
	fun testCookie2() {
		val cookies = HttpCookie.parse(COOKIE2)
		Assert.assertEquals(cookies.size, 1)
		val c = cookies.get(0)
		val name = c.name
		val value = c.value
		val path = c.path
		val http = c.isHttpOnly
		val secure = c.secure
		// String expires = c.hasExpired();
		Assert.assertEquals(name, "WebSessionId")
		Assert.assertEquals(value, "258")
		Assert.assertEquals(http, true)
		Assert.assertEquals(secure, true)
		// Assert.assertEquals(expires, "Tue, 29-Mar-2014 19:30:42 GMT");
		Assert.assertNull(c.domain)
		Assert.assertEquals(c.maxAge, 0)
	}

	@Test
	@Throws(Exception::class)
	fun testAddCookie() {
		manager.addCookie("https://example.com", COOKIE1)
		val header = manager.getCookieHeaderValue("https://example.com:500/")
		Assert.assertEquals(header, "JSESSIONID=123_84fsggasd")
		manager.deleteAllCookies()
	}

	// @Test
	@Throws(Exception::class)
	fun testAddCookieSubdomain() {
		val COOKIE = "JSESSIONID=123_84fsggasd; Domain=test.example.com; Max-Age=30; Path=/"
		manager.addCookie("https://example.com", COOKIE)
		Assert.assertNull(manager.getCookieHeaderValue("https://example.com"))
		Assert.assertEquals(manager.getCookieHeaderValue("https://test.example.com"), "JSESSIONID=123_84fsggasd")
		Assert.assertEquals(manager.getCookieHeaderValue("https://bla.test.example.com"), "JSESSIONID=123_84fsggasd")
		manager.deleteAllCookies()
	}

	// @Test
	@Throws(Exception::class)
	fun testUpdate() {
		val COOKIE = "JSESSIONID=123_84fsggasd; Domain=example.com; Max-Age=30; Path=/"
		val C2 = "JSESSIONID=123_84fsggasd; Domain=example.com; Max-Age=0; Path=/"
		manager.addCookie("https://example.com", COOKIE)
		Assert.assertEquals(manager.getCookieHeaderValue("https://example.com"), "JSESSIONID=123_84fsggasd")
		manager.addCookie("https://example.com", C2)
		Assert.assertNull(manager.getCookieHeaderValue("https://example.com"))
	}
}

private const val COOKIE1 = "set-cookie: JSESSIONID=123_84fsggasd; domain=example.com; Max-Age=30; Path=/"
private const val COOKIE2 = "set-cookie: WebSessionId=258; Secure; HttpOnly; Expires=Tue, 29-Mar-2014 19:30:42 GMT"
