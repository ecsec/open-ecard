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
import android.content.ServiceConnection;
import android.os.IBinder;
import org.openecard.android.lib.AppMessages;
import org.openecard.android.lib.AppResponse;
import org.openecard.android.lib.AppResponseStatusCodes;
import org.openecard.common.util.Promise;
import org.openecard.gui.android.eac.EacGui;
import org.openecard.gui.android.eac.EacGuiImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Mike Prechtl
 */
public class EacServiceConnection implements ServiceConnection {

    private static final Logger LOG = LoggerFactory.getLogger(EacServiceConnection.class);

    private final ServiceConnectionResponseHandler responseHandler;

    private Promise<EacGui> eacServiceImpl = new Promise<>();

    public EacServiceConnection(ServiceConnectionResponseHandler responseHandler) {
	this.responseHandler = responseHandler;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
	LOG.info("EAC Gui Service binded!");
	EacGui eacService = EacGuiImpl.Stub.asInterface(service);
	eacServiceImpl.deliver(eacService);
	responseHandler.handleServiceConnectionResponse(buildServiceConnectedResponse());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
	LOG.info("EAC Gui Service unbinded.");
	eacServiceImpl = new Promise<>();
    }

    public EacGui getEacGui() {
	try {
	    return eacServiceImpl.deref();
	} catch (InterruptedException ex) {
	    throw new RuntimeException("Waiting for EacGui interrupted.", ex);
	}
    }

    private AppResponse buildServiceConnectedResponse() {
	return new AppResponse(AppResponseStatusCodes.OK, AppMessages.APP_EAC_SERVICE_CONNECTED);
    }

}
