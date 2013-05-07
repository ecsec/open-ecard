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
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import java.net.URI;
import org.openecard.android.AndroidUtils;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.control.binding.intent.handler.IntentControlHandler;
import org.openecard.control.handler.ControlHandler;
import org.openecard.control.handler.ControlHandlers;
import org.openecard.scio.NFCCardTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Activity is registered for all Intents that point to http://localhost:24727.
 * <br />If such a Intent arrives it tries to start an appropriate ControlHandler depending on the requestUri.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class IntentHandlerActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(IntentHandlerActivity.class);
    private final I18n lang = I18n.getTranslation("android");

    private static ControlHandlers handlers;
    private ApplicationContext applicationContext;
    private Intent responseIntent;

    @Override
    protected void onDestroy() {
	logger.debug("onDestroy");
	super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(null);

	setContentView(R.layout.main);
	setResult(Activity.RESULT_OK);
	applicationContext = ((ApplicationContext) getApplicationContext());
	applicationContext.initialize(this);

	handleIntent(getIntent());

	displayText(lang.translationForKey("android.main.info"));
    }

    /**
     * Handles the intent the IntentHandlerActivity was started with.
     * <br /> It's action should equal Intent.ACTION_VIEW because
     * we've been started through a link to localhost.
     *
     * @param intent The intent the application was started with.
     */
    private void handleIntent(final Intent requestIntent) {
	final URI requestURI = URI.create(requestIntent.getDataString());

	Thread t = new Thread(new Runnable() {
	    @Override
	    public void run() {
		IntentControlHandler handler = findResponsibleHandler(requestURI);
		if (handler == null) {
		    logger.error("No handler found for the requestURI {}", requestURI);
		    IntentHandlerActivity.this.finish();
		}
		responseIntent = handler.handle(requestIntent);

		if (responseIntent.getAction().equals(Intent.ACTION_VIEW)) {
		    // authentication is finished; return the refresh URL Intent and finish
		    Intent refreshIntent = AndroidUtils.getRefreshIntent(responseIntent, applicationContext);
		    setResult(Activity.RESULT_OK, refreshIntent);
		    finish();
		} else if (responseIntent.getAction().equals(ECardConstants.Minor.SAL.CANCELLATION_BY_USER)) {
		    finish();
		} else {
		    try {
			int lengthOfLastAPDU = NFCCardTerminal.getInstance().getLengthOfLastAPDU();
			int maxTransceiveLength = NFCCardTerminal.getInstance().getMaxTransceiveLength();
			if (lengthOfLastAPDU > maxTransceiveLength && maxTransceiveLength > 0) {
			    runOnUiThread(new ExtendedLengthAlertDialog(IntentHandlerActivity.this));
			    return;
			}
		    } catch (Exception e) {
			// ignore
		    }
		    runOnUiThread(new UnexpectedErrorAlertDialog(IntentHandlerActivity.this));
		}
	    }
	});

	t.start();
    }

    private void displayText(final String text) {
	runOnUiThread(new Runnable() {

	    @Override
	    public void run() {
		TextView tv = (TextView) findViewById(R.id.textViewMain);
		tv.setText(text);
	    }
	});
    }

    public static void setHandlers(ControlHandlers handlers) {
	IntentHandlerActivity.handlers = handlers;
    }

    private IntentControlHandler findResponsibleHandler(URI requestURI) {
	for (ControlHandler handler : IntentHandlerActivity.handlers.getControlHandlers()) {
	    if (handler.getID().equals(requestURI.getPath())) {
		return (IntentControlHandler) handler;
	    }
	}
	return null;
    }

}
