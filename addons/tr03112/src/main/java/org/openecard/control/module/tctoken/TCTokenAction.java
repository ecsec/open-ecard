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

package org.openecard.control.module.tctoken;

import java.net.URL;
import java.util.List;
import java.util.Map;
import oasis.names.tc.dss._1_0.core.schema.Result;
import org.openecard.addon.Context;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.Attachment;
import org.openecard.addon.bind.BindingResult;
import org.openecard.addon.bind.BindingResultCode;
import org.openecard.addon.bind.Body;
import org.openecard.common.ECardConstants;
import org.openecard.control.legacy.ControlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TCTokenAction implements AppPluginAction {

    private static final Logger logger = LoggerFactory.getLogger(TCTokenAction.class);

    private GenericTCTokenHandler genericTCTokenHandler;

    @Override
    public void init(Context ctx) {
	this.genericTCTokenHandler = new GenericTCTokenHandler(ctx.getCardStates(), ctx.getDispatcher(), ctx.getUserConsent(), ctx.getRecognition());
    }

    @Override
    public void destroy() {
	// nothing to do
    }

    @Override
    public BindingResult execute(Body body, Map<String, String> parameters, List<Attachment> attachments) {
	BindingResult response;
	try {
	    TCTokenRequest tcTokenRequest = parseParameters(parameters);
	    TCTokenResponse tcTokenResponse = genericTCTokenHandler.handleActivate(tcTokenRequest);
	    response = this.handleResponse(tcTokenResponse);
	} catch (ControlException e) {
	    // TODO: was bad request
	    response = new BindingResult(BindingResultCode.WRONG_PARAMETER);
	    response.setResultMessage(e.getMessage());
	} catch (Exception e) {
	    response = new BindingResult(BindingResultCode.INTERNAL_ERROR);
	    logger.error(e.getMessage(), e);
	}
	return response;
    }

    /**
     * Processes the activation parameters.
     *
     * @param parameters The request URI query parameters.
     * @return A TCToken request for further processing in the TCToken handler.
     * @throws TCTokenException If the TCToken could not be fetched. That means either the URL is invalid, the server
     *   was not reachable or the returned value was not a TCToken or TCToken like structure.
     */
    private TCTokenRequest parseParameters(Map<String, String> parameters) throws TCTokenException {
	TCTokenRequest result;
	if (parameters.containsKey("tcTokenURL")) {
	    result = genericTCTokenHandler.parseTCTokenRequestURI(parameters);
	    result.setTokenFromObject(false);
	    return result;
	} else if (parameters.containsKey("activationObject")) {
	    result = genericTCTokenHandler.parseObjectURI(parameters);
	    result.setTokenFromObject(true);
	    return result;
	}

	throw new TCTokenException("No suitable set of parameters given in the request.");
    }

    /**
     *
     * @param response TC Token response after handling the activation
     * @return BindingResult with a redirect to the determined refreshAddress or with a error BindingResultcoe in error
     *    situations
     * @throws Exception
     */
    private BindingResult handleResponse(TCTokenResponse response) throws Exception {
	// TODO was bad request
	BindingResult httpResponse = new BindingResult(BindingResultCode.WRONG_PARAMETER);

	Result result = response.getResult();

	if (result.getResultMajor().equals(ECardConstants.Major.OK)) {
	    if (response.getRefreshAddress() != null) {
		return handleRedirectResponse(response.getRefreshAddress());
	    } else {
		httpResponse.setResultCode(BindingResultCode.INTERNAL_ERROR);
	    }
	} else {
	    if (result.getResultMessage().getValue() != null) {
		return handleErrorResponse(result.getResultMessage().getValue());
	    } else {
		httpResponse.setResultCode(BindingResultCode.INTERNAL_ERROR);
	    }
	}

	return httpResponse;
    }

    /**
     * Handle a redirect response.
     * 
     * @param location Redirect location
     * @return BindingResult
     */
    private BindingResult handleRedirectResponse(URL location) {
	BindingResult response = new BindingResult(BindingResultCode.REDIRECT);
	response.addParameter("Location", location.toString());
	return response;
    }

    /**
     * Handle a error response.
     * 
     * @param message an error message that serves as string entity for the HttpResponse
     * @return a BindingResult with BindingResultCode.WRONG_PARAMETER containing the error message
     */
    private BindingResult handleErrorResponse(String message) {
	// TODO was bad request
	BindingResult response = new BindingResult(BindingResultCode.WRONG_PARAMETER);
	response.setResultMessage(message);
	return response;
    }

}
