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

package org.openecard.android.system;

import android.app.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Client class for the Android Service initializing the Open eCard Stack.
 * <p>This class provides asynchronous methods to initialize the stack. Once the stack is initialized, the context
 * object is handed over in the callback functions of the provided callback handler. The handler functions are run in
 * the activity's UI thread.</p>
 * The class automatically adjusts its inner state when the Open eCard Stack is stopped from the outside.
 *
 * @author Tobias Wich
 * @author Mike Prechtl
 */
public class OpeneCardServiceClientHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OpeneCardServiceClientHandler.class);

    private final Activity activity;
    private final OpeneCardServiceClient client;
    private final OpeneCardServiceHandler connectionHandler;

    public OpeneCardServiceClientHandler(Activity activity, OpeneCardServiceHandler handler) {
	this.activity = activity;
	this.client = new OpeneCardServiceClient(activity.getApplicationContext());
	this.connectionHandler = handler;
    }

    ///
    /// Public methods
    ///

    /**
     * Starts the service asynchronously and calls the appropriate callback functions once it is finished.
     */
    public void startService() {
	new Thread(() -> {
	    final ServiceResponse r = client.startService();
	    activity.runOnUiThread(() -> {
		mapStartServiceResult(r);
	    });
	}, "OeC Service Start").start();
    }

    /**
     * Stops the service asynchronously and calls the appropriate callback functions once it is finished.
     */
    public void stopService() {
	new Thread(() -> {
	    final ServiceResponse r = client.stopService();
	    activity.runOnUiThread(() -> {
		mapStopServiceResult(r);
	    });
	}, "OeC Service Stop").start();
    }

    public void unbindService() {
	client.unbindService();
    }

    /**
     * Returns whether the Open eCard Stack is initialized or not.
     * This value can also change when the service managing the stack is stopped from the outside.
     *
     * @return {@code true} if the stack is initialized, {@code false} otherwise.
     */
    public boolean isInitialized() {
	return client.isInitialized();
    }

    private void mapStartServiceResult(ServiceResponse startResponse) {
	switch (startResponse.getResponseLevel()) {
	    case INFO:
		connectionHandler.onConnectionSuccess(client.getContext());
		break;
	    case WARNING:
		connectionHandler.onConnectionFailure((ServiceWarningResponse) startResponse);
		break;
	    case ERROR:
		connectionHandler.onConnectionFailure((ServiceErrorResponse) startResponse);
		break;
	    default:
		break;
	}
    }

    private void mapStopServiceResult(ServiceResponse stopResponse) {
	switch (stopResponse.getResponseLevel()) {
	    case INFO:
		connectionHandler.onDisconnectionSuccess();
		break;
	    case WARNING:
		connectionHandler.onDisconnectionFailure((ServiceWarningResponse) stopResponse);
		break;
	    case ERROR:
		connectionHandler.onDisconnectionFailure((ServiceErrorResponse) stopResponse);
		break;
	    default:
		break;
	}
    }

}
