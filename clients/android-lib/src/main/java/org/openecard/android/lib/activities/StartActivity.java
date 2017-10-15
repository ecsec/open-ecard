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

package org.openecard.android.lib.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import org.openecard.android.lib.AppConstants;
import org.openecard.android.lib.AppContext;
import org.openecard.android.lib.async.tasks.StartTaskResponse;
import org.openecard.android.lib.async.tasks.StartTaskResult;
import org.openecard.android.lib.async.tasks.StartTask;
import org.openecard.android.lib.R;


/**
 * @author Mike Prechtl
 */
public class StartActivity extends Activity implements AppConstants, StartTaskResult {

	protected AppContext ctx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oe_activity_start);
	}

	@Override
	protected void onStart() {
		super.onStart();
		StartTask ctxTask = new StartTask(this);
		ctxTask.execute();
	}

	@Override
	public void setResultOfStartTask(StartTaskResponse response) {
		this.ctx = response.getCtx();
		Intent resultIntent = new Intent();
		resultIntent.putExtra(INTENT_KEY_FOR_RESPONSE, response.getResponse());
		setResult(RESULT_OK, resultIntent);
		finish();
	}

}
