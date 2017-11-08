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
import java.util.HashMap;
import java.util.List;
import org.openecard.android.lib.ServiceErrorResponse;
import org.openecard.android.lib.ServiceResponse;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.gui.android.eac.EacGuiImpl;
import org.openecard.gui.android.eac.EacGuiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openecard.android.lib.ServiceResponseStatusCodes;
import org.openecard.android.lib.ServiceMessages;
import org.openecard.android.lib.ServiceWarningResponse;
import org.openecard.gui.android.eac.types.BoxItem;
import org.openecard.gui.android.eac.types.ServerData;


/**
 *
 * @author Mike Prechtl
 */
public class EacServiceConnection {

    private static final Logger LOG = LoggerFactory.getLogger(EacServiceConnection.class);

    private static final HashMap<EacServiceConnectionHandler, EacServiceConnection> CONNECTIONS = new HashMap<>();

    private final EacServiceConnectionHandler responseHandler;
    private final Context ctx;

    private EacGui eacService;
    private boolean alreadyConnected = false;

    public EacServiceConnection(EacServiceConnectionHandler responseHandler, Context ctx) {
	this.ctx = ctx;
	this.responseHandler = responseHandler;
    }

    public static EacServiceConnection createConnection(EacServiceConnectionHandler responseHandler, Context ctx) {
	EacServiceConnection connection;
	synchronized (EacServiceConnection.class) {
	    if (CONNECTIONS.containsKey(responseHandler)) {
		connection = CONNECTIONS.get(responseHandler);
	    } else {
		connection = new EacServiceConnection(responseHandler, ctx);
		CONNECTIONS.put(responseHandler, connection);
	    }
	}
	return connection;
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
	@Override
	public void onServiceConnected(ComponentName componentName, IBinder service) {
	    LOG.info("EAC Gui Service binded!");
	    eacService = EacGuiImpl.Stub.asInterface(service);
	    try {
		ServerData serverData = eacService.getServerData();
		responseHandler.onConnectionSuccess();
		responseHandler.onServerDataPresent(serverData);
	    } catch (RemoteException ex) {
		responseHandler.onConnectionFailure(buildErrorResponse(ex));
	    }
	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {
	    eacService = null;
	    switch (disconnectResponse.getResponseLevel()) {
		case INFO:
		    responseHandler.onDisconnectionSuccess();
		    break;
		case WARNING:
		    responseHandler.onDisconnectionFailure((ServiceWarningResponse) disconnectResponse);
		    break;
		case ERROR:
		    responseHandler.onDisconnectionFailure((ServiceErrorResponse) disconnectResponse);
		    break;
		default:
		    break;
	    }
	}
    };

    /*public EacGui getEacGui() {
	return eacService;
    }*/


    ///
    /// Build App responses
    ///

    private ServiceResponse disconnectResponse;

    private ServiceResponse buildDisconnectResponse() {
	return new ServiceResponse(ServiceResponseStatusCodes.EAC_SERVICE_DISCONNECTED, ServiceMessages.APP_EAC_SERVICE_DISCONNECTED);
    }

    private ServiceErrorResponse buildErrorResponse(Exception e) {
	return new ServiceErrorResponse(ServiceResponseStatusCodes.INTERNAL_ERROR, e.getMessage());
    }

    ///
    /// Public methods
    ///

    public synchronized void startService() {
	if (! alreadyConnected) {
	    Intent i = createEacGuiIntent();
	    LOG.info("Starting Eac Gui service...");
	    ctx.startService(i);
	    LOG.info("Binding Eac Gui service...");
	    ctx.bindService(i, serviceConnection, 0);
	    alreadyConnected = true;
	} else {
	    throw new IllegalStateException("Service already started...");
	}
    }

    public synchronized void stopService() {
	if (alreadyConnected) {
	    Intent i = createEacGuiIntent();
	    alreadyConnected = false;
	    disconnectResponse = buildDisconnectResponse();
	    ctx.stopService(i);
	    LOG.info("Unbinding Eac Gui service...");
	    ctx.unbindService(serviceConnection);
	} // else do nothing, because the service hasn't been started yet, maybe because the user canceled the request.
    }

    public synchronized void selectAttributes(List<BoxItem> readAccessAttributes, List<BoxItem> writeAccessAttributes) {
	try {
	    eacService.selectAttributes(readAccessAttributes, writeAccessAttributes);
	    String status = eacService.getPinStatus();
	    if (status.equals("PIN")) {
		responseHandler.onPINIsRequired();
	    } else {
		String msg = String.format("PIN Status '{0}' isn't supported yet.", status);
		throw new UnsupportedOperationException(msg);
	    }
	} catch (RemoteException ex) {
	    responseHandler.onRemoteError(buildErrorResponse(ex));
	}
    }

    public synchronized void enterPIN(String can, String pin) {
	try {
	    boolean pinCorrect = eacService.enterPin(can, pin);
	    if (pinCorrect) {
		responseHandler.onPINInputSuccess();
	    } else {
		responseHandler.onPINInputFailure();
	    }
	} catch (RemoteException ex) {
	    responseHandler.onRemoteError(buildErrorResponse(ex));
	}
    }

    public synchronized boolean isConnected() {
	return alreadyConnected;
    }

    private Intent createEacGuiIntent() {
	return new Intent(ctx, EacGuiService.class);
    }

}
