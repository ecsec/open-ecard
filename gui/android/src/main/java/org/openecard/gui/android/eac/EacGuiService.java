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

package org.openecard.gui.android.eac;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import org.openecard.common.util.Promise;


/**
 *
 * @author Tobias Wich
 */
public class EacGuiService extends Service {

    private static Promise<EacGuiImpl> serviceImpl;

    public static void prepare(Context androidCtx) {
	// clean promise
	serviceImpl = new Promise<>();

	// start service
	androidCtx.startService(createGuiIntent());
    }

    public static void shutdown(Context androidCtx) {
	serviceImpl = null;
	androidCtx.stopService(createGuiIntent());
    }

    public static synchronized Promise<EacGuiImpl> getServiceImpl() {
	return serviceImpl;
    }

    private static Intent createGuiIntent() {
	Intent i = new Intent(EacGuiService.class.getName());
	return i;
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {
	serviceImpl.deliver(new EacGuiImpl());
	return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
	try {
	    return serviceImpl.deref();
	} catch (InterruptedException ex) {
	    throw new RuntimeException("Waiting for EacGuiImpl interrupted.");
	}
    }

}
