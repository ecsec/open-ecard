/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

package org.openecard.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import java.util.Locale;
import org.openecard.android.AndroidUtils;
import org.openecard.common.I18n;
import org.openecard.scio.NFCCardTerminal;


/**
 * The ExtendedLengthAlertDialog shows an AlertDialog in it's run-method describing the extended length Problem and
 * gives the user the possibility to show more information or close the app.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class ExtendedLengthAlertDialog implements Runnable {

    private final Activity activity;
    private final I18n lang = I18n.getTranslation("android");

    ExtendedLengthAlertDialog(Activity activity) {
	this.activity = activity;
    }

    @Override
    public void run() {
	AlertDialog ad = new AlertDialog.Builder(this.activity).create();
	ad.setCancelable(false); // This blocks the 'BACK' button
	int lengthOfLastAPDU = NFCCardTerminal.getInstance().getLengthOfLastAPDU();
	int maxTransceiveLength = NFCCardTerminal.getInstance().getMaxTransceiveLength();

	// add description of the error
	ad.setMessage(lang.translationForKey("android.error.ext_apdu", lengthOfLastAPDU, maxTransceiveLength));

	// Add close button
	ad.setButton(lang.translationForKey("android.dialogs.quit"), new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		activity.finish();
	    }
	});

	// Add more info button
	ad.setButton2(lang.translationForKey("android.error.ext_apdu.more"), new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		Intent i;
		Locale locale = Locale.getDefault();
		String lang = locale.getLanguage();
		Uri uri;
		if (lang.equalsIgnoreCase("de")) {
		    uri = Uri.parse("https://www.openecard.org/de/framework/extendedlength");
		} else {
		    uri = Uri.parse("https://www.openecard.org/en/framework/extendedlength");
		}
		i = new Intent(Intent.ACTION_VIEW, uri);
		i = AndroidUtils.getRefreshIntent(i, activity);
		activity.startActivity(i);
		dialog.dismiss();
		activity.finish();
	    }
	});

	// finally show the dialog
	ad.show();
    }

}
