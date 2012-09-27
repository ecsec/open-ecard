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

import java.util.Iterator;
import java.util.Map;
import org.openecard.client.control.ControlException;
import org.openecard.client.control.client.ClientRequest;
import org.openecard.client.control.client.ClientResponse;
import org.openecard.client.control.client.ControlListener;
import org.openecard.client.control.client.ControlListeners;
import org.openecard.client.control.handler.ControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public abstract class ControlJavaScriptHandler extends ControlHandler {

    private static final Logger logger = LoggerFactory.getLogger(ControlJavaScriptHandler.class);
    private final ControlListeners listeners;

    public ControlJavaScriptHandler(String id, ControlListeners listeners) {
	super(id);
	this.listeners = listeners;
    }

    /**
     * Handles a request and creates a client request.
     *
     * @param httpRequest HTTP request
     * @return A client request or null
     * @throws Exception If the request should be handled by the handler but is malformed
     */
    public abstract ClientRequest handleRequest(Map data) throws ControlException, Exception;

    /**
     * Handles a client response and creates a response.
     *
     * @param clientResponse Client response
     * @return Response
     * @throws Exception
     */
    public abstract Object[] handleResponse(ClientResponse clientResponse) throws ControlException, Exception;

    public Object[] handle(Map request) throws Exception {
	if (logger.isDebugEnabled()) {
	    StringBuilder b = new StringBuilder();
	    Iterator<Map.Entry> i = request.entrySet().iterator();
	    while (i.hasNext()) {
		Map.Entry e = i.next();
		b.append("\n '").append(e.getKey()).append("' : '").append(e.getValue()).append("'");
	    }
	    logger.debug("JavaScript request: {}", b.toString());
	}
	Object[] response = null;

	try {
	    ClientRequest clientRequest = handleRequest(request);
	    ClientResponse clientResponse = null;
	    if (clientRequest == null) {
		throw new ControlException();
	    }
	    for (ControlListener listener : listeners.getControlListeners()) {
		clientResponse = listener.request(clientRequest);
		if (clientResponse != null) {
		    break;
		}
	    }

	    response = handleResponse(clientResponse);
	} catch (ControlException e) {
	    //TODO
	    throw e;
	} catch (Exception e) {
	    logger.error(e.getMessage(), e);
	} finally {
	    logger.debug("JavaScript response: {}", response);
	    logger.debug("JavaScript request handled by: {}", this.getClass().getName());
	    return response;
	}
    }

}
