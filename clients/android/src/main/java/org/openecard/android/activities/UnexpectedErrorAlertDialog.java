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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import org.openecard.common.I18n;


/**
 * The UnexpectedErrorAlertDialog shows an AlertDialog in it's run-method describing that there was an unexpected
 * Problem and gives the user the possibility to view the log of the App.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class UnexpectedErrorAlertDialog implements Runnable {

    private final MainActivity ctx;
    private final I18n lang = I18n.getTranslation("android");

    UnexpectedErrorAlertDialog(MainActivity ctx) {
	this.ctx = ctx;
    }

    @Override
    public void run() {
	AlertDialog ad = new AlertDialog.Builder(ctx).create();
	ad.setCancelable(false); // This blocks the 'BACK' button

	// add description of the error
	ad.setMessage(lang.translationForKey("android.error.unexpected"));

	// Add close button
	ad.setButton(lang.translationForKey("android.dialogs.quit"), new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		ctx.finish();
	    }
	});

	// Add more info button
	ad.setButton2(lang.translationForKey("android.error.ext_apdu.more"), new DialogInterface.OnClickListener() {
	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		Intent intent = new Intent(ctx, LogViewerActivity.class);
		ctx.startActivity(intent);
	    }
	});

	// finally show the dialog
	ad.show();
    }

}
