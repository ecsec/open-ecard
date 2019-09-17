/** **************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ************************************************************************** */
package org.openecard.mobile.activation.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Neil Crossley
 */
public class ActivationUrlFactory {

    public final String protocol;
    public final String domain;
    public final Integer port;
    public final String resourceName;
    public final Map<String, String> queries;

    public ActivationUrlFactory(String protocol, String domain, Integer port, String resourceName, Map<String, String> queries) {
	this.protocol = protocol;
	this.domain = domain;
	this.port = port;
	this.resourceName = resourceName;
	this.queries = queries;
    }

    public ActivationUrlFactory withQueryParameter(String key, String value) {
	Map<String, String> result = new HashMap<>(this.queries);
	result.put(key, value);
	return new ActivationUrlFactory(protocol, domain, port, resourceName, result);
    }

    public ActivationUrlFactory withHttps() {
	return new ActivationUrlFactory("https", domain, port, resourceName, queries);
    }

    public URL create() {
	StringBuilder urlBuilder = new StringBuilder();
	urlBuilder.append(protocol);
	urlBuilder.append("://");
	urlBuilder.append(domain);
	if (port != null) {
	    urlBuilder.append(":");
	    urlBuilder.append(port);
	}
	urlBuilder.append("/");
	urlBuilder.append(resourceName);
	if (!this.queries.isEmpty()) {
	    urlBuilder.append("?");
	    boolean requireParameterJoin = false;
	    for (Map.Entry<String, String> entry : queries.entrySet()) {
		if (requireParameterJoin) {
		    urlBuilder.append("&");
		} else {
		    requireParameterJoin = true;
		}
		urlBuilder.append(entry.getKey());
		urlBuilder.append("=");
		urlBuilder.append(entry.getValue());
	    }
	}
	try {
	    return new URL(urlBuilder.toString());
	} catch (MalformedURLException ex) {
	    throw new IllegalArgumentException("Could not create the activation URL.", ex);
	}
    }

    public static ActivationUrlFactory fromResource(String resourceName) {
	return new ActivationUrlFactory("http", "localhost", 2427, resourceName, new HashMap<>());
    }
}
