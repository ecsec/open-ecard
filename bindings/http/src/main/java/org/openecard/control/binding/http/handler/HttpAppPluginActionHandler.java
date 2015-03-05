/****************************************************************************
 * Copyright (C) 2013-2015 HS Coburg.
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
import java.util.HashMap;
import java.util.Map;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.RequestBody;
import org.openecard.addon.bind.ResponseBody;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpEntityEnclosingRequest;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpStatus;
import org.openecard.apache.http.ParseException;
import org.openecard.apache.http.entity.ContentType;
import org.openecard.apache.http.entity.StringEntity;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.HttpRequestLineUtils;
import org.openecard.control.binding.http.common.DocumentRoot;
import org.openecard.control.binding.http.common.HeaderTypes;
import org.openecard.control.binding.http.common.Http11Response;
import org.openecard.control.binding.http.handler.common.DefaultHandler;
import org.openecard.control.binding.http.handler.common.FileHandler;
import org.openecard.control.binding.http.handler.common.IndexHandler;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Dirk Petrautzki
 * @author Tobias Wich
 */
public class HttpAppPluginActionHandler extends HttpControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpAppPluginActionHandler.class);

    private final AddonManager addonManager;
    private final AddonSelector selector;
    private final WSMarshaller marshaller;

    public HttpAppPluginActionHandler(AddonManager addonManager) {
	super("*");

	this.addonManager = addonManager;
	this.selector = new AddonSelector(addonManager);
	try {
	    marshaller = WSMarshallerFactory.createInstance();
	} catch (WSMarshallerException e) {
	    throw new RuntimeException(e);
	}
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpResponse httpResponse, HttpContext context) throws HttpException,
	    IOException {
	logger.debug("HTTP request: {}", httpRequest.toString());
	// deconstruct request uri
	String uri = httpRequest.getRequestLine().getUri();
	URI requestURI = URI.create(uri);
	String path = requestURI.getPath();
	String resourceName = path.substring(1, path.length()); // remove leading '/'

	// find suitable addon
	try {
	    AppPluginAction action = selector.getAppPluginAction(resourceName);
	    HttpResponse response;
	    if (addonManager == null) {
		response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		StringEntity entity = new StringEntity("Addon initialization failed.", "UTF-8");
		response.setEntity(entity);
	    } else {
		String rawQuery = requestURI.getRawQuery();
		Map<String, String> queries = new HashMap<>(0);
		if (rawQuery != null) {
		    queries = HttpRequestLineUtils.transform(rawQuery);
		}
		RequestBody body = null;
		if (httpRequest instanceof HttpEntityEnclosingRequest) {
		    logger.debug("Request contains an entity.");
		    body = getRequestBody(httpRequest, resourceName);
		}
		BindingResult bindingResult = action.execute(body, queries, null);
		response = createHTTPResponseFromBindingResult(bindingResult);
	    }
	    response.setParams(httpRequest.getParams());
	    logger.debug("HTTP response: {}", response);
	    Http11Response.copyHttpResponse(response, httpResponse);
	} catch (AddonNotFoundException ex) {
	    if (path.equals("/")) {
		new IndexHandler().handle(httpRequest, httpResponse, context);
	    } else if (path.startsWith("/")) {
		new FileHandler(new DocumentRoot("/www", "/www-files")).handle(httpRequest, httpResponse, context);
	    } else {
		new DefaultHandler().handle(httpRequest, httpResponse, context);
	    }
	}
    }


    private void addHTTPEntity(HttpResponse response, BindingResult bindingResult) {
	ResponseBody responseBody = bindingResult.getBody();
	if (responseBody != null && responseBody.hasValue()) {
	    logger.debug("BindingResult contains a body.");
	    // determine content type
	    ContentType ct = ContentType.create(responseBody.getMimeType(), Charset.forName("UTF-8"));
	    StringEntity entity = new StringEntity(responseBody.getValue(), ct);
	    response.setEntity(entity);
	    // evaluate Base64 flag
	    if (responseBody.isBase64()) {
		response.setHeader("Content-Transfer-Encoding", "Base64");
	    }
	} else {
	    logger.debug("BindingResult contains no body.");
	    if (bindingResult.getResultMessage() != null) {
		ContentType ct = ContentType.create("text/plain", Charset.forName("UTF-8"));
		StringEntity entity = new StringEntity(bindingResult.getResultMessage(), ct);
		response.setEntity(entity);
	    }
	}
    }

    private HttpResponse createHTTPResponseFromBindingResult(BindingResult bindingResult) {
	BindingResultCode resultCode = bindingResult.getResultCode();
	logger.debug("Recieved BindingResult with ResultCode {}", resultCode);
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
		    logger.error("No redirect address available in given BindingResult instance.");
		    response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		}
		break;
	    case WRONG_PARAMETER:
	    case MISSING_PARAMETER:
		response = new Http11Response(HttpStatus.SC_BAD_REQUEST);
		break;
	    case INTERNAL_ERROR:
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
		logger.error("Untreated result code: " + resultCode);
		response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

	addHTTPEntity(response, bindingResult);
	return response;
    }

    private RequestBody getRequestBody(HttpRequest httpRequest, String resourceName) throws IOException {
	try {
	    HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) httpRequest;
	    HttpEntity entity = entityRequest.getEntity();
	    InputStream is = entity.getContent();

	    // TODO: This assumes the content is UTF-8. Evaluate what is actually sent.
	    String value = FileUtils.toString(is);
	    String mimeType = ContentType.get(entity).getMimeType();
	    // TODO: find out if we have a Base64 coded value
	    boolean base64Content = false;

	    RequestBody body = new RequestBody(resourceName, null);
	    body.setValue(value, mimeType, base64Content);
	    return body;
	} catch (UnsupportedCharsetException | ParseException e) {
	    logger.error("Failed to create request body.", e);
	}

	return null;
    }

}
