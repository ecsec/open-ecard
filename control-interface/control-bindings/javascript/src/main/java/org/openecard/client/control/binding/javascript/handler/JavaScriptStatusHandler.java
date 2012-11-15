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

package org.openecard.client.control.binding.javascript.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.openecard.client.control.ControlException;
import org.openecard.client.control.module.status.GenericStatusHandler;
import org.openecard.client.ws.WSMarshaller;
import org.openecard.client.ws.WSMarshallerException;
import org.openecard.client.ws.WSMarshallerFactory;
import org.openecard.ws.schema.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;


/**
 * Implements a StatusHandler to get information about the functionality of the client.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class JavaScriptStatusHandler extends JavaScriptControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(JavaScriptStatusHandler.class);
    private final WSMarshaller m;
    private final GenericStatusHandler genericStatusHandler;

    /**
     * Creates a new JavaScriptStatusHandler.
     *
     *  @param genericStatusHandler to handle the generic part of the Status request
     */
    public JavaScriptStatusHandler(GenericStatusHandler genericStatusHandler) {
	super("getStatus");
	this.genericStatusHandler = genericStatusHandler;
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
	    Status status = genericStatusHandler.handleRequest();
	    Document contentDoc = m.marshal(status);
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
