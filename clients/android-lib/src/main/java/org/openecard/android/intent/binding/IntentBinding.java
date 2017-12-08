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

package org.openecard.android.intent.binding;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonNotFoundException;
import org.openecard.addon.AddonSelector;
import org.openecard.addon.bind.AppPluginAction;
import org.openecard.addon.bind.BindingResult;
import org.openecard.android.system.OpeneCardContext;
import org.openecard.android.async.tasks.BindingTaskResult;
import org.openecard.android.ex.BindingTaskStillRunning;
import org.openecard.android.ex.ContextNotInitialized;
import org.openecard.android.ServiceMessages;
import org.openecard.android.async.tasks.BindingTaskResponse;
import static org.openecard.android.intent.binding.IntentBindingConstants.ADDON_INIT_FAILED;
import static org.openecard.android.intent.binding.IntentBindingConstants.ADDON_NOT_FOUND;
import org.openecard.common.util.HttpRequestLineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class handle the intent binding.
 *
 * @author Mike Prechtl
 */
public class IntentBinding {

    private static final Logger LOG = LoggerFactory.getLogger(IntentBinding.class);

    private BindingTaskResult calling;
    private AddonManager addonManager;
    private AddonSelector addonSelector;

    private static IntentBinding instance;

    private IntentBinding() {
    }

    public static IntentBinding getInstance() {
	synchronized (IntentBinding.class) {
	    if (instance == null) {
		instance = new IntentBinding();
	    }
	}
	return instance;
    }

    public void setAddonManager(AddonManager addonManager) {
	this.addonManager = addonManager;
	this.addonSelector = new AddonSelector(addonManager);
    }

    public void setBindingResultReceiver(BindingTaskResult calling) {
	this.calling = calling;
    }

    public AddonManager getAddonManager() {
	return addonManager;
    }

    public AddonSelector getAddonSelector() {
	return addonSelector;
    }

    public BindingTaskResult getBindingResultReceiver() {
	return calling;
    }

    public synchronized void handleRequest(String uri) throws ContextNotInitialized, BindingTaskStillRunning {
	if (calling == null) {
	    throw new IllegalStateException(ServiceMessages.PLEASE_PROVIDE_BINDING_RESULT_RECEIVER);
	}

	// check if service context is initialized
	OpeneCardContext ctx = OpeneCardContext.getContext();
	if (ctx == null || ! ctx.isInitialized()) {
	    throw new ContextNotInitialized(ServiceMessages.PLEASE_START_OPENECARD_SERVICE);
	}

	// execute binding task async
	URI requestURI = URI.create(uri);
	String path = requestURI.getPath();
	String resourceName = path.substring(1, path.length()); // remove leading '/'

	// find suitable addon
	try {
	    AppPluginAction action = getAddonSelector().getAppPluginAction(resourceName);
	    if (getAddonManager() == null) {
		throw new IllegalStateException(ADDON_INIT_FAILED);
	    } else {
		String rawQuery = requestURI.getRawQuery();
		Map<String, String> queries = new HashMap<>(0);
		if (rawQuery != null) {
		    queries = HttpRequestLineUtils.transform(rawQuery);
		}
		BindingResult result = action.execute(null, queries, null, null);
		getBindingResultReceiver().setResultOfBindingTask(new BindingTaskResponse(result));
	    }
	} catch (AddonNotFoundException ex) {
	    LOG.info(ADDON_NOT_FOUND, ex);
	} catch (UnsupportedEncodingException ex) {
	    LOG.warn("Unsupported encoding.", ex);
	} catch (Exception ex) {
	    LOG.warn(ex.getMessage(), ex);
	}
    }

}
