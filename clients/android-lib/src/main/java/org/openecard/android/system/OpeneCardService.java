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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.openecard.android.ServiceConstants.FAILURE;
import static org.openecard.android.ServiceConstants.REQUIRED_API;
import static org.openecard.android.ServiceConstants.SUCCESS;
import static org.openecard.android.ServiceMessages.BELOW_API_LEVEL_21_NOT_SUPPORTED;
import static org.openecard.android.ServiceMessages.SERVICE_RESPONSE_OK;
import static org.openecard.android.ServiceMessages.SERVICE_TERMINATE_FAILURE;
import static org.openecard.android.ServiceMessages.SERVICE_TERMINATE_SUCCESS;
import static org.openecard.android.ServiceResponseStatusCodes.INIT_SUCCESS;
import static org.openecard.android.ServiceResponseStatusCodes.INTERNAL_ERROR;
import static org.openecard.android.ServiceResponseStatusCodes.NFC_NOT_AVAILABLE;
import static org.openecard.android.ServiceResponseStatusCodes.NFC_NOT_ENABLED;
import static org.openecard.android.ServiceResponseStatusCodes.NFC_NO_EXTENDED_LENGTH;
import static org.openecard.android.ServiceResponseStatusCodes.NOT_REQUIRED_API_LEVEL;
import static org.openecard.android.ServiceResponseStatusCodes.SHUTDOWN_FAILED;
import static org.openecard.android.ServiceResponseStatusCodes.SHUTDOWN_SUCCESS;
import org.openecard.android.ex.ApduExtLengthNotSupported;
import org.openecard.android.ex.NfcDisabled;
import org.openecard.android.ex.NfcUnavailable;
import org.openecard.android.ex.UnableToInitialize;


/**
 * @author Mike Prechtl
 */
public class OpeneCardService extends Service {

    private static final Logger LOG = LoggerFactory.getLogger(OpeneCardServiceImpl.class);

    boolean isRequiredAPIUsed = false;

    @Override
    public void onCreate() {
	super.onCreate();
	isRequiredAPIUsed = Build.VERSION.SDK_INT >= REQUIRED_API;
    }

    @Override
    public IBinder onBind(Intent intent) {
	return mBinder;
    }

    private final org.openecard.android.OpeneCardService.Stub mBinder = new OpeneCardServiceImpl();

    ///
    /// Service Implementation
    ///

    private class OpeneCardServiceImpl extends org.openecard.android.OpeneCardService.Stub {

	@Override
	public ServiceResponse start() throws RemoteException {
	    LOG.info("Start Open eCard Service...");
	    ServiceResponse response;
	    try {
		initializeContext();
		// build response whether the initialization of app context was successful or failed.
		if (isRequiredAPIUsed) {
		    response = new ServiceResponse(INIT_SUCCESS, SERVICE_RESPONSE_OK);
		} else {
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
	}

	@Override
	public ServiceResponse stop() throws RemoteException {
	    LOG.info("Stop Open eCard Service...");
	    OpeneCardContext ctx = OpeneCardContext.getContext();
	    String resultCode = ctx.shutdown();
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
	}
    }

    private OpeneCardContext initializeContext() throws UnableToInitialize, NfcUnavailable, NfcDisabled, ApduExtLengthNotSupported {
	OpeneCardContext octx = OpeneCardContext.getContext();;
	if (isRequiredAPIUsed) {
	    if (! octx.isInitialized()) {
		octx.initialize();
	    }
	} else {
	    LOG.warn(BELOW_API_LEVEL_21_NOT_SUPPORTED);
	}
	return octx;
    }

}
