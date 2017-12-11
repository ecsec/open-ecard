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
import static org.openecard.android.activation.ActivationResultCode.*;
import org.openecard.android.ex.ApduExtLengthNotSupported;
import org.openecard.android.ex.InitializationException;
import org.openecard.android.ex.NfcDisabled;
import org.openecard.android.ex.NfcUnavailable;
import org.openecard.android.ex.UnableToInitialize;
import org.openecard.android.system.OpeneCardContext;
import org.openecard.common.util.HttpRequestLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Mike Prechtl
 */
public class ActivationController {

    private static final Logger LOG = LoggerFactory.getLogger(ActivationController.class);

    public ActivationResult activate(String url) {
	// ensure that service context is initialized
	OpeneCardContext sctx;
	try {
	    sctx = ensureInitialized();
	} catch (InitializationException ex) {
	    return new ActivationResult(INTERNAL_ERROR, ex.getMessage());
	}

	// create request uri and extract query strings
	URI requestURI = URI.create(url);
	String path = requestURI.getPath();
	String resourceName = path.substring(1, path.length()); // remove leading '/'

	// find suitable addon
	String failureMessage;
	AddonManager manager = sctx.getManager();
	AddonSelector selector = new AddonSelector(manager);
	try {
	    if (manager == null || selector == null) {
		throw new IllegalStateException("Addon initialization failed.");
	    } else {
		AppPluginAction action = selector.getAppPluginAction(resourceName);

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
	}

	return new ActivationResult(INTERRUPTED, failureMessage);
    }

    public OpeneCardContext ensureInitialized() throws InitializationException {
	OpeneCardContext sctx = OpeneCardContext.getContext();
	if (! sctx.isInitialized()) {
	    try {
		sctx.initialize();
	    } catch (UnableToInitialize | NfcUnavailable | NfcDisabled | ApduExtLengthNotSupported ex) {
		throw new InitializationException(ex);
	    }
	}
	return sctx;
    }

    private ActivationResult createActivationResult(BindingResult result) {
	ActivationResult activationResult;
	switch (result.getResultCode()) {
	     case REDIRECT:
		String location = result.getAuxResultData().get(AuxDataKeys.REDIRECT_LOCATION);
		activationResult = new ActivationResult(location, REDIRECT);
		break;
	    default:
		activationResult = new ActivationResult(INTERNAL_ERROR, result.getResultMessage());
	}
	return activationResult;
    }

}
