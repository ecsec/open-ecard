/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.client.control.binding.http.common;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.util.EntityUtils;


/**
 * HttpRequest wrapper to parse request parameters.
 *
 * @author Benedikt Biallowons <benedikt.biallowons@ecsec.de>
 */
public class HttpRequestWrapper {

    private final HttpRequest request;
    private final Map<String, List<String>> parameterMap;

    /**
     * Create HttpRequestWrapper instance.
     *
     * @param request HttpRequest
     */
    public HttpRequestWrapper(HttpRequest request) {
	this.request = request;
	this.parameterMap = new HashMap<String, List<String>>();

	parseRequestParameters();
    }

    /**
     * Returns a key value map of available parameters, null otherwise.
     *
     * @return parameter map
     */
    public Map<String, List<String>> getRequestParameters() {
	if (!this.parameterMap.isEmpty()) {
	    return this.parameterMap;
	}

	return null;
    }

    /**
     * Returns a list of parameter values if the given parameter name is found,
     * null otherwise.
     *
     * @param parameterName the request parameter name
     * @return list of parameter values or null
     */
    public List<String> getRequestParameter(String parameterName) {
	if (this.parameterMap.containsKey(parameterName)) {
	    return this.parameterMap.get(parameterName);
	}

	return null;
    }

    /**
     * Returns true if the given parameter name is found, false otherwise.
     *
     * @param parameterName parameter name
     * @return true or false
     */
    public boolean hasRequestParameter(String parameterName) {
	return this.parameterMap.containsKey(parameterName) ? true : false;
    }

    /**
     * Returns a key value map of available parameters. The map can be empty but
     * never null.
     *
     * @return a parameter map
     */
    private Map<String, List<String>> parseRequestParameters() {
	String method = this.request.getRequestLine().getMethod();

	if (method.equals(Http11Method.GET.getMethodString())) {
	    // decoded query string
	    String query = URI.create(this.request.getRequestLine().getUri()).getQuery();

	    if (query == null) {
		return parameterMap;
	    }

	    for (String parameter : query.split("&")) {
		String name = parameter.substring(0, parameter.indexOf("="));
		String value = parameter.substring(parameter.indexOf("=") + 1, parameter.length());

		if (this.parameterMap.containsKey(name)) {
		    this.parameterMap.get(name).add(value);
		} else {
		    List<String> values = new ArrayList<String>();
		    values.add(value);
		    this.parameterMap.put(name, values);
		}
	    }
	} else if (method.equals(Http11Method.POST.getMethodString())
		&& request instanceof HttpEntityEnclosingRequest) {
	    HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
	    String entityContent;
	    try {
		entityContent = EntityUtils.toString(entity, "UTF-8");
		// TODO: implement POST request parameter parsing
	    } catch (IOException e) {
	    }
	}

	return parameterMap;
    }

}
