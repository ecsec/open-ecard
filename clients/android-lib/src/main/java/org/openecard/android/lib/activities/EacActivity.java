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

package org.openecard.android.lib.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import org.openecard.android.lib.async.tasks.BindingTaskResult;
import org.openecard.android.lib.async.tasks.WaitForCardRecognizedTask;
import org.openecard.android.lib.ex.BindingTaskStillRunning;
import org.openecard.android.lib.ex.ContextNotInitialized;
import org.openecard.android.lib.intent.binding.IntentBinding;
import org.openecard.android.lib.services.EacServiceConnection;
import org.openecard.android.lib.services.ServiceConnectionResponseHandler;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.gui.android.eac.EacGuiService;


/**
 * @author Mike Prechtl
 */
public abstract class EacActivity extends NfcActivity implements BindingTaskResult, ServiceConnectionResponseHandler {

    private EacServiceConnection mEacGuiConnection;
    private WaitForCardRecognizedTask waitTask;

    private volatile boolean alreadyBinded = false;

    protected String getBindingURI() {
	Uri data = getIntent().getData();
        return data.toString();
    }

    @Override
    protected void onStart() {
	super.onStart();
	if (! alreadyBinded) {
	    IntentBinding binding = IntentBinding.getInstance();
	    binding.setContextWrapper(this);

	    mEacGuiConnection = new EacServiceConnection(this, getApplicationContext());
	    mEacGuiConnection.startService();

	    this.alreadyBinded = true;
	}
    }

    @Override
    protected void onStop() {
	super.onStop();
	IntentBinding binding = IntentBinding.getInstance();
	binding.cancelRequest();
	if (waitTask != null && (waitTask.getStatus().equals(AsyncTask.Status.PENDING)
		|| waitTask.getStatus().equals(AsyncTask.Status.RUNNING))) {
	    waitTask.cancel(true);
	}
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	if (alreadyBinded) {
	    mEacGuiConnection.stopService();
	    unbindEacGui();
	    alreadyBinded = false;
	}
    }

    @Override
    protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
    }

    protected synchronized void handleRequest(String uri) throws ContextNotInitialized, BindingTaskStillRunning {
	IntentBinding binding = IntentBinding.getInstance();
	binding.handleRequest(uri);
    }

    protected synchronized void cancelRequest() {
	IntentBinding binding = IntentBinding.getInstance();
	binding.cancelRequest();
    }

    protected synchronized EacGui getEacGui() {
	if (mEacGuiConnection == null) {
	    throw new IllegalStateException("There is no Eac Gui Connection available.");
	}
	return mEacGuiConnection.getEacGui();
    }

    public synchronized void bindEacGui() {
	EacServiceConnection con = getServiceConnection();
	bindService(new Intent(getApplicationContext(), EacGuiService.class), con, Context.BIND_AUTO_CREATE);
    }

    public synchronized void unbindEacGui() {
	unbindService(getServiceConnection());
    }

    public synchronized EacServiceConnection getServiceConnection() {
	return mEacGuiConnection;
    }

    public abstract void cardRecognized();

}
