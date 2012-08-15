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

import java.util.Locale;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ReasonPhraseCatalog;
import org.apache.http.StatusLine;
import org.apache.http.message.BasicHttpResponse;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Http11Response extends BasicHttpResponse {

    public Http11Response(StatusLine statusline) {
	super(statusline);
    }

    public Http11Response(int code, String reason) {
	super(HttpVersion.HTTP_1_1, code, reason);
    }

    public Http11Response(int code) {
	this(code, null);
    }

    public Http11Response(StatusLine statusline, ReasonPhraseCatalog catalog, Locale locale) {
	super(statusline, catalog, locale);
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

	// copy rest
	out.setParams(in.getParams());
	out.setLocale(in.getLocale());
	out.setStatusLine(in.getStatusLine());
	out.setEntity(in.getEntity());
    }

}
