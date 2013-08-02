/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

package org.openecard.control.module.status;

import java.util.Map;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.Body;
import org.openecard.control.ControlException;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.schema.StatusChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Dirk Petrautzki <dirk.petrautzki@hs-coburg.de>
 */
public class WaitForChangeAction implements AppPluginAction {

    private static final Logger logger = LoggerFactory.getLogger(WaitForChangeAction.class);
    private GenericWaitForChangeHandler genericWaitForChangeHandler;
    private WSMarshaller m;

    @Override
    public void init(Context ctx) {
	try {
	    m = WSMarshallerFactory.createInstance();
	    m.removeAllTypeClasses();
	    m.addXmlTypeClass(StatusChange.class);
	} catch (WSMarshallerException e) {
	    logger.error(e.getMessage(), e);
	    throw new RuntimeException(e);
	}
	this.genericWaitForChangeHandler = new GenericWaitForChangeHandler(ctx.getEventHandler());
    }

    @Override
    public void destroy() {
	// nothing to do
    }

    @Override
    public BindingResult execute(Body body, Map<String, String> parameters, Attachment attachments) {
	BindingResult response = null;
	try {
	    StatusChangeRequest request = checkParameters(parameters);
	    StatusChange status = genericWaitForChangeHandler.getStatusChange(request);
	    if (status == null) {
		String msg = "There is no event queue for the specified session identifier existing.";
		throw new ControlException(msg);
	    } else {
		response = this.handleResponse(status);
	    }
	} catch (ControlException e) {
	    response = new BindingResult(BindingResultCode.WRONG_PARAMETER);
	    response.setResultMessage(e.getMessage());
	} catch (Exception e) {
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    logger.error(e.getMessage(), e);
	}
	return response;
    }

    /**
     * Check the request parameters.
     *
     * @param parameters The request parameters.
     * @return A StatusChangeRequest for further processing 
     * @throws ControlException if a parameter is malformed or missing
     */
    private StatusChangeRequest checkParameters(Map<String, String> parameters) throws ControlException {

	if (parameters.containsKey("session")) {
	    String value = parameters.get("session");
	    if (value != null && !value.isEmpty()) {
		StatusChangeRequest request = new StatusChangeRequest(value);
		return request;
	    } else {
		throw new ControlException("Value for session parameter is missing.");
	    }
	}
	for (String s : parameters.keySet()) {
	    if (!s.equals("session")) {
		logger.debug("Unknown query element: {}", s);
	    }
	}

	throw new ControlException("Mandatory parameter session is missing.");
    }

   /**
    *
    * @param response response after handling the StatusChange request
    * @return BindingResult with the StatusChange information 
    * @throws Exception
    */
    private BindingResult handleResponse(StatusChange response) throws Exception {
	BindingResult httpResponse = new BindingResult(BindingResultCode.OK);

	String xml = m.doc2str(m.marshal(response));
	Body body = new Body(xml.getBytes(), "text/xml");
	httpResponse.setBody(body);

	return httpResponse;
    }

}
