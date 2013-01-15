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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.TextView;
import java.net.URI;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.openecard.common.ECardConstants;
import org.openecard.common.I18n;
import org.openecard.control.binding.intent.handler.IntentControlHandler;
import org.openecard.control.handler.ControlHandler;
import org.openecard.control.handler.ControlHandlers;


/**
 * This is the main Activity. It is the first activity to open when the Application is started.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class MainActivity extends Activity {

    private final I18n lang = I18n.getTranslation("android");

    private static ControlHandlers handlers;
    private ApplicationContext applicationContext;

    @Override
    protected void onDestroy() {
	applicationContext.shutdown();
	Editor editor = getSharedPreferences("clear_cache", Context.MODE_PRIVATE).edit();
	editor.clear();
	editor.commit();
	super.onDestroy();
	System.exit(0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(null);

	setContentView(R.layout.main);
	applicationContext = ((ApplicationContext) getApplicationContext());
	applicationContext.initialize();

	final Intent intent = getIntent();

	handleIntent(intent);
	displayText(lang.translationForKey("android.main.info"));
    }

    /**
     * Handles the intent the MainActivity was started with.
     * <br /> It's action should equal Intent.ACTION_VIEW because
     * we've been started through a link to localhost.
     * 
     * @param intent The intent the application was started with.
     * @throws TCTokenException
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    private void handleIntent(final Intent intent) {
	if (intent != null) {
	    String action = intent.getAction();
	    if (action == Intent.ACTION_VIEW) {
		final URI requestURI = URI.create(intent.getDataString());
		Thread t = new Thread(new Runnable() {

		    @Override
		    public void run() {
			Intent browserIntent = null;

			for (ControlHandler handler : MainActivity.handlers.getControlHandlers()) {
			    if (handler.getID().equals(requestURI.getPath())) {
				browserIntent = ((IntentControlHandler) handler).handle(intent);
				break;
			    }
			}

			if (browserIntent.getAction().equals(Intent.ACTION_VIEW)) {
			    startActivity(browserIntent);
			    MainActivity.this.finish();
			} else if (browserIntent.getAction().equals(ECardConstants.Minor.SAL.CANCELLATION_BY_USER)) {
			    MainActivity.this.finish();
			} else {
			    runOnUiThread(new ExtendedLengthAlertDialog(MainActivity.this));
			}

		    }
		});

		t.start();
	    }
	}
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
	MainActivity.handlers = handlers;
    }

}
