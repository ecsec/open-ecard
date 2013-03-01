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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import org.openecard.android.R;
import org.openecard.common.I18n;


/**
 * Simple Activity to show an Error when NFC is missing or deactivated. We need to implement this as activity because a
 * normal dialog would be closed when the initialize method of ApplicationContext returns.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class NFCErrorActivity extends Activity {

    private final I18n lang = I18n.getTranslation("android");

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// Set up the window layout
	setContentView(R.layout.error);

	Button b = (Button) findViewById(R.id.button);
	b.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		finish();
	    }
	}); 

	TextView description = (TextView) findViewById(R.id.description); 
	description.setText(lang.translationForKey("android.error.nfc_error"));
    }

}
