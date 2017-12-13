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
import org.openecard.android.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Mike Prechtl
 */
public class OpeneCardServiceClientHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OpeneCardServiceClientHandler.class);

    private final Activity activity;
    private final OpeneCardServiceClient client;
    private final ConnectionHandler connectionHandler;

    public OpeneCardServiceClientHandler(Activity activity, ConnectionHandler handler) {
	this.activity = activity;
	this.client = new OpeneCardServiceClient(activity);
	this.connectionHandler = handler;
    }

    ///
    /// Public methods
    ///

    public void startService() {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		final ServiceResponse r = client.startService();
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			mapStartServiceResult(r);
		    }
		});
	    }
	}, "OeC Service Start").start();
    }

    public void stopService() {
	new Thread(new Runnable() {
	    @Override
	    public void run() {
		final ServiceResponse r = client.stopService();
		activity.runOnUiThread(new Runnable() {
		    @Override
		    public void run() {
			mapStopServiceResult(r);
		    }
		});
	    }
	}, "OeC Service Stop").start();
    }

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
