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

package org.openecard.android.lib.intent.binding;

import android.os.AsyncTask;
import org.openecard.addon.AddonManager;
import org.openecard.addon.AddonSelector;
import org.openecard.android.lib.ServiceContext;
import org.openecard.android.lib.async.tasks.BindingTask;
import org.openecard.android.lib.async.tasks.BindingTaskResult;
import org.openecard.android.lib.ex.BindingTaskStillRunning;
import org.openecard.android.lib.ex.ContextNotInitialized;
import org.openecard.android.lib.ServiceMessages;


/**
 * This class handle the intent binding.
 *
 * @author Mike Prechtl
 */
public class IntentBinding {

    private BindingTaskResult calling;
    private AddonManager addonManager;
    private AddonSelector addonSelector;

    private static IntentBinding instance;

    private BindingTask bindingTask;

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
	ServiceContext ctx = ServiceContext.getServiceContext();
	if (ctx == null || ! ctx.isInitialized()) {
	    throw new ContextNotInitialized(ServiceMessages.PLEASE_START_OPENECARD_SERVICE);
	}

	// execute binding task async
	if (bindingTask == null || bindingTask.getStatus().equals(AsyncTask.Status.FINISHED)) {
	    bindingTask = new BindingTask(this, uri);
	    bindingTask.execute();
	} else {
	    throw new BindingTaskStillRunning(ServiceMessages.BINDING_TASK_STILL_RUNNING);
	}
    }

    public synchronized void cancelRequest() {
	// if a binding task is already running, then interrupt it
	if (bindingTask != null && (bindingTask.getStatus().equals(AsyncTask.Status.RUNNING)
		|| bindingTask.getStatus().equals(AsyncTask.Status.PENDING))) {
	    bindingTask.cancel(true);
	    bindingTask = null;
	}
    }

}
