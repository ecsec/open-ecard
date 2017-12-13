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

import org.openecard.android.ServiceResponse;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import javax.annotation.Nullable;
import org.openecard.android.OpeneCardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.android.system.ServiceConstants.*;
import static org.openecard.android.ServiceMessages.*;
import static org.openecard.android.ServiceResponseStatusCodes.*;
import org.openecard.android.ex.ApduExtLengthNotSupported;
import org.openecard.android.ex.NfcDisabled;
import org.openecard.android.ex.NfcUnavailable;
import org.openecard.android.ex.UnableToInitialize;


/**
 * @author Mike Prechtl
 */
public class OpeneCardServiceImpl extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(OpeneCardServiceInt.class);

    private static OpeneCardContext context;
    private boolean isRequiredAPIUsed = false;

    @Nullable
    public static OpeneCardContext getContext() {
	return context;
    }

    @Override
    public void onCreate() {
	super.onCreate();
	isRequiredAPIUsed = Build.VERSION.SDK_INT >= REQUIRED_API;
    }

    @Override
    public void onDestroy() {
	try {
	    mBinder.stopService();
	} catch (RemoteException ex) {
	    LOG.error("Error stopping Open eCard Service.", ex);
	}
	super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
	return mBinder;
    }

    private final OpeneCardService.Stub mBinder = new OpeneCardServiceInt();

    ///
    /// Service Implementation
    ///

    private class OpeneCardServiceInt extends OpeneCardService.Stub {

	@Override
	public ServiceResponse startService() throws RemoteException {
	    if (context == null) {
		LOG.info("Start Open eCard Service...");
		ServiceResponse response;
		try {
		    // build response whether the initialization of app context was successful or failed.
		    if (isRequiredAPIUsed) {
			context = initializeContext();
			response = new ServiceResponse(INIT_SUCCESS, SERVICE_RESPONSE_OK);
		    } else {
			LOG.warn(BELOW_API_LEVEL_21_NOT_SUPPORTED);
			response = new ServiceErrorResponse(NOT_REQUIRED_API_LEVEL, BELOW_API_LEVEL_21_NOT_SUPPORTED);
		    }
		} catch (UnableToInitialize ex) {
		    response = new ServiceErrorResponse(INTERNAL_ERROR, ex.getMessage());
		} catch (NfcDisabled ex) {
		    response = new ServiceWarningResponse(NFC_NOT_ENABLED, ex.getMessage());
		} catch (NfcUnavailable ex) {
		    response = new ServiceErrorResponse(NFC_NOT_AVAILABLE, ex.getMessage());
		} catch (ApduExtLengthNotSupported ex) {
		    response = new ServiceErrorResponse(NFC_NO_EXTENDED_LENGTH, ex.getMessage());
		}
		return response;
	    } else {
		LOG.info("Service already started, nothing to do here.");
		return new ServiceResponse(INIT_SUCCESS, SERVICE_RESPONSE_OK);
	    }
	}

	@Override
	public ServiceResponse stopService() throws RemoteException {
	    if (context != null) {
		LOG.info("Stop Open eCard Service...");
		String resultCode = context.shutdown();
		context = null;
		ServiceResponse response = null;
		switch (resultCode) {
		    case SUCCESS:
			response = new ServiceResponse(SHUTDOWN_SUCCESS, SERVICE_TERMINATE_SUCCESS);
			break;
		    case FAILURE:
			response = new ServiceWarningResponse(SHUTDOWN_FAILED, SERVICE_TERMINATE_FAILURE);
			break;
		}
		stopSelf();
		return response;
	    } else {
		LOG.info("Stop Open eCard Service not necessary.");
		return new ServiceResponse(SHUTDOWN_SUCCESS, SERVICE_TERMINATE_SUCCESS);
	    }
	}
	
	private OpeneCardContext initializeContext() throws UnableToInitialize, NfcUnavailable, NfcDisabled, ApduExtLengthNotSupported {
	    OpeneCardContext octx = new OpeneCardContext(getApplicationContext());
	    octx.initialize();
	    return octx;
	}

    }

}
