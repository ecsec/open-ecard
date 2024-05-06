/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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

package org.openecard.control.binding.http.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.openecard.common.I18n;
import org.openecard.common.util.HTMLUtils;
import org.openecard.control.binding.http.common.DocumentRoot;
import org.openecard.control.binding.http.common.HTTPTemplate;
import org.openecard.control.binding.http.common.HeaderTypes;
import org.openecard.control.binding.http.common.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An HttpResponseInterceptor implementation for errors.
 * <br>
 * <br>
 * The interceptor handles just messages with defined HTTP status codes. If such a message is received than the content
 * will be modified by using a given HTML template.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class ErrorResponseInterceptor implements HttpResponseInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ErrorResponseInterceptor.class);
    private static I18n lang = I18n.getTranslation("http");
    private final HTTPTemplate template;
    private final List<Integer> errorCodes;

    /**
     * Create a new ErrorInterceptor from the given {@code documentRoot} and the given {@code template}.
     * <br>
     * <br>
     * This constructor does not need a list of http status codes instead an predefined list is generated and used. This
     * means that a so created ErrorResponseInterceptor handle messages with the http status codes 400 to 417, 423, 429
     * and 500 to 505 in a special way.
     *
     * @param documentRoot Document root
     * @param template HTML template used to render the message content.
     */
    public ErrorResponseInterceptor(DocumentRoot documentRoot, String template) {
	this(documentRoot, template, generateErrorCodes());
    }

    /**
     * Create a new ErrorInterceptor form the given {@code documentRoot}, the {@code template} and the given {@code errorCodes}.
     *
     * @param documentRoot Document root
     * @param template HTML template used to render the message content.
     * @param errorCodes List of HTTP error status codes which shall be handled by this interceptor.
     */
    public ErrorResponseInterceptor(DocumentRoot documentRoot, String template, List<Integer> errorCodes) {
	this.template = new HTTPTemplate(documentRoot, template);
	this.errorCodes = errorCodes;
    }

    @Override
    public void process(HttpResponse httpResponse, HttpContext httpContext) throws HttpException, IOException {
	StatusLine statusLine = httpResponse.getStatusLine();
	int statusCode = statusLine.getStatusCode();

	if (errorCodes.contains(statusCode)) {
	    LOG.debug("HTTP response intercepted");
	    Header contentType = httpResponse.getFirstHeader(HeaderTypes.CONTENT_TYPE.fieldName());
	    if (contentType != null) {
		// Intercept response with the content type "text/plain"
		if (contentType.getValue().contains(MimeType.TEXT_PLAIN.getMimeType())) {
		    // Remove old headers
		    httpResponse.removeHeaders(HeaderTypes.CONTENT_TYPE.fieldName());
		    httpResponse.removeHeaders(HeaderTypes.CONTENT_LENGTH.fieldName());

		    // Read message body
		    String content = readEntity(httpResponse.getEntity());
		    // escape string to prevent script content to be injected into the template (XSS)
		    content = HTMLUtils.escapeHtml(content);

		    template.setProperty("%%%MESSAGE%%%", content);
		}
	    } else {
		template.setProperty("%%%MESSAGE%%%", lang.translationForKey("http." + statusCode));
	    }

	    template.setProperty("%%%TITLE%%%", "Error");
	    String reason = statusLine.getReasonPhrase();
	    template.setProperty("%%%HEADLINE%%%", reason);

	    // Add new content
	    httpResponse.setEntity(new StringEntity(template.toString(), "UTF-8"));
	    httpResponse.addHeader(HeaderTypes.CONTENT_TYPE.fieldName(), MimeType.TEXT_HTML.getMimeType() + "; charset=utf-8");
	    httpResponse.addHeader(HeaderTypes.CONTENT_LENGTH.fieldName(), String.valueOf(template.getBytes().length));
	}
    }

    private String readEntity(HttpEntity httpEntity) throws IOException {
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	httpEntity.writeTo(baos);

	ContentType type = ContentType.getOrDefault(httpEntity);
	return new String(baos.toByteArray(), type.getCharset());
    }

    private static ArrayList<Integer> generateErrorCodes() {
	ArrayList<Integer> result = new ArrayList<>();
	for (int i = 400; i <= 417; i++) {
	    result.add(i);
	}

	// additional codes used by the HttpAppPluginActionHandler
	result.add(423); // Locked
	result.add(429); // Too many requests

	for (int i = 500; i <= 505; i++) {
	    result.add(i);
	}
	return result;
    }

}
