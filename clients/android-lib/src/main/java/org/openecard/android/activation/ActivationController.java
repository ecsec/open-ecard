/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.android.activation;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.common.util.HttpRequestLineUtils;
import org.openecard.mobile.activation.ActivationResult;
import static org.openecard.mobile.activation.ActivationResultCode.*;
import org.openecard.mobile.activation.common.CommonActivationResult;
import org.openecard.mobile.system.OpeneCardContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Low level functionality to perform the activation procedure according to BSI TR-03124-1.
 *
 * @author Mike Prechtl
 * @author Tobias Wich
 */
public class ActivationController {

    private static final Logger LOG = LoggerFactory.getLogger(ActivationController.class);

    private final OpeneCardContext sctx;

    /**
     * Creates an instance of the controller bound to the currently initialized Open eCard Stack.
     * The Open eCard Stack is represented by the context object.
     *
     * @param sctx
     */
    public ActivationController(OpeneCardContext sctx) {
	this.sctx = sctx;
    }

    /**
     * Performs an activation according to BSI TR-03124-1, but does not perform the return to web session part.
     * A result containing the outcome of the
     *
     * @param url
     * @return
     */
    public ActivationResult activate(String url) {
	// create request uri and extract query strings
	URI requestURI = URI.create(url);
	String path = requestURI.getPath();
	String resourceName = path.substring(1, path.length()); // remove leading '/'

	// find suitable addon
	String failureMessage;
	AddonManager manager = sctx.getManager();
	AddonSelector selector = null;
	AppPluginAction action = null;
	try {
	    if (manager == null) {
		throw new IllegalStateException("Addon initialization failed.");
	    } else {
		selector = new AddonSelector(manager);
		action = selector.getAppPluginAction(resourceName);

		String rawQuery = requestURI.getRawQuery();
		Map<String, String> queries = new HashMap<>(0);
		if (rawQuery != null) {
		    queries = HttpRequestLineUtils.transform(rawQuery);
		}
		BindingResult result = action.execute(null, queries, null, null);
		return createActivationResult(result);
	    }
	} catch (AddonNotFoundException ex) {
	    failureMessage = ex.getMessage();
	    LOG.info("Addon not found.", ex);
	} catch (UnsupportedEncodingException ex) {
	    failureMessage = "Unsupported encoding.";
	    LOG.warn(failureMessage, ex);
	} catch (Exception ex) {
	    failureMessage = ex.getMessage();
	    LOG.warn(ex.getMessage(), ex);
	} finally {
	    if (selector != null && action != null) {
		selector.returnAppPluginAction(action);
	    }
	}

	LOG.info("Returning error as INTERRUPTED result.");
	return new CommonActivationResult(INTERRUPTED, failureMessage);
    }

    private CommonActivationResult createActivationResult(BindingResult result) {
	LOG.info("Returning result: {}", result);
	CommonActivationResult activationResult;
	switch (result.getResultCode()) {
	    case REDIRECT:
		String location = result.getAuxResultData().get(AuxDataKeys.REDIRECT_LOCATION);
		activationResult = new CommonActivationResult(location, REDIRECT);
		break;
	    case OK:
		activationResult = new CommonActivationResult(OK);
		break;
	    case INTERRUPTED:
		activationResult = new CommonActivationResult(INTERRUPTED, result.getResultMessage());
		break;
	    case DEPENDING_HOST_UNREACHABLE:
		activationResult = new CommonActivationResult(DEPENDING_HOST_UNREACHABLE, result.getResultMessage());
		break;
	    case WRONG_PARAMETER:
	    case MISSING_PARAMETER:
	    case RESOURCE_UNAVAILABLE:
		activationResult = new CommonActivationResult(CLIENT_ERROR, result.getResultMessage());
		break;
	    default:
		activationResult = new CommonActivationResult(INTERNAL_ERROR, result.getResultMessage());
	}
	return activationResult;
    }

}
