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

package org.openecard.android.lib.async.tasks;

import android.content.ContextWrapper;
import android.os.AsyncTask;
import android.os.Build;
import static org.openecard.android.lib.ServiceConstants.*;
import org.openecard.android.lib.ServiceContext;
import org.openecard.android.lib.ServiceResponse;
import org.openecard.android.lib.ex.NfcDisabled;
import org.openecard.android.lib.ex.NfcUnavailable;
import org.openecard.android.lib.ex.UnableToInitialize;
import org.openecard.android.lib.utils.NfcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openecard.android.lib.ServiceErrorResponse;
import static org.openecard.android.lib.ServiceMessages.*;
import static org.openecard.android.lib.ServiceResponseStatusCodes.*;
import org.openecard.android.lib.ServiceWarningResponse;
import org.openecard.android.lib.ex.ApduExtLengthNotSupported;


/**
 * This async task will create the app context. The ifd, addon manager, sal, ... will be initialized in the app context
 * (see {@link ServiceContext})
 *
 * @author Mike Prechtl
 */
public class StartTask extends AsyncTask<Void, Void, StartTaskResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(StartTask.class);

    private final StartTaskResult calling;
    private final boolean isRequiredAPIUsed;

    public StartTask(StartTaskResult calling) {
	if (calling instanceof ContextWrapper) {
	    this.calling = calling;
	    this.isRequiredAPIUsed = Build.VERSION.SDK_INT >= REQUIRED_API;
	} else {
	    throw new IllegalArgumentException("StartTaskResult has to be implemented by a ContextWrapper.");
	}
    }

    @Override
    protected StartTaskResponse doInBackground(Void... voids) {
	ServiceContext ctx = null;
	ServiceResponse response;
	try {
	    ctx = getServiceContext();
	    NfcUtils.getInstance().setServiceContext(ctx); // set app context in nfc utils
	    // build response whether the initialization of app context was successful or failed.
	    if (isRequiredAPIUsed) {
		response = new ServiceResponse(INIT_SUCCESS, APP_RESPONSE_OK);
	    } else {
		response = new ServiceErrorResponse(NOT_REQUIRED_API_LEVEL, APP_API_LEVEL_21_NOT_SUPPORTED);
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
	return new StartTaskResponse(ctx, response);
    }

    private ServiceContext getServiceContext() throws UnableToInitialize, NfcUnavailable, NfcDisabled,
	    ApduExtLengthNotSupported {
	ServiceContext ctx = null;
	if (isRequiredAPIUsed) {
	    ctx = (ServiceContext) ((ContextWrapper) calling).getApplicationContext();
	    if (! ctx.isInitialized()) {
		ctx.initialize();
	    }
	} else {
	    LOG.warn(APP_API_LEVEL_21_NOT_SUPPORTED);
	}
	return ctx;
    }

    @Override
    protected void onPostExecute(StartTaskResponse result) {
	calling.setResultOfStartTask(result);
    }

}
