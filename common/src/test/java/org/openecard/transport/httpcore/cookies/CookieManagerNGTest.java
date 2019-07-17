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

package org.openecard.transport.httpcore.cookies;

import java.net.HttpCookie;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Hans-Martin Haase
 */
public class CookieManagerNGTest {

    private static final String COOKIE1 = "set-cookie: JSESSIONID=123_84fsggasd; domain=example.com; Max-Age=30; Path=/";
    private static final String COOKIE2 = "set-cookie: WebSessionId=258; Secure; HttpOnly; Expires=Tue, 29-Mar-2014 19:30:42 GMT";

    private final CookieManager manager = new CookieManager();

    @Test
    public void testCookie1() {
	List<HttpCookie> cookies = HttpCookie.parse(COOKIE1);
	Assert.assertEquals(cookies.size(), 1);
	HttpCookie c = cookies.get(0);
	String name = c.getName();
	String value = c.getValue();
	String domain = c.getDomain();
	long maxage = c.getMaxAge();
	String path = c.getPath();
	Assert.assertEquals(name, "JSESSIONID");
	Assert.assertEquals(value, "123_84fsggasd");
	Assert.assertEquals(domain, "example.com");
	Assert.assertEquals(maxage, 30);
	Assert.assertEquals(path, "/");
    }

    @Test
    public void testCookie2() {
	List<HttpCookie> cookies = HttpCookie.parse(COOKIE2);
	Assert.assertEquals(cookies.size(), 1);
	HttpCookie c = cookies.get(0);
	String name = c.getName();
	String value = c.getValue();
	String path = c.getPath();
	boolean http = c.isHttpOnly();
	boolean secure = c.getSecure();
	//String expires = c.hasExpired();
	Assert.assertEquals(name, "WebSessionId");
	Assert.assertEquals(value, "258");
	Assert.assertEquals(http, true);
	Assert.assertEquals(secure, true);
	//Assert.assertEquals(expires, "Tue, 29-Mar-2014 19:30:42 GMT");
	Assert.assertNull(c.getDomain());
	Assert.assertEquals(c.getMaxAge(), 0);
    }

    @Test
    public void testAddCookie() throws Exception {
	manager.addCookie("https://example.com", COOKIE1);
	String header = manager.getCookieHeaderValue("https://example.com:500/");
	Assert.assertEquals(header, "JSESSIONID=123_84fsggasd");
	manager.deleteAllCookies();
    }

    public void testAddCookieSubdomain() throws Exception {
	String COOKIE = "JSESSIONID=123_84fsggasd; Domain=test.example.com; Max-Age=30; Path=/";
	manager.addCookie("https://example.com", COOKIE);
	Assert.assertNull(manager.getCookieHeaderValue("https://example.com"));
	Assert.assertEquals(manager.getCookieHeaderValue("https://test.example.com"), "JSESSIONID=123_84fsggasd");
	Assert.assertEquals(manager.getCookieHeaderValue("https://bla.test.example.com"), "JSESSIONID=123_84fsggasd");
	manager.deleteAllCookies();
    }

    public void testUpdate() throws Exception {
	String COOKIE = "JSESSIONID=123_84fsggasd; Domain=example.com; Max-Age=30; Path=/";
	String C2 = "JSESSIONID=123_84fsggasd; Domain=example.com; Max-Age=0; Path=/";
	manager.addCookie("https://example.com", COOKIE);
	Assert.assertEquals(manager.getCookieHeaderValue("https://example.com"), "JSESSIONID=123_84fsggasd");
	manager.addCookie("https://example.com", C2);
	Assert.assertNull(manager.getCookieHeaderValue("https://example.com"));
    }

}
