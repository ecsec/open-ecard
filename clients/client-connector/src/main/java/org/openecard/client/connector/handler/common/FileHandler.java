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

package org.openecard.client.connector.handler.common;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.openecard.client.connector.common.DocumentRoot;
import org.openecard.client.connector.common.MimeType;
import org.openecard.client.connector.handler.ConnectorCommonHandler;
import org.openecard.client.connector.http.Http11Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class FileHandler extends ConnectorCommonHandler {

    private static final Logger _logger = LoggerFactory.getLogger(FileHandler.class);

    private DocumentRoot documentRoot;


    /**
     * Creates a new file handler.
     *
     * @param documentRoot Document root
     */
    public FileHandler(DocumentRoot documentRoot) {
	super("/*");
	this.documentRoot = documentRoot;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws Exception {
	// Return 404 Not Found in the default case
	Http11Response httpResponse = new Http11Response(HttpStatus.SC_NOT_FOUND);
	RequestLine requestLine = httpRequest.getRequestLine();

	if (requestLine.getMethod().equals("GET")) {
	    URI requestURI = URI.create(requestLine.getUri());

	    URL filePath = documentRoot.getFile(URLDecoder.decode(requestURI.getPath(), "UTF-8"));
	    if (filePath != null) {
		// Handle file
		_logger.debug("Handle file request");
		handleFile(httpResponse, filePath);
	    } else {
		_logger.debug("The DocumentRoot does not contain the URI: {}", requestURI.getPath());
	    }
	} else {
	    // Return 405 Method Not Allowed
	    httpResponse.setStatusCode(HttpStatus.SC_METHOD_NOT_ALLOWED);
	}

	return httpResponse;
    }

    private void handleFile(Http11Response httpResponse, URL file) throws Exception {
	String fname = file.toString();
	String fileExtension = fname.substring(fname.lastIndexOf(".") + 1);
	MimeType mimeType = MimeType.fromFilenameExtension(fileExtension);
	String typeName = (mimeType != null) ? mimeType.getMimeType() : MimeType.TEXT_PLAIN.getMimeType();

	httpResponse.setStatusCode(HttpStatus.SC_OK);
	BasicHttpEntity entity = new BasicHttpEntity();
	entity.setContent(file.openStream());
	entity.setContentType(ContentType.create(typeName, "UTF-8").toString());
	httpResponse.setEntity(entity);
    }

}
