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
import android.net.Uri;
import android.os.Bundle;
import org.openecard.addon.bind.AuxDataKeys;
import org.openecard.addon.bind.BindingResult;
import org.openecard.android.lib.async.tasks.BindingTaskResponse;
import org.openecard.android.lib.async.tasks.BindingTaskResult;
import org.openecard.android.lib.ex.BindingTaskStillRunning;
import org.openecard.android.lib.ex.ContextNotInitialized;
import org.openecard.android.lib.intent.binding.IntentBinding;


/**
 * @author Mike Prechtl
 */
public class EacActivity extends NfcActivity implements BindingTaskResult {

    private final Activity callingActivity;

    private volatile boolean alreadyInitialized = false;

    public EacActivity(Activity activity) {
	super(activity);
	this.callingActivity = activity;
    }

    public String getBindingURI(Intent i) {
	Uri data = i.getData();
        return data.toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    }

    @Override
    public synchronized void onResume() {
	super.onResume();
    }

    @Override
    public synchronized void onPause() {
	super.onPause();
    }

    public void onStart() {
	if (! alreadyInitialized) {
	    IntentBinding binding = IntentBinding.getInstance();
	    binding.setContextWrapper(this);
	    this.alreadyInitialized = true;
	}
    }

    public void onStop() {
	IntentBinding binding = IntentBinding.getInstance();
	binding.cancelRequest();
    }

    public void onDestroy() {
	if (alreadyInitialized) {
	    alreadyInitialized = false;
	}
    }

    @Override
    public void onNewIntent(Intent intent) {
	super.onNewIntent(intent);
    }

    public synchronized void handleRequest(String uri) throws ContextNotInitialized, BindingTaskStillRunning {
	IntentBinding binding = IntentBinding.getInstance();
	binding.handleRequest(uri);
    }

    public synchronized void cancelRequest() {
	IntentBinding binding = IntentBinding.getInstance();
	binding.cancelRequest();
    }

    @Override
    public void setResultOfBindingTask(BindingTaskResponse response) {
	redirectToResultLocation(response.getBindingResult());
    }

    public void redirectToResultLocation(BindingResult result) {
	String location = result.getAuxResultData().get(AuxDataKeys.REDIRECT_LOCATION);
	if (location != null) {
	    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
	    callingActivity.startActivity(i);
	}
    }

}
