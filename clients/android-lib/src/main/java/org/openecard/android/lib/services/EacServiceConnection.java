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
import org.openecard.android.lib.AppMessages;
import org.openecard.android.lib.AppResponse;
import org.openecard.android.lib.AppResponseStatusCodes;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.gui.android.eac.EacGuiImpl;
import org.openecard.gui.android.eac.EacGuiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Mike Prechtl
 */
public class EacServiceConnection implements ServiceConnection {

    private static final Logger LOG = LoggerFactory.getLogger(EacServiceConnection.class);

    private final ServiceConnectionResponseHandler responseHandler;
    private final Context ctx;

    private EacGui eacService;
    private boolean alreadyStarted = false;

    public EacServiceConnection(ServiceConnectionResponseHandler responseHandler, Context ctx) {
	this.ctx = ctx;
	this.responseHandler = responseHandler;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
	LOG.info("EAC Gui Service binded!");
	eacService = EacGuiImpl.Stub.asInterface(service);
	responseHandler.handleServiceConnectionResponse(buildServiceConnectedResponse());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
	LOG.info("EAC Gui Service unbinded.");
	responseHandler.handleServiceConnectionResponse(disconnectResponse);
    }

    public EacGui getEacGui() {
	return eacService;
    }


    ///
    /// Build App responses
    ///

    private AppResponse disconnectResponse;

    private AppResponse buildServiceConnectedResponse() {
	return new AppResponse(AppResponseStatusCodes.EAC_SERVICE_CONNECTED, AppMessages.APP_EAC_SERVICE_CONNECTED);
    }

    private AppResponse buildDisconnectResponse() {
	return new AppResponse(AppResponseStatusCodes.EAC_SERVICE_DISCONNECTED, AppMessages.APP_EAC_SERVICE_DISCONNECTED);
    }


    ///
    /// Public methods
    ///

    public synchronized void startService() {
	if (! alreadyStarted) {
	    Intent i = createEacGuiIntent();
	    LOG.info("Starting Eac Gui service...");
	    ctx.startService(i);
	    LOG.info("Binding Eac Gui service...");
	    ctx.bindService(i, this, 0);
	    alreadyStarted = true;
	} else {
	    throw new IllegalStateException("Service already started...");
	}
    }

    public synchronized void stopService() {
	if (alreadyStarted) {
	    Intent i = createEacGuiIntent();
	    alreadyStarted = false;
	    disconnectResponse = buildDisconnectResponse();
	    ctx.stopService(i);
	    LOG.info("Unbinding Eac Gui service...");
	    ctx.unbindService(this);
	} else {
	    throw new IllegalStateException("Service already stopped...");
	}
    }

    public synchronized boolean isServiceAlreadyStarted() {
	return alreadyStarted;
    }

    private Intent createEacGuiIntent() {
	return new Intent(ctx, EacGuiService.class);
    }

}
