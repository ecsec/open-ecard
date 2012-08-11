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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import org.openecard.client.connector.common.DocumentRoot;
import org.openecard.client.connector.common.MimeType;
import org.openecard.client.connector.handler.ConnectorCommonHandler;
import org.openecard.client.connector.http.HTTPConstants;
import org.openecard.client.connector.http.HTTPRequest;
import org.openecard.client.connector.http.HTTPResponse;
import org.openecard.client.connector.http.HTTPStatusCode;
import org.openecard.client.connector.http.header.EntityHeader;
import org.openecard.client.connector.http.header.RequestLine;
import org.openecard.client.connector.http.header.StatusLine;
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
    public HTTPResponse handle(HTTPRequest httpRequest) throws Exception {
	// Return 404 Not Found in the default case
	HTTPResponse httpResponse = new HTTPResponse(HTTPStatusCode.NOT_FOUND_404);
	RequestLine requestLine = httpRequest.getRequestLine();

	if (requestLine.getMethod().equals(RequestLine.Methode.GET.name())) {
	    URI requestURI = requestLine.getRequestURI();

	    File file = new File(documentRoot.getPath(), URLDecoder.decode(requestURI.getPath(), HTTPConstants.CHARSET));
	    if (documentRoot.contains(file)) {
		if (file.isFile() && !file.isDirectory()) {
		    // Handle file
		    _logger.debug("Handle file request");
		    handleFile(httpResponse, file);
		} else if (!file.isFile() && file.isDirectory()) {
		    // Handle directory
		    _logger.debug("Handle directory request");
		    handleDirectory(httpResponse, file);
		} else {
		    _logger.debug("Cannot handle the request");
		    httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.INTERNAL_SERVER_ERROR_500));
		}
	    } else {
		_logger.debug("The DocumentRoot does not contain the URI: {}", requestURI.getPath());
	    }
	} else {
	    // Return 405 Method Not Allowed
	    httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.METHOD_NOT_ALLOWED_405));
	}

	return httpResponse;
    }

    private void handleFile(HTTPResponse httpResponse, File file) throws Exception {
	InputStream is = new BufferedInputStream(new FileInputStream(file));
	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	int b;
	while ((b = is.read()) != -1) {
	    baos.write(b);
	}

	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.OK_200));
	httpResponse.setMessageBody(baos.toByteArray());

	try {
	    String fileExtention = file.getName().substring(file.getName().lastIndexOf(".") + 1, file.getName().length());
	    MimeType mineType = MimeType.fromFilenameExtension(fileExtention);
	    if (mineType != null) {
		httpResponse.addEntityHeaders(new EntityHeader(EntityHeader.Field.CONTENT_TYPE, mineType.getMimeType()));
	    } else {
		_logger.debug("Unknown MINE type for the file: {}", file.toString());
		httpResponse.addEntityHeaders(new EntityHeader(EntityHeader.Field.CONTENT_TYPE, MimeType.TEXT_PLAIN.getMimeType()));
	    }
	} catch (StringIndexOutOfBoundsException e) {
	    _logger.debug("Cannot determine the MINE type of the file: {}", file.toString());
	    httpResponse.addEntityHeaders(new EntityHeader(EntityHeader.Field.CONTENT_TYPE, MimeType.TEXT_PLAIN.getMimeType()));
	}
    }

    private void handleDirectory(HTTPResponse httpResponse, File file) {
	// Directory Listing is not allowed
	_logger.debug("Directory Listing is not allowed for: {}", file.toString());
	httpResponse.setStatusLine(new StatusLine(HTTPStatusCode.FORBIDDEN_403));
    }

}
