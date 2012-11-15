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

package org.openecard.client.control.binding.javascript.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.openecard.client.control.ControlException;
import org.openecard.client.control.module.status.GenericWaitForChangeHandler;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
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
     * @param genericWaitForChangeHandler to handle the generic part of the WaitForChange request
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
	    StringBuilder b = new StringBuilder();
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

	    Document contentDoc = m.marshal(genericWaitForChangeHandler.getStatusChange());
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

}
