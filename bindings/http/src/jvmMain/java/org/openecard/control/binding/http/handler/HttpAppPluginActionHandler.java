/****************************************************************************
 * Copyright (C) 2013-2022 HS Coburg.
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

package org.openecard.control.binding.http.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.Headers;
import org.openecard.addon.bind.RequestBody;
import org.openecard.addon.bind.ResponseBody;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.openecard.common.OpenecardProperties;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.HttpRequestLineUtils;
import org.openecard.control.binding.http.common.DocumentRoot;
import org.openecard.control.binding.http.common.HeaderTypes;
import org.openecard.control.binding.http.common.Http11Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class HttpAppPluginActionHandler extends HttpControlHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HttpAppPluginActionHandler.class);

    public static final String METHOD_HDR = "X-OeC-Method";

    private final AddonSelector selector;

    public HttpAppPluginActionHandler(@Nonnull AddonManager addonManager) {
	super("*");

	this.selector = new AddonSelector(addonManager);
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext context) throws HttpException,
	    IOException {
	LOG.debug("HTTP request: {}", httpRequest.toString());

	CORSFilter corsFilter = new CORSFilter();
	HttpResponse corsResp = corsFilter.preProcess(httpRequest, context);
	if (corsResp != null) {
	    // CORS Response created, return it to the caller
	    // This is either a preflight response, or a block, because the Origin mismatched
	    LOG.debug("HTTP response: {}", corsResp);
	    Http11Response.copyHttpResponse(corsResp, httpResponse);
	    return;
	}

	// deconstruct request uri
	String uri = httpRequest.getRequestLine().getUri();
	URI requestURI = URI.create(uri);
	String path = requestURI.getPath();
	String resourceName = path.substring(1, path.length()); // remove leading '/'

	// find suitable addon
	AppPluginAction action = null;
	try {
	    action = selector.getAppPluginAction(resourceName);

	    String rawQuery = requestURI.getRawQuery();
	    Map<String, String> queries = createQueryMap();
	    if (rawQuery != null) {
		queries.putAll(HttpRequestLineUtils.transform(rawQuery));
	    }

	    RequestBody body = null;
	    if (httpRequest instanceof HttpEntityEnclosingRequest) {
		LOG.debug("Request contains an entity.");
		body = getRequestBody((HttpEntityEnclosingRequest) httpRequest, resourceName);
	    }

	    Headers headers = readReqHeaders(httpRequest);
	    // and add some special values to the header section
	    headers.setHeader(METHOD_HDR, httpRequest.getRequestLine().getMethod());

	    BindingResult bindingResult = action.execute(body, queries, headers, null, null);

	    HttpResponse response = createHTTPResponseFromBindingResult(bindingResult);
	    response.setParams(httpRequest.getParams());
	    LOG.debug("HTTP response: {}", response);
	    Http11Response.copyHttpResponse(response, httpResponse);

	    // CORS post processing
	    corsFilter.postProcess(httpRequest, httpResponse, context);
	} catch (AddonNotFoundException ex) {
	    if (path.equals("/")) {
		new IndexHandler().handle(httpRequest, httpResponse, context);
	    } else if (path.startsWith("/")) {
		new FileHandler(new DocumentRoot("/www", "/www-files")).handle(httpRequest, httpResponse, context);
	    } else {
		new DefaultHandler().handle(httpRequest, httpResponse, context);
	    }
	} finally {
	    if (action != null) {
		selector.returnAppPluginAction(action);
	    }
	}
    }


    private Headers readReqHeaders(HttpRequest httpRequest) {
	Headers headers = new Headers();

	// loop over all headers in the request
	HeaderIterator it = httpRequest.headerIterator();
	while (it.hasNext()) {
	    Header next = it.nextHeader();
	    String name = next.getName();
	    String value = next.getValue();

	    if (isMultiValueHeaderType(name)) {
		for (String part : value.split(",")) {
		    headers.addHeader(name, part.trim());
		}
	    } else {
		headers.addHeader(name, value);
	    }
	}

	return headers;
    }

    private boolean isMultiValueHeaderType(@Nonnull String name) {
	// TODO: add further header types
	switch (name) {
	    case "Accept":
	    case "Accept-Language":
	    case "Accept-Encoding":
		return true;
	    default:
		return false;
	}
    }


    private void addHTTPEntity(HttpResponse response, BindingResult bindingResult) {
	ResponseBody responseBody = bindingResult.getBody();
	if (responseBody != null && responseBody.hasValue()) {
	    LOG.debug("BindingResult contains a body.");
	    // determine content type
	    ContentType ct = ContentType.create(responseBody.getMimeType(), responseBody.getEncoding());

	    ByteArrayEntity entity = new ByteArrayEntity(responseBody.getValue(), ct);
	    response.setEntity(entity);
	} else {
	    LOG.debug("BindingResult contains no body.");
	    if (bindingResult.getResultMessage() != null) {
		ContentType ct = ContentType.create("text/plain", Charset.forName("UTF-8"));
		StringEntity entity = new StringEntity(bindingResult.getResultMessage(), ct);
		response.setEntity(entity);
	    }
	}
    }

    private HttpResponse createHTTPResponseFromBindingResult(BindingResult bindingResult) {
	BindingResultCode resultCode = bindingResult.getResultCode();
	LOG.debug("Recieved BindingResult with ResultCode {}", resultCode);
	HttpResponse response;
	switch (resultCode) {
	    case OK:
		response = new Http11Response(HttpStatus.SC_OK);
		break;
	    case REDIRECT:
		response = new Http11Response(HttpStatus.SC_SEE_OTHER);
		String location = bindingResult.getAuxResultData().get(AuxDataKeys.REDIRECT_LOCATION);
		if (location != null && ! location.isEmpty()) {
		    response.addHeader(HeaderTypes.LOCATION.fieldName(), location);
		} else {
		    // redirect requires a location field
		    LOG.error("No redirect address available in given BindingResult instance.");
		    response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		break;
	    case WRONG_PARAMETER:
	    case MISSING_PARAMETER:
		response = new Http11Response(HttpStatus.SC_BAD_REQUEST);
		break;
	    case INTERNAL_ERROR:
	    case INTERRUPTED:
		response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		break;
	    case RESOURCE_UNAVAILABLE:
	    case DEPENDING_HOST_UNREACHABLE:
		response = new Http11Response(HttpStatus.SC_NOT_FOUND);
		break;
	    case RESOURCE_LOCKED:
		response = new Http11Response(HttpStatus.SC_LOCKED);
		break;
	    case TIMEOUT:
		response = new Http11Response(HttpStatus.SC_GATEWAY_TIMEOUT);
		break;
	    case TOO_MANY_REQUESTS:
		// Code for TOO MANY REQUESTS is 429 according to RFC 6585
		response = new Http11Response(429);
		break;
	    default:
		LOG.error("Untreated result code: {}", resultCode);
		response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	addHTTPEntity(response, bindingResult);
	return response;
    }

    private RequestBody getRequestBody(HttpEntityEnclosingRequest entityRequest, String resourceName) throws IOException {
	try {
	    HttpEntity entity = entityRequest.getEntity();
	    InputStream is = entity.getContent();

	    ContentType ct = ContentType.get(entity);
	    byte[] value = FileUtils.toByteArray(is);
	    String mimeType = ct.getMimeType();
	    Charset cs = ct.getCharset();

	    RequestBody body = new RequestBody(resourceName);
	    body.setValue(value, cs, mimeType);
	    return body;
	} catch (UnsupportedCharsetException | ParseException e) {
	    LOG.error("Failed to create request body.", e);
	}

	return null;
    }

    private Map<String, String> createQueryMap() {
	boolean caseInsensitivePath = Boolean.valueOf(OpenecardProperties.getProperty("legacy.case_insensitive_path"));
	if (! caseInsensitivePath) {
	    return new HashMap<>(0);
	} else {
	    return new TreeMap<>(new Comparator<String>() {
		@Override
		public int compare(String o1, String o2) {
		    return o1.compareToIgnoreCase(o2);
		}
	    });
	}
    }

}
