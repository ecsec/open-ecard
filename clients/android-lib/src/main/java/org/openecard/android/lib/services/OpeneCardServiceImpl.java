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

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import org.openecard.android.lib.ServiceContext;
import org.openecard.android.lib.ServiceResponse;
import org.openecard.android.lib.OpeneCardService;
import org.openecard.android.lib.async.tasks.ShutdownTask;
import org.openecard.android.lib.async.tasks.ShutdownTaskResponse;
import org.openecard.android.lib.async.tasks.ShutdownTaskResult;
import org.openecard.android.lib.async.tasks.StartTask;
import org.openecard.android.lib.async.tasks.StartTaskResponse;
import org.openecard.android.lib.async.tasks.StartTaskResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ExecutionException;
import org.openecard.android.lib.ServiceErrorResponse;
import org.openecard.android.lib.ServiceResponseStatusCodes;


/**
 * @author Mike Prechtl
 */
public class OpeneCardServiceImpl extends Service implements StartTaskResult, ShutdownTaskResult {

    private static final Logger LOG = LoggerFactory.getLogger(OpeneCardServiceImpl.class);

    @Override
    public void onCreate() {
	super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
	return mBinder;
    }

    @Override
    public void setResultOfShutdownTask(ShutdownTaskResponse response) {
	// do nothing
    }

    @Override
    public void setResultOfStartTask(StartTaskResponse response) {
	// do nothing
    }

    private final OpeneCardService.Stub mBinder = new OpeneCardServiceImplStub(this);

    ///
    /// Service Implementation
    ///

    private class OpeneCardServiceImplStub extends OpeneCardService.Stub {

	private final Service service;

	private OpeneCardServiceImplStub(Service service) {
	    this.service = service;
	}

	@Override
	public ServiceResponse start() throws RemoteException {
	    LOG.info("Start Open eCard Service...");
	    StartTask task = new StartTask((StartTaskResult) service);
	    try {
		StartTaskResponse response = task.execute().get();
		return response.getResponse();
	    } catch (ExecutionException | InterruptedException ex) {
		LOG.warn(ex.getMessage(), ex);
		return new ServiceErrorResponse(ServiceResponseStatusCodes.INTERNAL_ERROR, ex.getMessage());
	    }
	}

	@Override
	public ServiceResponse stop() throws RemoteException {
	    LOG.info("Stop Open eCard Service...");
	    ServiceContext ctx = (ServiceContext) service.getApplicationContext();
	    ShutdownTask task = new ShutdownTask(ctx, (ShutdownTaskResult) service);
	    try {
		ShutdownTaskResponse response = task.execute().get();
		stopSelf();
		return response.getResponse();
	    } catch (ExecutionException | InterruptedException ex) {
		LOG.warn(ex.getMessage(), ex);
		stopSelf();
		return new ServiceErrorResponse(ServiceResponseStatusCodes.INTERNAL_ERROR, ex.getMessage());
	    }
	}
    }

}
