/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

import java.io.IOException;
import org.openecard.apache.http.HttpException;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.HttpResponseInterceptor;
import org.openecard.apache.http.protocol.HttpContext;
import org.openecard.common.Version;


/**
 * HttpResponseInterceptor implementation which adds the {@code Server} header to all responses sent by the HTTP Binding.
 *
 * @author Hans-Martin Haase
 */
public class ServerHeaderResponseInterceptor implements HttpResponseInterceptor {

    @Override
    public void process(HttpResponse hr, HttpContext hc) throws HttpException, IOException {
	hr.addHeader("Server", buildServerHeaderValue());
    }

    /**
     * Creates the value of the {@code Server} header according to BSI-TR-03124-1 v1.2 section 2.2.2.1.
     *
     * @return A string containing the {@code Server} header value.
     */
    public String buildServerHeaderValue() {
	StringBuilder builder = new StringBuilder();
	builder.append(Version.getName());
	builder.append("/");
	builder.append(Version.getVersion());
	builder.append(" (");
	for (String version : Version.getSpecVersions()) {
	    builder.append(Version.getSpecName());
	    builder.append("/");
	    builder.append(version);
	    builder.append(" ");
	}
	// remove last white space
	builder.deleteCharAt(builder.lastIndexOf(" "));
	builder.append(")");
	return builder.toString();
    }
    
}
