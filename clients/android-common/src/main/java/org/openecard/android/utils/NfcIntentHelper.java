/****************************************************************************
 * Copyright (C) 2019 ecsec GmbH.
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
package org.openecard.android.utils;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.Settings;
import static org.openecard.android.utils.NfcCapabilityHelper.LOG;

/**
 * Provides methods to enable/disable the nfc dispatch or to jump to the nfc settings.
 *
 * @author Mike Prechtl
 * @author Neil Crossley
 */
public class NfcIntentHelper {

    private final NfcCapabilityHelper<Activity> capabilityHelper;

    public NfcIntentHelper(NfcCapabilityHelper<Activity> innerHelper) {
	this.capabilityHelper = innerHelper;
    }

    public static NfcIntentHelper create(Activity activity) {
	if (activity == null) {
	    throw new IllegalArgumentException("activity cannot be null");
	}

	NfcCapabilityHelper<Activity> innerHelper = NfcCapabilityHelper.create(activity);

	return new NfcIntentHelper(innerHelper);
    }

    /**
     * This method opens the nfc settings on the corresponding device where the user can enable nfc.
     */
    public void goToNFCSettings() {
	Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
	this.capabilityHelper.getContext().startActivityForResult(intent, 0);
    }

    public void enableNFCDispatch() {
	if (this.capabilityHelper.isNFCEnabled()) {
	    LOG.debug("Enable NFC foreground dispatch...");
	    Activity activity = this.capabilityHelper.getContext();
	    Intent activityIntent = new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    int flags = android.os.Build.VERSION.SDK_INT >= 31 ? android.app.PendingIntent.FLAG_MUTABLE : 0;
	    PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, activityIntent, flags);
	    // enable dispatch of messages with nfc tag
	    this.capabilityHelper.getNfcAdapter().enableForegroundDispatch(activity, pendingIntent, null, null);
	}
    }

    public void disableNFCDispatch() {
	if (this.capabilityHelper.isNFCEnabled()) {
	    LOG.debug("Disable NFC foreground dispatch...");
	    // disable dispatch of messages with nfc tag
	    this.capabilityHelper.getNfcAdapter().disableForegroundDispatch(this.capabilityHelper.getContext());
	}
    }

}
