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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import org.openecard.android.R;


/**
 * Activity for the demo tab.
 * It simply shows a button to start the demo process via an intent.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class DemoActivity extends Activity {

    private final Uri demoUri = Uri.parse("http://localhost:24727/eID-Client?tcTokenURL=https%3A%2F%2Feservice.openecard.org%2FtcToken%3Fcard-type%3Dhttp%253A%252F%252Fbsi.bund.de%252Fcif%252Fnpa.xml%26with-html%3D");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	setContentView(R.layout.demo);

	ImageView imageView = (ImageView) findViewById(R.id.imageViewDemo);
	imageView.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		Intent i = new Intent(DemoActivity.this, IntentHandlerActivity.class);
		i.setData(demoUri);
		startActivity(i);
	    }
	});
    }

}
