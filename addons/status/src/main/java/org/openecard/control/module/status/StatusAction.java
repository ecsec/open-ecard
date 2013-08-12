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

import java.util.List;
import java.util.Map;
import org.openecard.addon.Context;
import org.openecard.addon.EventHandler;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.Body;
import org.openecard.common.interfaces.ProtocolInfo;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.recognition.CardRecognition;
import org.openecard.ws.marshal.WSMarshaller;
import org.openecard.ws.marshal.WSMarshallerException;
import org.openecard.ws.marshal.WSMarshallerFactory;
import org.openecard.ws.schema.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;


/**
 * 
 * @author Dirk Petrautzki <dirk.petrautzki@hs-coburg.de>
 */
public class StatusAction implements AppPluginAction {

    private static final Logger logger = LoggerFactory.getLogger(StatusAction.class);
    private GenericStatusHandler genericStatusHandler;
    private WSMarshaller m;

    @Override
    public void init(Context ctx) {
	try {
	    m = WSMarshallerFactory.createInstance();
	    m.removeAllTypeClasses();
	    m.addXmlTypeClass(Status.class);
	} catch (WSMarshallerException e) {
	    logger.error(e.getMessage(), e);
	    throw new RuntimeException(e);
	}
	CardRecognition rec = ctx.getRecognition();
	ProtocolInfo protocolInfo = ctx.getProtocolInfo();
	CardStateMap cardStates = ctx.getCardStates();
	EventHandler eventHandler = ctx.getEventHandler();
	this.genericStatusHandler = new GenericStatusHandler(cardStates, eventHandler, protocolInfo, rec);
    }

    @Override
    public void destroy() {
	// nothing to do
    }

    @Override
    public BindingResult execute(Body body, Map<String, String> parameters, List<Attachment> attachments) {
	BindingResult response = null;
	try {
	    StatusRequest statusRequest = checkParameters(parameters);
	    Status statusResponse = genericStatusHandler.handleRequest(statusRequest);
	    response = this.handleResponse(statusResponse);
	} catch (StatusException e) {
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
     * @throws StatusException if a parameter is malformed or missing
     */
    private StatusRequest checkParameters(Map<String, String> parameters) throws StatusException {
	StatusRequest statusRequest = new StatusRequest();

	if (parameters.containsKey("session")) {
	    String value = parameters.get("session");
	    if (value != null && !value.isEmpty()) {
		statusRequest.setSessionIdentifier(value);
	    } else {
		throw new StatusException("Value for session parameter is missing.");
	    }
	}
	for (String s : parameters.keySet()) {
	    if (!s.equals("session")) {
		logger.debug("Unknown query element: {}", s);
	    }
	}

	return statusRequest;
    }

    /**
     *
     * @param response response after handling the Status request
     * @return BindingResult with the Status information
     * @throws Exception
     */
    private BindingResult handleResponse(Status response) throws Exception {
	// TODO was bad request
	BindingResult httpResponse = new BindingResult(BindingResultCode.OK);

	Node xml = m.marshal(response);
	Body body = new Body(xml, "text/xml");
	httpResponse.setBody(body);

	return httpResponse;
    }

}
