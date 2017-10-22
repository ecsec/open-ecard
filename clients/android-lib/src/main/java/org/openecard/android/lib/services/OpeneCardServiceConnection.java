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

package org.openecard.android.lib.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import org.openecard.android.lib.AppContext;
import org.openecard.android.lib.AppMessages;
import org.openecard.android.lib.AppResponse;
import org.openecard.android.lib.AppResponseStatusCodes;
import org.openecard.android.lib.OpeneCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Mike Prechtl
 */
public class OpeneCardServiceConnection implements ServiceConnection {

    private static final Logger LOG = LoggerFactory.getLogger(OpeneCardServiceConnection.class);

    private final ServiceConnectionResponseHandler responseHandler;
    private final Context ctx;

    private OpeneCardService mService;
    private boolean alreadyStarted = false;

    public OpeneCardServiceConnection(ServiceConnectionResponseHandler responseHandler, Context ctx) {
	this.ctx = ctx;
	this.responseHandler = responseHandler;
    }

    ///
    /// Service connect and disconnect
    ///

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
	LOG.info("Service binded!");
	mService = OpeneCardService.Stub.asInterface(service);
	try {
	    AppResponse response = mService.start();
	    responseHandler.handleServiceConnectionResponse(response);
	} catch (RemoteException ex) {
	    responseHandler.handleServiceConnectionResponse(buildErrorResponse(ex));
	}
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
	mService = null;
	responseHandler.handleServiceConnectionResponse(disconnectResponse);
    }

    ///
    /// Build App responses
    ///

    private AppResponse disconnectResponse;

    private AppResponse buildErrorResponse(Exception ex) {
	return new AppResponse(AppResponseStatusCodes.INTERNAL_ERROR, ex.getMessage());
    }

    private AppResponse buildDisconnectResponse(Exception ex) {
	return ex == null ? new AppResponse(AppResponseStatusCodes.INIT_SUCCESS, AppMessages.APP_TERMINATE_SUCCESS)
		: new AppResponse(AppResponseStatusCodes.INTERNAL_ERROR, ex.getMessage());
    }

    ///
    /// Public methods
    ///

    public synchronized void startService() {
	if (! alreadyStarted) {
	    Intent i = createOpeneCardIntent();
	    LOG.info("Starting service…");
	    ctx.startService(i);
	    LOG.info("Binding service…");
	    ctx.bindService(i, this, AppContext.BIND_AUTO_CREATE);
	    alreadyStarted = true;
	} else {
	    throw new IllegalStateException("Service already started...");
	}
    }

    public synchronized void stopService() {
	if (alreadyStarted) {
	    try {
		Intent i = createOpeneCardIntent();
		mService.stop();
		alreadyStarted = false;
		disconnectResponse = buildDisconnectResponse(null);
		ctx.stopService(i);
		ctx.unbindService(this);
	    } catch (RemoteException ex) {
		disconnectResponse = buildDisconnectResponse(ex);
	    }
	} else {
	    throw new IllegalStateException("Service already stopped...");
	}
    }

    public synchronized boolean isServiceAlreadyStarted() {
	return alreadyStarted;
    }

    private Intent createOpeneCardIntent() {
	return new Intent(ctx, OpeneCardServiceImpl.class);
    }

}
