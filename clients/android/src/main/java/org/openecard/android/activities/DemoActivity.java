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
import android.webkit.WebView;
import org.openecard.android.R;


/**
 * Activity for the demo tab.
 * It simply shows a button to start the demo process via an intent.
 * 
 * @author Dirk Petrautzki
 */
public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.demo);

	WebView webView = (WebView) findViewById(R.id.demoWebView);
	webView.loadUrl("file:///android_asset/demo_C.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// set the result in the hosting aboutactivity
	getParent().setResult(resultCode, data);
	finish();
    }
}
