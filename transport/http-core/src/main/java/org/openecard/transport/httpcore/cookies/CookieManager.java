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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The class represents a Cookie manager used to manage cookies according to RFC 6265.
 * <br>
 * <br>
 * This implementation provides an in memory cookie storage but may be also used for on disk cookie storage with the
 * {@link CookieManager#CookieManager(java.util.Map) } constructor which may consume a custom map implementation which
 * may be capable for the on disk storage.
 * <br>
 * <br>
 * The implementation is protected against cookie overflow. The implementation is restricted to store 3000 cookies at all
 * and 50 cookies per domain.
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class CookieManager {

    private static final Logger LOG = LoggerFactory.getLogger(CookieManager.class);

    private final int maxCookiesPerDomain = 50;
    private final int maxCookies = 3000;
    private final Map<String, List<HttpCookie>> cookieMap;

    private int currentCookieCount = 0;

    /**
     * Creates a default CookieManager instance which provides an in memory cookie management.
     */
    public CookieManager() {
	cookieMap = new HashMap<>();
    }

    /**
     * Creates a CookieManager instance with a custom Map implementation which is used to store the cookies.
     * <br>
     * <br>
     * This constructor may be used in case there is a need for an on disk storage of the cookies. The storage of the
     * cookies on disk have to be handled by the {@link Map} implementation.
     *
     * @param cookieMap {@link Map} implementation to use for the cookie storage.
     */
    public CookieManager(@Nonnull Map<String, List<HttpCookie>> cookieMap) {
	this.cookieMap = cookieMap;
    }

    /**
     * Adds a cookie to the manager instance.
     *
     * @param domain Address of the caller which want to set the cookie.
     * @param cookieHeaderValue The Set-Cookie header.
     * @throws CookieException If the cookie can't be added to the storage.
     */
    public void addCookie(@Nonnull String domain, @Nonnull String cookieHeaderValue) throws CookieException {
	List<HttpCookie> cookies = HttpCookie.parse(cookieHeaderValue);
	if ((cookies.size() + currentCookieCount) <= maxCookies) {
	    for (HttpCookie cookie : cookies) {
		try {
		    String domainKey = createDomainKey(domain, cookie);
		    addCookie(domainKey, cookie);
		} catch(MalformedURLException ex) {
		    String msg = "Invalid value (%s) in the \"domain\" parameter received.";
		    msg = String.format(msg, domain);
		    LOG.error(msg, ex);
		    throw new CookieException(msg, ex);
		}
	    }

	} else {
	    String msg = "The cookie storage is full.";
	    LOG.error(msg);
	    throw new CookieException(msg);
	}
    }

    private void addCookie(@Nonnull String domainKey, HttpCookie cookie) throws CookieException {
	if (! update(domainKey, cookie)) {

	    List<HttpCookie> cookies = cookieMap.get(domainKey);
	    if (cookies == null) {
		cookies = new ArrayList<>();
	    }

	    if (cookies.size() == maxCookiesPerDomain) {
		String msg = "Maximal number of cookies per domain reached.";
		LOG.error(msg);
		throw new CookieException(msg);
	    }

	    if (LOG.isDebugEnabled()) {
		String msg = "Setting cookie %s for domain %s.";
		msg = String.format(msg, cookie, domainKey);
		LOG.debug(msg);
	    }
	    cookies.add(cookie);
	    cookieMap.put(domainKey, cookies);
	    currentCookieCount++;
	}
    }

    /**
     * Deletes all cookies from the store.
     */
    public void deleteAllCookies() {
	cookieMap.clear();
	currentCookieCount = 0;
    }

    /**
     * Deletes the specified cookie from the store.
     *
     * @param domain URL which identifies the cookies of a domain in the storage.
     * @param name Name of the cookie.
     * @throws CookieException If the given {@code domain} is no valid URL.
     */
    public void deleteCookie(@Nonnull String domain, @Nonnull String name) throws CookieException {
	URL url;
	try {
	    url = new URL(domain);
	} catch(MalformedURLException ex) {
	    String msg = "The \"domain\" parameter contains an invalid URL.";
	    throw new CookieException(msg, ex);
	}

	List<HttpCookie> cookies = cookieMap.get(url.getHost());
	if (cookies != null && ! cookies.isEmpty()) {
	    for (int i = 0; i < cookies.size(); i++) {
		HttpCookie c = cookies.get(i);
		if (c.getName().equals(name)) {
		    cookies.remove(i);
		    currentCookieCount--;
		    break;
		}
	    }
	}
    }

    /**
     * Creates a string containing all the cookies set by the given domain.
     *
     * @param domain The domain for which the cookies shall be returned.
     * @return A String containing all cookies (according to RFC 6265) registered for {@code domain} or {@code NULL} if
     * no cookies exist for {@code domain}.
     * @throws CookieException If the given {@code domain} parameter is an invalid URL.
     */
    @Nullable
    public String getCookieHeaderValue(@Nonnull String domain) throws CookieException {
	try {
	    Set<String> keySet = cookieMap.keySet();
	    List<HttpCookie> domainCookies;
	    List<HttpCookie> usableCookies = new ArrayList<>();
	    URL domAsURL = new URL(domain);

	    for (String key : keySet) {
		if (domAsURL.getHost().endsWith(key)) {
		    domainCookies = cookieMap.get(key);
		    ArrayList<HttpCookie> cleanList = new ArrayList<>();
		    if (domainCookies != null && domainCookies.size() > 0) {
			for (HttpCookie c : domainCookies) {
			    // according to RFC 6265 it is not allowed to return the cookie to a subdomain or so in case
			    // there is no Domain attribute in the cookie. This mean if we got a cookie from example.com it
			    // is not allowed to return the cookie to www.example.com or foo.example.com just to example.com.
			    if (c.getDomain() == null || c.getDomain().isEmpty()) {
				if (key.equals(domAsURL.getHost())) {
				    if (c.getPath() == null || c.getPath().isEmpty()) {
					usableCookies.add(c);
					cleanList.add(c);
				    } else {
					if (domAsURL.getPath().startsWith(c.getPath())) {
					    usableCookies.add(c);
					    cleanList.add(c);
					}
				    }
				}
			    } else {
				if (c.getPath() == null || c.getPath().isEmpty() || domAsURL.getPath().startsWith(c.getPath())) {
				    usableCookies.add(c);
				    cleanList.add(c);
				}
			    }
			}
		    }

		    for (HttpCookie c : cleanList) {
			clean(key, c);
		    }
		}
	    }

	    StringBuilder headerValue = new StringBuilder();
	    for (HttpCookie c : usableCookies) {
		headerValue.append(c.getName());
		headerValue.append("=");
		headerValue.append(c.getValue());
		headerValue.append("; ");
	    }

	    if (headerValue.length() > 0) {
		int lastSemicolon = headerValue.lastIndexOf("; ");
		headerValue.delete(lastSemicolon, lastSemicolon + 2);
		return headerValue.toString();
	    }
	} catch(MalformedURLException ex) {
	    String msg = "The given value (%s) of the \"domain\" parameter is not valid URL.";
	    msg = String.format(msg, domain);
	    LOG.error(msg, ex);
	    throw new CookieException(msg, ex);
	}
	
	return null;
    }

    /**
     * Creates the key which is used to store the list of cookies for a domain.
     *
     * @param domain Full server name.
     * @param cookie A cookie which may contain a {@code Domain} attribute.
     * @return {@code domain} in case the cookie does not contain a {@code Domain} attribute or the content of the
     * {@code Domain} attribute in case it is set.
     * @throws MalformedURLException if the domain parameter is no valid URI.
     */
    @Nonnull
    private String createDomainKey(@Nonnull String domain, @Nonnull HttpCookie cookie) throws MalformedURLException {
	// just to check whether we have a valid url.
	URL url = new URL(domain);
	String domainAttr = cookie.getDomain();
	if (! (domainAttr == null || domainAttr.isEmpty())) {
	    if (domainAttr.startsWith(".")) {
		domainAttr = domainAttr.substring(1);
	    }
	    return domainAttr;
	} else {
	    return url.getHost();
	}
    }

    /**
     * Removes the specified cookie from the storage.
     *
     * If the cookie was the last one for a key than the map entry for the domain is removed.
     *
     * @param key Domain key which identifies the list of cookies containing {@code c}.
     * @param c {@link Cookie} object to delete.
     */
    private void clean(@Nonnull String key, @Nonnull HttpCookie c) {
	if (c.hasExpired()) {
	    List<HttpCookie> domainCookies = cookieMap.get(key);
	    domainCookies.remove(c);
	    currentCookieCount--;
	    if (domainCookies.isEmpty()) {
		cookieMap.remove(key);
	    }
	}
    }

    /**
     * Updates the cookie.
     * <br>
     * This means expiration time update.
     *
     * @param domainKey The key which addresses the cookie, may be a full server address or just a domain.
     * @param cookie The new cookie which may replace an old one.
     * @return {@code TRUE} if an update was performed else {@code FALSE}.
     */
    private boolean update(String domainKey, HttpCookie cookie) {
	List<HttpCookie> cookies = cookieMap.get(domainKey);
	if (cookies == null || cookies.isEmpty()) {
	    return false;
	}

	for (HttpCookie c : cookies) {
	    if (c.equals(cookie)) {
		clean(domainKey, c);
		if (! cookie.hasExpired()) {
		    cookies.add(cookie);
		}
		return true;
	    }
	}

	return false;
    }

}
