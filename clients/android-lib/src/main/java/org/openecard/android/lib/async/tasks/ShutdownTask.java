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
import org.openecard.android.lib.AppConstants;
import org.openecard.android.lib.AppContext;
import org.openecard.android.lib.AppMessages;
import org.openecard.android.lib.AppResponse;
import org.openecard.android.lib.AppResponseStatusCodes;


/**
 * @author Mike Prechtl
 */
public class ShutdownTask extends AsyncTask<Void, Void, ShutdownTaskResponse> implements AppConstants, AppMessages,
		AppResponseStatusCodes {

	private final AppContext ctx;
	private final ShutdownTaskResult calling;

	public ShutdownTask(AppContext ctx, ShutdownTaskResult calling) {
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
		AppResponse response = null;
		switch (resultCode) {
			case SUCCESS:
				response = new AppResponse(OK, APP_TERMINATE_SUCCESS);
				break;
			case FAILURE:
				response = new AppResponse(SHUTDOWN_FAILED, APP_TERMINATE_FAILURE);
				break;
		}
		return new ShutdownTaskResponse(response);
	}

	@Override
	protected void onPostExecute(ShutdownTaskResponse response) {
		calling.setResultOfShutdownTask(response);
	}

}
