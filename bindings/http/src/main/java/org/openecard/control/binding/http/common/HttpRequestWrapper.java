/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

package org.openecard.control.binding.http.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpEntityEnclosingRequest;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.util.EntityUtils;
import org.openecard.common.util.HttpRequestLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HttpRequest wrapper to parse request parameters.
 *
 * @author Benedikt Biallowons
 */
public class HttpRequestWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(HttpRequestWrapper.class);

    private final HttpRequest request;
    private final Map<String, List<String>> parameterMap;

    /**
     * Create HttpRequestWrapper instance.
     *
     * @param request HttpRequest
     */
    public HttpRequestWrapper(HttpRequest request) {
	this.request = request;
	this.parameterMap = new HashMap<>();

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
	return this.parameterMap.containsKey(parameterName);
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
	    String query = URI.create(this.request.getRequestLine().getUri()).getRawQuery();
	    if (query == null) {
		return parameterMap;
	    }

	    Map<String, String> queries;
	    try {
		queries = HttpRequestLineUtils.transform(query);
	    } catch (UnsupportedEncodingException ex) {
		queries = HttpRequestLineUtils.transformRaw(query);
	    }

	    for (Map.Entry<String, String> next : queries.entrySet()) {
		String name = next.getKey();
		String value = next.getValue();

		if (this.parameterMap.containsKey(name)) {
		    this.parameterMap.get(name).add(value);
		} else {
		    List<String> values = new ArrayList<>();
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
