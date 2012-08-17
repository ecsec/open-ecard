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

import java.io.IOException;
import java.util.Locale;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.StatusLine;
import org.apache.http.impl.EnglishReasonPhraseCatalog;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;


/**
 * Interceptor to correct incomplete HTTP status line.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StatusLineResponseInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse hr, HttpContext hc) throws HttpException, IOException {
	StatusLine statusLine = hr.getStatusLine();
	int statusCode = statusLine.getStatusCode();
	Locale locale = hr.getLocale();
	String reason = statusLine.getReasonPhrase();
	reason = reason != null ? reason : reasonForCode(statusCode, locale);
	hr.setStatusLine(new BasicStatusLine(statusLine.getProtocolVersion(), statusCode, reason));
    }

    /**
     * Get reason phrase for HTTP status code.
     *
     * @param code HTTP status code
     * @param locale Langue the reason should be written in, or null for ENGLISH.
     * @return Reason phrase, or "Extension Code" if code is not defined in the RFC.
     */
    private static String reasonForCode(int code, Locale locale) {
	locale = locale != null ? locale : Locale.ENGLISH;
	String reason = EnglishReasonPhraseCatalog.INSTANCE.getReason(code, locale);
	return reason != null ? reason : "Extension Code";
    }

}
