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
import org.openecard.android.lib.ServiceContext;
import org.openecard.android.lib.ex.ApduExtLengthNotSupported;
import org.openecard.android.lib.utils.NfcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An activity which disables NFC if the activity is disabled. NFC is enabled if the activity is enabled.
 *
 * @author Mike Prechtl
 */
public class NfcActivity {

    private static final Logger LOG = LoggerFactory.getLogger(NfcActivity.class);

    private final Activity callingActivity;

    public NfcActivity(Activity activity) {
	this.callingActivity = activity;
    }

    public void onCreate(Bundle savedInstanceState) {
	NfcUtils.getInstance().setServiceContext(ServiceContext.getServiceContext());
    }

    public synchronized void onResume() {
	NfcUtils.getInstance().enableNFCDispatch(callingActivity);
    }

    public synchronized void onPause() {
	try {
	    NfcUtils.getInstance().disableNFCDispatch(callingActivity);
	} catch (Exception e) {
	    LOG.info(e.getMessage(), e);
	}
    }

    public void onNewIntent(Intent intent) {
	try {
	    NfcUtils.getInstance().retrievedNFCTag(intent);
	} catch (ApduExtLengthNotSupported ex) {
	    LOG.error(ex.getMessage());
	}
    }

}
