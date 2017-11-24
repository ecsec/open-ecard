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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.android.lib.ServiceContext;
import org.openecard.android.lib.async.tasks.BindingTaskResponse;
import org.openecard.android.lib.async.tasks.BindingTaskResult;
import org.openecard.android.lib.ex.ApduExtLengthNotSupported;
import org.openecard.android.lib.ex.BindingTaskStillRunning;
import org.openecard.android.lib.ex.ContextNotInitialized;
import org.openecard.android.lib.intent.binding.IntentBinding;
import org.openecard.android.lib.utils.NfcUtils;
import org.openecard.gui.android.eac.EacGuiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class provides the basic functionality as specified in the technical guideline and the
 * initialisation of the Eac UI interface service. By extending the class, the UI can be added
 * (see BindingActivity in <a href="https://github.com/ecsec/open-ecard-android">ecsec/open-ecard-android</a>).
 *
 * @author Mike Prechtl
 */
public abstract class AbstractActivationActivity extends Activity implements BindingTaskResult {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractActivationActivity.class);

    private boolean eacAlreadyConnected = false;

    private volatile boolean alreadyInitialized = false;
    // if someone returns to the App, but Binding uri was already used.
    private volatile boolean bindingUriAlreadyUsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// set Open eCard Service Context
	NfcUtils.getInstance().setServiceContext(ServiceContext.getServiceContext());
    }

    @Override
    protected synchronized void onResume() {
	super.onResume();

	// enable dispatch with nfc tag
	NfcUtils.getInstance().enableNFCDispatch(this);
    }

    @Override
    protected synchronized void onPause() {
	super.onPause();

	try {
	    // disable dispatch with nfc tag
	    NfcUtils.getInstance().disableNFCDispatch(this);
	} catch (Exception e) {
	    LOG.info(e.getMessage(), e);
	}
    }

    @Override
    protected void onStart() {
	super.onStart();

	// initialize intent binding
	if (! alreadyInitialized) {
	    IntentBinding binding = IntentBinding.getInstance();
	    binding.setBindingResultReceiver(this);
	    ServiceContext.getServiceContext().setEacStarter(this.guiStarter);
	    this.alreadyInitialized = true;
	}

	String bindingUri = getBindingURI(getIntent());
	if (bindingUri != null && ! bindingUriAlreadyUsed) {
	    // start TR procedure according to [BSI-TR-03124-1]
	    HandleRequestAsync task = new HandleRequestAsync();
	    task.execute(bindingUri);
	    bindingUriAlreadyUsed = true;
	} else {
	    finish();
	}
    }

    private class HandleRequestAsync extends AsyncTask<String, Void, Void> {
	@Override
	protected Void doInBackground(String... uri) {
	    IntentBinding binding = IntentBinding.getInstance();
	    try {
		binding.handleRequest(uri[0]);
	    } catch (ContextNotInitialized | BindingTaskStillRunning e) {
		LOG.error(e.getMessage(), e);
	    }
	    return null;
	}
    }

    private Runnable guiStarter = new Runnable() {
	@Override
	public void run() {
	    if (! eacAlreadyConnected) {
		// start and bind eac gui service
		Intent i = createEacGuiIntent();
		Context ctx = getApplicationContext();
		LOG.info("Starting Eac Gui service...");
		ctx.startService(i);
		LOG.info("Binding Eac Gui service...");
		ctx.bindService(i, getServiceConnection(), 0);
		eacAlreadyConnected = true;
	    }
	}
    };

    @Override
    protected void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
	try {
	    // extract nfc tag
	    NfcUtils.getInstance().retrievedNFCTag(intent);

	} catch (ApduExtLengthNotSupported ex) {
	    LOG.error(ex.getMessage());
	}
    }


    @Override
    protected void onStop() {
	super.onStop();
	// cancel request if app is closed or minimized
	IntentBinding binding = IntentBinding.getInstance();
	binding.cancelRequest();
	if (eacAlreadyConnected) {
	    // unbind eac gui service
	    Intent i = createEacGuiIntent();
	    Context ctx = getApplicationContext();
	    eacAlreadyConnected = false;
	    ctx.stopService(i);
	    LOG.info("Unbinding Eac Gui service...");
	    ctx.unbindService(getServiceConnection());
	} // else do nothing, because the service hasn't been started yet, maybe because the user canceled the request.
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	if (alreadyInitialized) {
	    alreadyInitialized = false;
	}
    }

    @Override
    public void setResultOfBindingTask(BindingTaskResponse response) {
	redirectToResultLocation(response.getBindingResult());
    }

    public void redirectToResultLocation(BindingResult result) {
	String location = result.getAuxResultData().get(AuxDataKeys.REDIRECT_LOCATION);
	if (location != null) {
	    // redirct to result location
	    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
	    startActivity(i);
	}
    }

    /**
     * Extracts the binding uri from the intent.
     *
     * @param i the corresponding intent.
     * @return
     */
    protected String getBindingURI(Intent i) {
	Uri data = i.getData();
	return data != null ? data.toString() : null;
    }

    /**
     * Creates an intent to start the Eac Gui Service.
     *
     * @return
     */
    protected Intent createEacGuiIntent() {
	return new Intent(getApplicationContext(), EacGuiService.class);
    }

    /**
     * Returns true if the activity is already connected to the Eac Gui Service, otherwise false is returned.
     *
     * @return
     */
    protected boolean isConnectedToEacService() {
	return eacAlreadyConnected;
    }

    /**
     * Implement this method to provide an instance of a service connection. The service connection is needed to
     * connect to the Eac Gui Service.
     *
     * @return
     */
    public abstract ServiceConnection getServiceConnection();

}
