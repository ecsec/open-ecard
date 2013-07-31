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

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import org.openecard.addon.AddonManager;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.sal.state.CardStateMap;
import org.openecard.control.ControlInterface;
import org.openecard.control.binding.http.HTTPBinding;
import org.openecard.control.handler.ControlHandlers;
import org.openecard.gui.UserConsent;
import org.openecard.recognition.CardRecognition;
import org.openecard.sal.TinySAL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Service starts the control interface. <br />
 * It get's started when the app is opened or when the boot completed event is received.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TCTokenService extends Service implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(AndroidUtils.class);

    private final IBinder mBinder = new MyBinder();
    // Control interface
    private ControlInterface control;

    public void run() {
	ApplicationContext appCtx = (ApplicationContext) getApplicationContext();
	HTTPBinding binding;
	try {
	    binding = new HTTPBinding(HTTPBinding.DEFAULT_PORT);
	    Dispatcher dispatcher = appCtx.getEnv().getDispatcher();
	    UserConsent gui = appCtx.getGUI();
	    CardStateMap cardStates = appCtx.getCardStates();
	    CardRecognition recognition = appCtx.getRecognition();
	    AddonManager addonManager = AddonManager.createInstance(dispatcher, gui, cardStates, recognition, appCtx.getEnv().getEventManager(),((TinySAL) appCtx.getEnv().getSAL()).getProtocolInfo());
	    binding.setAddonManager(addonManager);
	    ControlHandlers handler = new ControlHandlers();
	    control = new ControlInterface(binding, handler);
	    control.start();
	} catch (Exception e) {
	    logger.error("Starting of control interface failed", e);
	    System.exit(-1);
	}
    }

    // for pre-2.0 devices
    @Override
    public void onStart(Intent intent, int startId) {
	Thread t = new Thread(this);
	t.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
	Thread t = new Thread(this);
	t.start();
	return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
	return mBinder;
    }

    public class MyBinder extends Binder {
	TCTokenService getService() {
	    return TCTokenService.this;
	}
    }

}
