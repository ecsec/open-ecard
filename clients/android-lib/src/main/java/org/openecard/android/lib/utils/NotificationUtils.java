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

package org.openecard.android.lib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;


/**
 * @author Mike Prechtl
 */
public class NotificationUtils {

    private static final String STD_NOTIFICATION_TITLE = "Open eCard";

    public static void showNotification(Activity activity, Context context, String title, String message) {
	NotificationCompat.Builder mBuilder
		= new NotificationCompat.Builder(context)
			.setContentTitle(title)
			.setContentText(message);
	// Creates an explicit intent for an Activity in your app
	Intent resultIntent = new Intent(context, activity.getClass());
    }

    public static void showNotification(Activity activity, Context context, String message) {
	showNotification(activity, context, STD_NOTIFICATION_TITLE, message);
    }

}
