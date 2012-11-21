/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.control.binding.javascript.handler;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.openecard.control.ControlException;
import org.openecard.control.module.status.GenericWaitForChangeHandler;
import org.openecard.control.module.status.StatusChangeRequest;
import org.openecard.ws.WSMarshaller;
import org.openecard.ws.WSMarshallerException;
import org.openecard.ws.WSMarshallerFactory;
import org.openecard.ws.schema.StatusChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * Implements a WaitForChangeHandler to wait for a change in the client.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class JavaScriptWaitForChangeHandler extends JavaScriptControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptWaitForChangeHandler.class);

    private final WSMarshaller m;
    private final GenericWaitForChangeHandler genericWaitForChangeHandler;

    /**
     * Creates a new JavaScriptWaitForChangeHandler.
     *
     * @param genericWaitForChangeHandler
     *            to handle the generic part of the WaitForChange request
     */
    public JavaScriptWaitForChangeHandler(GenericWaitForChangeHandler genericWaitForChangeHandler) {
	super("waitForChange");
	this.genericWaitForChangeHandler = genericWaitForChangeHandler;
	try {
	    m = WSMarshallerFactory.createInstance();
	} catch (WSMarshallerException e) {
	    logger.error(e.getMessage(), e);
	    throw new RuntimeException(e);
	}
    }

    @Override
    public Object[] handle(Map request) {
	if (logger.isDebugEnabled()) {
	    StringBuilder b = new StringBuilder(2048);
	    Iterator<Map.Entry> i = request.entrySet().iterator();
	    while (i.hasNext()) {
		Map.Entry e = i.next();
		b.append("\n '").append(e.getKey()).append("' : '").append(e.getValue()).append("'");
	    }
	    logger.debug("JavaScript request: {}", b.toString());
	    logger.debug("JavaScript request handled by: {}", this.getClass().getName());
	}
	try {
	    ArrayList<String> xml = new ArrayList<String>();
	    StatusChangeRequest statusChangeRequest = this.handleRequest(request);
	    StatusChange statusChange = genericWaitForChangeHandler.getStatusChange(statusChangeRequest);

	    if (statusChange == null) {
		return null;
	    }

	    Document contentDoc = m.marshal(statusChange);
	    String result = m.doc2str(contentDoc);
	    xml.add(result);

	    return xml.toArray(new Object[xml.size()]);
	} catch (ControlException e) {
	    // TODO
	    throw e;
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }

    /**
     * Extracts the StatusChangeRequest from the request-data.
     * @param data the request data
     * @return the extracted StatusChangeRequest or null if an error occurred
     */
    private StatusChangeRequest handleRequest(Map data) {
	try {
	    StatusChangeRequest statusChangeRequest = null;
	    // TODO: rewrite code so that it is safer
	    Iterator i = data.entrySet().iterator();
	    while (i.hasNext()) {
		Map.Entry e = (Map.Entry) i.next();
		// check content
		if ("session".equals(e.getKey())) {
		    // session
		    String value = URLDecoder.decode(e.getValue().toString(), "UTF-8");
		    statusChangeRequest = new StatusChangeRequest(value);
		}
	    }

	    return statusChangeRequest;
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	    return null;
	}
    }

}
