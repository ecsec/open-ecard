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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nonnull;


/**
 * Class implementing a Cookie according to RFC 6265.
 *
 * @author Hans-Martin Haase
 */
public class Cookie {

    private final String name;
    private final String value;
    
    private long creationTime;
    private boolean httpOnly;
    private boolean secure;
    private Date expires;
    private long maxAge = -1;
    private String domain = "";
    private String path = "";

    /**
     * Creates a new Cookie object with the given name and the given value.
     *
     * @param cookieName Name of the cookie to create.
     * @param cookieValue Value of the cookie to create.
     * @throws NullPointerException If one of the parameters is {@code NULL}.
     */
    public Cookie(@Nonnull String cookieName, @Nonnull String cookieValue) {
	if (cookieName == null) {
	    throw new NullPointerException("NULL value not allowed for the cookie name.");
	}

	if (cookieValue == null) {
	    throw new NullPointerException("NULL value not allowed for the cookie value");
	}
	name = cookieName;
	value = cookieValue;
	creationTime = System.currentTimeMillis();
    }

    /**
     * Sets the {@code httpOnly} attribute of the cookie.
     *
     * @param httpOnly Boolean value indicating that the cookie is httpOnly according to RFC 6265 sec. 4.1.2.6.
     */
    public void setHttpOnly(boolean httpOnly) {
	this.httpOnly = httpOnly;
    }

    /**
     * Sets the {@code secure} attribute of the cookie.
     *
     * @param isSecure Boolean value indicating that the cookie is secure according to RFC 6265 sec. 4.1.2.5.
     */
    public void setSecure(boolean isSecure) {
	this.secure = isSecure;
    }

    /**
     * Sets the {@code Expires} attribute of the cookie.
     *
     * @param dateString The expiration date of the cookie in the format {@code EEE, dd-MMM-yyyy HH:mm:ss zzz}.
     * @throws CookieException If {@code dateString} has the wrong format.
     */
    public void setExpires(@Nonnull String dateString) throws CookieException {
	DateFormat format = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz");
	try {
	    expires = format.parse(dateString);
	} catch (ParseException ex) {
	    throw new CookieException("The date string has a wrong format and can not be parsed.", ex);
	}
    }

    /**
     * Sets the {@code Max-Age} attribute of the cookie.
     *
     * @param maxAgeSeconds String representing the maximal lifetime of the cookie in seconds.
     */
    public void setMaxAge(@Nonnull String maxAgeSeconds) {
	long lifeTimeMilliSeconds = Long.parseLong(maxAgeSeconds) * 1000;
	maxAge = creationTime + lifeTimeMilliSeconds;
    }

    /**
     * Sets the {@code Domain} attribute of the cookie.
     *
     * @param domain The domain which is covered by this cookie.
     */
    public void setDomain(@Nonnull String domain) {
	this.domain = domain;
    }

    /**
     * Sets the {@code Path} attribute of the cookie.
     *
     * @param path The resource path of the domain for which the cookie is valid.
     */
    public void setPath(@Nonnull String path) {
	this.path = path;
    }

    /**
     * Get the name of the cookie.
     *
     * @return The name of the Cookie.
     */
    @Nonnull
    public String getName() {
	return name;
    }

    /**
     * Get the value of the cookie.
     *
     * @return The value of the cookie.
     */
    @Nonnull
    public String getValue() {
	return value;
    }

    /**
     * Get the domain for which the cookie is valid.
     *
     * @return The domain name which is covered by the cookie or the empty string if the {@code Domain} attribute was
     * not set.
     */
    @Nonnull
    public String getDomain() {
	return domain;
    }

    /**
     * Get the resource path which is covered by this cookie.
     *
     * @return The resource path covered by this cookie or {@code NULL} if no {@code Path} attribute was set.
     */
    @Nonnull
    public String getPath() {
	return path;
    }

    /**
     * Indicates whether the cookie shall be access only via HTTP.
     *
     * @return {@code TRUE} if the cookie shall be access by HTTP only else {@code FALSE}.
     */
    public boolean isHttpOnly() {
	return httpOnly;
    }

    /**
     * Indicates whether the cookie is expired or not.
     * <br/>
     * <br/>
     * Note: This method evaluates the {@code Max-Age} attribute and {@code Expires} attribute while the {@code Max-Age}
     * attribute has precedence before the {@code Expires} attribute. If none of the above attributes is set than {@code
     * FALSE} so the cookie is hold for the complete session.
     *
     * @return {@code TRUE} if the cookie is expired else {@code FALSE}.
     */
    public boolean isExpired() {
	if (maxAge != -1 || expires != null) {
	    // according to RFC 6265 maxAge has precedence
	    if (maxAge != -1) {
		return maxAge <= System.currentTimeMillis();
	    }

	    if (expires != null) {
		Date now = new Date(System.currentTimeMillis());
		return now.after(expires);
	    }
	} else {
	    // according to RFC 6265 if non of the both is given the cookie is valid until the end of the session so the
	    // manager decides whether it is valid or not.
	    return false;
	}

	return true;
    }

    /**
     * Indicates whether the cookies secure attribute is set or not.
     *
     * @return {@code TRUE} if the {@code secure} attribute is set else {@code FALSE}
     */
    public boolean isSecure() {
	return secure;
    }

    @Override
    public boolean equals(Object o) {
	if (! (o instanceof Cookie)) {
	    return false;
	}

	Cookie c = (Cookie) o;
	return c.getName().equals(this.name) && c.getDomain().equals(this.domain) && c.getPath().equals(this.path) &&
		c.getValue().equals(this.value);
    }

    @Override
    public int hashCode() {
	int hash = this.name.hashCode() + this.value.hashCode() + this.domain.hashCode() + this.path.hashCode();
	return hash;
    }

    /**
     * Sets the creation time of the cookie.
     * <br/>
     * Should be used with {@link System#currentTimeMillis()} or {@link Cookie#getCreationTime()}.
     *
     * @param time The creation time to set.
     */
    public void setCreationTime(long time) {
	this.creationTime = time;
    }

    /**
     * Get the creation time of the cookie.
     *
     * @return The creation time of the cookie in the style of {@link System#currentTimeMillis()}.
     */
    public long getCreationTime() {
	return creationTime;
    }
}
