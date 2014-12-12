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
 ***************************************************************************/

package org.openecard.common.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;


/**
 * Simple URL builder class.
 * The URL builders available (e.g. Jersey, Apache httpclient, ...) all add large dependencies. This class provides a
 * limited subset of those URL builders. <br/>
 * The UrlBuilder is immutable meaning every function yields a new instance and leaves the original instance unmodified.
 *
 * @author Tobias Wich
 */
@Immutable
public class UrlBuilder {

    private final UrlEncoder encoder;

    private String scheme;
    private String userInfo;
    private String host;
    private int port;
    private String path;
    private final HashMap<String, String> queryParams;
    private String fragment;

    private UrlBuilder(UrlEncoder encoder, String scheme, String userInfo, String host, int port, String path,
	    HashMap<String, String> queryParams, String fragment) {
	this.encoder = encoder;
	this.scheme = scheme;
	this.userInfo = userInfo;
	this.host = host;
	this.port = port;
	this.path = path;
	this.queryParams = queryParams;
	this.fragment = fragment;
    }

    private UrlBuilder(UrlBuilder other) {
	this.encoder = other.encoder;
	this.scheme = other.scheme;
	this.userInfo = other.userInfo;
	this.host = other.host;
	this.port = other.port;
	this.path = other.path;
	this.queryParams = new HashMap<>(other.queryParams);
	this.fragment = other.fragment;
    }

    /**
     * Creates a UrlBuilder instance based on the given URI.
     *
     * @param baseUri The URI to use as a template for the builder that should be constructed.
     * @return UriBuilder instance for the given URI.
     */
    public static UrlBuilder fromUrl(URI baseUri) {
	UrlEncoder encoder = new UrlEncoder();

	String scheme = baseUri.getScheme();
	String userInfo = baseUri.getRawUserInfo();
	String host = baseUri.getHost();
	int port = baseUri.getPort();
	String path = baseUri.getRawPath();
	if (path == null || path.isEmpty()) {
	    path = "/";
	}
	HashMap<String, String> queryParams = new HashMap<>(HttpRequestLineUtils.transformRaw(baseUri.getRawQuery()));
	String fragment = baseUri.getRawFragment();

	return new UrlBuilder(encoder, scheme, userInfo, host, port, path, queryParams, fragment);
    }

    /**
     * Creates a UrlBuilder instance based on the given URL.
     *
     * @param baseUrl The URL to use as a template for the builder that should be constructed.
     * @return UriBuilder instance for the given URL.
     * @throws URISyntaxException Thrown in case the given URL could not be converted to the URI class and therefore is
     *   not a valid URI.
     */
    public static UrlBuilder fromUrl(URL baseUrl) throws URISyntaxException {
	return fromUrl(baseUrl.toURI());
    }

    /**
     * Creates a UrlBuilder instance based on the given URL.
     *
     * @param baseUrl The URL to use as a template for the builder that should be constructed.
     * @return UriBuilder instance for the given URL.
     * @throws URISyntaxException Thrown in case the given URL could not be converted to the URI class and therefore is
     *   not a valid URI.
     */
    public static UrlBuilder fromUrl(String baseUrl) throws URISyntaxException {
	return fromUrl(new URI(baseUrl));
    }

    /**
     * Constructs an URI object based on the values in the builder.
     *
     * @return New URI instance resembling the values from the builder.
     * @throws URISyntaxException Thrown in case the values in the builder do not create a valid URI.
     */
    public URI build() throws URISyntaxException {
	StringBuilder sb = new StringBuilder();
	sb.append(scheme).append("://");
	if (userInfo != null) {
	    sb.append(userInfo).append("@");
	}
	sb.append(host);
	if (port != -1) {
	    sb.append(":").append(port);
	}
	sb.append(path);
	Iterator<Map.Entry<String, String>> it = queryParams.entrySet().iterator();
	if (it.hasNext()) {
	    sb.append("?");
	    while (it.hasNext()) {
		Map.Entry<String, String> element = it.next();
		String key = element.getKey();
		String val = element.getValue();
		sb.append(key);
		if (val != null) {
		    sb.append("=").append(val);
		}
		if (it.hasNext()) {
		    sb.append("&");
		}
	    }
	}
	if (fragment != null) {
	    sb.append("#").append(fragment);
	}

	return new URI(sb.toString());
    }


    /**
     * Replaces the scheme (protocol) part of the URL.
     *
     * @param scheme Value to replace.
     * @return A copy of the UrlBuilder with the scheme part modified.
     */
    public UrlBuilder scheme(@Nonnull String scheme) {
	UrlBuilder b = new UrlBuilder(this);
	b.scheme = scheme;
	return b;
    }

    /**
     * Replaces the user info part of the URL.
     *
     * @param userInfo Value to replace.
     * @return A copy of the UrlBuilder with the user info part modified.
     */
    public UrlBuilder userInfo(@Nonnull String userInfo) {
	UrlBuilder b = new UrlBuilder(this);
	b.userInfo = userInfo;
	return b;
    }

    /**
     * Replaces the host part of the URL.
     *
     * @param host Value to replace.
     * @return A copy of the UrlBuilder with the host part modified.
     */
    public UrlBuilder host(@Nonnull String host) {
	UrlBuilder b = new UrlBuilder(this);
	b.host = host;
	return b;
    }

    /**
     * Replaces the port part of the URL.
     *
     * @param port Value to replace. A value of -1 disables the port component.
     * @return A copy of the UrlBuilder with the port part modified.
     */
    public UrlBuilder port(int port) {
	UrlBuilder b = new UrlBuilder(this);
	b.port = port;
	return b;
    }

    /**
     * Replaces the path part of the URL.
     *
     * @param path Value to replace.
     * @return A copy of the UrlBuilder with the path part modified.
     */
    public UrlBuilder replacePath(@Nullable String path) {
	UrlBuilder b = new UrlBuilder(this);
	if (path == null) {
	    path = "/";
	}
	b.path = path;
	return b;
    }

    /**
     * Adds a query parameter to the URL.
     *
     * @param key Key of the parameter.
     * @param value Value of the parameter.
     * @return A copy of the UrlBuilder with added query parameter.
     */
    public UrlBuilder queryParam(@Nonnull String key, @Nullable String value) {
	UrlBuilder b = new UrlBuilder(this);
	b.addQueryInt(key, value);
	return b;
    }

    /**
     * Adds a query parameter to the URL.
     * The parameter value is URL encoded as in forms ({@code application/x-www-form-urlencoded}).
     *
     * @param key Key of the parameter.
     * @param value Value of the parameter.
     * @return A copy of the UrlBuilder with added query parameter.
     */
    public UrlBuilder queryParamUrl(@Nonnull String key, @Nullable String value) {
	UrlBuilder b = new UrlBuilder(this);
	b.addQueryInt(key, encoder.urlEncodeUrl(value));
	return b;
    }

    /**
     * Removes all query parameters from the URL.
     *
     * @return A copy of the UrlBuilder with added query parameter.
     */
    public UrlBuilder removeQueryParams() {
	UrlBuilder b = new UrlBuilder(this);
	b.queryParams.clear();
	return b;
    }

    /**
     * Replaces the fragment part of the URL.
     *
     * @param fragment Value to replace.
     * @return A copy of the UrlBuilder with the fragment part modified.
     */
    public UrlBuilder fragment(@Nullable String fragment) {
	UrlBuilder b = new UrlBuilder(this);
	b.fragment = encoder.encodeFragment(fragment);
	return b;
    }


    private void addQueryInt(@Nonnull String k, @Nullable String v) {
	k = encoder.encodeQueryParam(k);
	v = encoder.encodeQueryParam(v);
	queryParams.put(k, v);
    }

}
