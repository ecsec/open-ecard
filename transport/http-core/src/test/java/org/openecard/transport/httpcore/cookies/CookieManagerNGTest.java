/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

import org.openecard.transport.httpcore.cookies.CookieManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Hans-Martin Haase
 */
public class CookieManagerNGTest {

    private static final Pattern pat = Pattern.compile("(?<name>.+?)=(?<value>.+?)((; Path=(?<path>.+?))|(; "
	    + "Domain=(?<domain>.+?))|(; (?<httponly>HttpOnly))|(; (?<secure>Secure))|(; Expires=(?<expires>.+?))|"
	    + "(; Max-Age=(?<maxage>\\d+?)))*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    private static final String COOKIE1 = "JSESSIONID=123_84fsggasd; domain=example.com; Max-Age=30; Path=/";
    private static final String COOKIE2 = "WebSessionId=258; Secure; HttpOnly; Expires=Tue, 29-Mar-2014 19:30:42 GMT";

    private final CookieManager manager = new CookieManager();

    @Test
    public void testCookie1() {
	Matcher matcher = pat.matcher(COOKIE1);
	boolean match = matcher.matches();
	Assert.assertTrue(match);
	String name = matcher.group("name");
	String value = matcher.group("value");
	String domain = matcher.group("domain");
	String maxage = matcher.group("maxage");
	String path = matcher.group("path");
	Assert.assertEquals(name, "JSESSIONID");
	Assert.assertEquals(value, "123_84fsggasd");
	Assert.assertEquals(domain, "example.com");
	Assert.assertEquals(maxage, "30");
	Assert.assertEquals(path, "/");
    }

    @Test
    public void testCookie2() {
	Matcher matcher = pat.matcher(COOKIE2);
	boolean match = matcher.matches();
	Assert.assertTrue(match);
	String name = matcher.group("name");
	String value = matcher.group("value");
	String http = matcher.group("httponly");
	String secure = matcher.group("secure");
	String expires = matcher.group("expires");
	Assert.assertEquals(name, "WebSessionId");
	Assert.assertEquals(value, "258");
	Assert.assertEquals(http, "HttpOnly");
	Assert.assertEquals(secure, "Secure");
	Assert.assertEquals(expires, "Tue, 29-Mar-2014 19:30:42 GMT");
	Assert.assertNull(matcher.group("domain"));
	Assert.assertNull(matcher.group("maxage"));
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
