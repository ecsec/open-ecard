/****************************************************************************
 * Copyright (C) 2012-2013 ecsec GmbH.
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

package org.openecard.transport.httpcore;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.openecard.apache.http.Header;
import org.openecard.apache.http.HttpRequest;
import org.openecard.apache.http.HttpResponse;
import org.openecard.apache.http.RequestLine;
import org.openecard.apache.http.StatusLine;
import org.slf4j.Logger;


/**
 * Utility functions for Apache HTTP core.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class HttpUtils {

    /**
     * Dump the given HTTP request and log it with the given logger instance.
     *
     * @see #dumpHttpRequest(org.slf4j.Logger, java.lang.String, org.openecard.apache.http.HttpRequest)
     * @param logger Logger to dump HTTP request to.
     * @param req Request to dump.
     */
    public static void dumpHttpRequest(@Nonnull Logger logger, @Nonnull HttpRequest req) {
	dumpHttpRequest(logger, null, req);
    }

    /**
     * Dump the given HTTP request and log it with the given logger instance.
     * An optional message can be given wich will be printed in the head of the log entry to define the context of the
     * message. The request message is not modified by this method.
     *
     * @param logger Logger to dump HTTP request to.
     * @param msg Message qualifying the context of the request.
     * @param req Request to dump.
     */
    public static void dumpHttpRequest(@Nonnull Logger logger, @Nullable String msg, @Nonnull HttpRequest req) {
	if (logger.isDebugEnabled()) {
	    StringWriter w = new StringWriter();
	    PrintWriter pw = new PrintWriter(w);

	    pw.print("HTTP Request");
	    if (msg != null) {
		pw.format(" (%s)", msg);
	    }
	    pw.println(":");
	    RequestLine rl = req.getRequestLine();
	    pw.format("  %s %s %s%n", rl.getMethod(), rl.getUri(), rl.getProtocolVersion().toString());
	    for (Header h : req.getAllHeaders()) {
		pw.format("  %s: %s%n", h.getName(), h.getValue());
	    }
	    pw.flush();

	    logger.debug(w.toString());
	}
    }

    /**
     * Dump the given HTTP response and log it with the given logger instance.
     * The response message is not modifyed by the method. If the data contained in the message should be printed, it
     * must be extracted seperately and provided in the respective parameter..
     *
     * @param logger Logger to dump HTTP request to.
     * @param res Response to dump.
     * @param entityData Response data to dump if not null.
     */
    public static void dumpHttpResponse(@Nonnull Logger logger, @Nonnull HttpResponse res,
	    @Nullable byte[] entityData) {
	if (logger.isDebugEnabled()) {
	    StringWriter w = new StringWriter();
	    PrintWriter pw = new PrintWriter(w);

	    pw.println("HTTP Response:");
	    StatusLine sl = res.getStatusLine();
	    pw.format("  %s %d %s%n", sl.getProtocolVersion().toString(), sl.getStatusCode(), sl.getReasonPhrase());
	    for (Header h : res.getAllHeaders()) {
		pw.format("  %s: %s%n", h.getName(), h.getValue());
	    }
	    if (entityData != null) {
		pw.format(new String(entityData));
	    }
	    pw.println();
	    pw.flush();

	    logger.debug(w.toString());
	}
    }

}
