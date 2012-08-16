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

package org.openecard.client.connector.http;

import org.apache.http.*;
import org.apache.http.message.BasicHttpResponse;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Http11Response extends BasicHttpResponse {

    public Http11Response(StatusLine statusline) {
	this(statusline.getStatusCode(), statusline.getReasonPhrase());
    }

    public Http11Response(int code, String reason) {
	super(HttpVersion.HTTP_1_1, code, reason);
    }

    public Http11Response(int code) {
	this(code, null);
    }


    /**
     * Copy the content of a HttpResponse to another instance.
     *
     * @param in HttpResponse
     * @param out HttpResponse
     */
    public static void copyHttpResponse(HttpResponse in, HttpResponse out) {
	// remove and copy headers
	HeaderIterator headIt = out.headerIterator();
	while (headIt.hasNext()) {
	    headIt.nextHeader();
	    headIt.remove();
	}
	headIt = in.headerIterator();
	while (headIt.hasNext()) {
	    Header next = headIt.nextHeader();
	    out.addHeader(next);
	}

	// set entity stuff
	if (in.getEntity() != null) {
	    HttpEntity entity = in.getEntity();
	    out.setEntity(entity);
	    if (entity.getContentType() != null) {
		out.setHeader(entity.getContentType());
	    }
	    if (entity.getContentEncoding() != null) {
		out.setHeader(entity.getContentEncoding());
	    }
	    if (entity.getContentLength() > 0) {
		out.setHeader(HeaderTypes.CONTENT_LENGTH.fieldName(), Long.toString(entity.getContentLength()));
	    }
	    // TODO: use chunked, repeatable and streaming attribute from entity
	}

	// copy rest
	out.setLocale(in.getLocale());
	out.setStatusLine(in.getStatusLine());
    }

}
