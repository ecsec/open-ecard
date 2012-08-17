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

package org.openecard.client.connector.interceptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.openecard.client.common.I18n;
import org.openecard.client.connector.ConnectorConstants;
import org.openecard.client.connector.common.DocumentRoot;
import org.openecard.client.connector.common.HTTPTemplate;
import org.openecard.client.connector.common.MimeType;
import org.openecard.client.connector.http.HeaderTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ErrorResponseInterceptor implements HttpResponseInterceptor {

    private static final Logger _logger = LoggerFactory.getLogger(ErrorResponseInterceptor.class);

    private static I18n lang = ConnectorConstants.getI18n();
    private final HTTPTemplate template;
    private final List<Integer> errorCodes;


    /**
     * Create a new ErrorInterceptor.
     *
     * @param documentRoot Document root
     * @param template Template
     */
    public ErrorResponseInterceptor(DocumentRoot documentRoot, String template) {
	this(documentRoot, template, generateErrorCodes());
    }

    private static ArrayList<Integer> generateErrorCodes() {
	ArrayList<Integer> result = new ArrayList<Integer>();
	for (int i = 400; i <= 417; i++) {
	    result.add(i);
	}
	for (int i = 500; i <= 505; i++) {
	    result.add(i);
	}
	return result;
    }


    /**
     * Create a new ErrorInterceptor.
     *
     * @param documentRoot Document root
     * @param template Template
     * @param errorCodes Error codes
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
	    _logger.debug("HTTP response intercepted");
	    Header contentType = httpResponse.getFirstHeader(HeaderTypes.CONTENT_TYPE.fieldName());
	    if (contentType != null) {
		// Intercept response with the content type "text/plain"
		if (contentType.getValue().contains(MimeType.TEXT_PLAIN.getMimeType())) {
		    // Remove old headers
		    httpResponse.removeHeaders(HeaderTypes.CONTENT_TYPE.fieldName());
		    httpResponse.removeHeaders(HeaderTypes.CONTENT_LENGTH.fieldName());

		    // Read message body
		    String content = readEntity(httpResponse.getEntity());

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

}
