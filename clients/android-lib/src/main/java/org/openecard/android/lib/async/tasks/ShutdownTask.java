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
import org.openecard.android.lib.ServiceContext;
import static org.openecard.android.lib.ServiceMessages.*;
import org.openecard.android.lib.ServiceResponse;
import static org.openecard.android.lib.ServiceConstants.*;
import static org.openecard.android.lib.ServiceResponseStatusCodes.*;
import org.openecard.android.lib.ServiceWarningResponse;


/**
 * @author Mike Prechtl
 */
public class ShutdownTask extends AsyncTask<Void, Void, ShutdownTaskResponse> {

    private final ServiceContext ctx;
    private final ShutdownTaskResult calling;

    public ShutdownTask(ServiceContext ctx, ShutdownTaskResult calling) {
	if (calling instanceof ContextWrapper) {
	    this.ctx = ctx;
	    this.calling = calling;
	} else {
	    throw new IllegalArgumentException("ShutdownTaskResult has to be implemented by a ContextWrapper.");
	}
    }

    @Override
    protected ShutdownTaskResponse doInBackground(Void... voids) {
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
	return new ShutdownTaskResponse(response);
    }

    @Override
    protected void onPostExecute(ShutdownTaskResponse response) {
	calling.setResultOfShutdownTask(response);
    }

}
