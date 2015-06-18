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

package org.openecard.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


/**
 * This BroadcastReceiver will receive the ACTION_BOOT_COMPLETED intent and start the TCTokenService
 *
 * @author Dirk Petrautzki
 */
public class StartServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
	if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
	    Intent i = new Intent(context, TCTokenService.class);
	    context.startService(i);
	}
    }

}
