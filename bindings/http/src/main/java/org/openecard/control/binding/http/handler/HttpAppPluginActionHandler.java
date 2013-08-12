/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.UnsupportedCharsetException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.Body;
import org.openecard.apache.http.HttpEntity;
import org.openecard.apache.http.HttpEntityEnclosingRequest;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpStatus;
import org.openecard.apache.http.ParseException;
import org.openecard.apache.http.entity.BasicHttpEntity;
import org.openecard.apache.http.entity.ContentType;
import org.openecard.apache.http.entity.StringEntity;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.apache.http.protocol.HttpRequestHandler;
import org.openecard.common.util.FileUtils;
import org.openecard.common.util.HttpRequestLineUtils;
import org.openecard.control.binding.http.common.DocumentRoot;
import org.openecard.control.binding.http.common.HeaderTypes;
import org.openecard.control.binding.http.common.Http11Response;
import org.openecard.control.binding.http.handler.common.DefaultHandler;
import org.openecard.control.binding.http.handler.common.FileHandler;
import org.openecard.control.binding.http.handler.common.IndexHandler;
import org.openecard.control.handler.ControlHandler;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class HttpAppPluginActionHandler extends ControlHandler implements HttpRequestHandler {

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
		Map<String, String> queries = new HashMap<String, String>(0);
		if (rawQuery != null) {
		    queries = HttpRequestLineUtils.transform(rawQuery);
		}
		Body body = null;
		if (httpRequest instanceof HttpEntityEnclosingRequest) {
		    logger.debug("Request contains an entity.");
		    body = getRequestBody(httpRequest);
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

    private HttpEntity createHTTPEntityFromBody(BindingResult bindingResult) {
	Body responseBody = bindingResult.getBody();
	if (responseBody != null) {
	    logger.debug("BindingResult contains a body.");
	    BasicHttpEntity entity = new BasicHttpEntity();
	    try {
		Node value = responseBody.getValue();
		if (value.getFirstChild().getNodeName().equals("base64Content")) {
		    byte[] bytes = value.getFirstChild().getFirstChild().getNodeValue().getBytes("UTF-8");
		    entity.setContent(new ByteArrayInputStream(bytes));
		} else {
		    entity.setContent(new ByteArrayInputStream(marshaller.doc2str(value).getBytes("UTF-8")));
		}
	    } catch (TransformerException e) {
		logger.error("Failed to set http entity", e);
	    } catch (UnsupportedEncodingException e) {
		logger.error("Failed to set http entity", e);
	    }
	    entity.setContentType(ContentType.create(responseBody.getMimeType()).toString());
	    return entity;
	}
	logger.debug("BindingResult contains NO body.");
	return null;
    }

    private HttpEntity createHTTPEntity(BindingResult bindingResult) {
	HttpEntity entity = createHTTPEntityFromBody(bindingResult);
	if (entity == null && bindingResult.getResultMessage() != null) {
	    try {
		entity = new StringEntity(bindingResult.getResultMessage());
	    } catch (UnsupportedEncodingException e) {
		logger.error("StringEntity creation failed. Returned entity will be null", e);
	    }
	}
	return entity;
    }

    private HttpResponse createHTTPResponseFromBindingResult(BindingResult bindingResult) {
	BindingResultCode resultCode = bindingResult.getResultCode();
	logger.debug("Recieved BindingResult with ResultCode {}", resultCode);
	HttpResponse response;
	if (resultCode.equals(BindingResultCode.OK)) {
	    response = new Http11Response(HttpStatus.SC_OK);
	} else if (resultCode.equals(BindingResultCode.REDIRECT)) {
	    response = new Http11Response(HttpStatus.SC_SEE_OTHER);
	    response.addHeader(HeaderTypes.LOCATION.fieldName(), bindingResult.getParameters().get("Location"));
	} else if (resultCode.equals(BindingResultCode.WRONG_PARAMETER)) {
	    response = new Http11Response(HttpStatus.SC_BAD_REQUEST);
	} else if (resultCode.equals(BindingResultCode.INTERNAL_ERROR)) {
	    response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	} else {
	    logger.error("Untreated result code: " + resultCode);
	    response = new Http11Response(HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}
	HttpEntity entity = createHTTPEntity(bindingResult);
	response.setEntity(entity);
	return response;
    }

    private Body getRequestBody(HttpRequest httpRequest) throws IOException {
	HttpEntityEnclosingRequest entityRequest = (HttpEntityEnclosingRequest) httpRequest;
	HttpEntity entity = entityRequest.getEntity();
	InputStream is = entity.getContent();
	try {
	    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = fac.newDocumentBuilder();
	    Document d = builder.newDocument();
	    Element elemBase64 = d.createElement("base64Content");
	    elemBase64.setTextContent(FileUtils.toString(is));
	    d.appendChild(elemBase64);
	    return new Body(d, ContentType.get(entity).getMimeType());
	} catch (UnsupportedCharsetException e) {
	    logger.error("Failed to create request body.", e);
	} catch (ParseException e) {
	    logger.error("Failed to create request body.", e);
	} catch (ParserConfigurationException e) {
	    logger.error("Failed to create request body.", e);
	}
	return null;
    }

}
