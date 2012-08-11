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

package org.openecard.client.connector.interceptor.error;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.openecard.client.common.I18n;
import org.openecard.client.connector.ConnectorConstants;
import org.openecard.client.connector.common.DocumentRoot;
import org.openecard.client.connector.common.HTTPTemplate;
import org.openecard.client.connector.common.MimeType;
import org.openecard.client.connector.http.HTTPConstants;
import org.openecard.client.connector.http.header.EntityHeader;
import org.openecard.client.connector.interceptor.ConnectorResponseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class ErrorResponseInterceptor extends ConnectorResponseInterceptor {

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
	this(documentRoot,
		template,
		new ArrayList<Integer>() {
		    {
			for (int i = 400; i <= 417; i++) {
			    add(i);
			}
			for (int i = 500; i <= 505; i++) {
			    add(i);
			}
		    }

		});
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
	Integer statusCode = Integer.valueOf(httpResponse.getStatusLine().getStatusCode());

	if (errorCodes.contains(statusCode)) {
	    _logger.debug("HTTP response intercepted");
	    Header contentType = httpResponse.getFirstHeader(EntityHeader.Field.CONTENT_TYPE.getFieldName());
	    if (contentType != null) {
		// Intercept response with the content type "text/plain"
		if (contentType.getValue().contains(MimeType.TEXT_PLAIN.getMimeType())) {
		    // Remove old headers
		    httpResponse.removeHeaders(EntityHeader.Field.CONTENT_TYPE.getFieldName());
		    httpResponse.removeHeaders(EntityHeader.Field.CONTENT_LENGTH.getFieldName());

		    // Read message body
		    String content = readEntiry(httpResponse.getEntity());

		    template.setProperty("%%%MESSAGE%%%", content);
		}
	    } else {
		template.setProperty("%%%MESSAGE%%%", lang.translationForKey("http." + statusCode));
	    }

	    template.setProperty("%%%TITLE%%%", "Error");
	    template.setProperty("%%%HEADLINE%%%", httpResponse.getStatusLine().getReasonPhrase());

	    // Add new content
	    httpResponse.setEntity(new StringEntity(template.toString(), HTTPConstants.CHARSET));
	    httpResponse.addHeader(
		    EntityHeader.Field.CONTENT_TYPE.getFieldName(),
		    MimeType.TEXT_HTML.getMimeType() + "; charset=" + HTTPConstants.CHARSET.toLowerCase());
	    httpResponse.addHeader(
		    EntityHeader.Field.CONTENT_LENGTH.getFieldName(),
		    String.valueOf(template.getBytes().length));
	}
    }

    private String readEntiry(HttpEntity httpEntiry) throws IOException {
	InputStream is = new BufferedInputStream(httpEntiry.getContent());
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	int b;
	while ((b = is.read()) != -1) {
	    baos.write(b);
	}

	return new String(baos.toByteArray());
    }

}
