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

package org.openecard.client.android.activities;

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import org.openecard.client.android.ApplicationContext;
import org.openecard.client.android.R;
import org.openecard.client.android.RootHelper;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This Activity shows information about the currently used ecard.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardInfoActivity extends Activity implements EventCallback {

    private ApplicationContext applicationContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	// Set up the window layout
	setContentView(R.layout.cardinfo);

	RootHelper.startPCSCD(getFilesDir());
	
	// register for events
	applicationContext = ((ApplicationContext) getApplicationContext());
	applicationContext.initialize();
	applicationContext.getEnv().getEventManager().registerAllEvents(this);

	Button b = (Button) findViewById(R.id.button_back);
	b.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		finish();
	    }
	});
 
    }
    
    @Override
    protected void onDestroy() {
	applicationContext.shutdown();
	RootHelper.killPCSCD();
        super.onDestroy();  
    }
        
    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventType.equals(EventType.CARD_RECOGNIZED)) {
	    if (eventData instanceof ConnectionHandleType) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		if (ch.getRecognitionInfo().getCardType().equals("http://bsi.bund.de/cif/npa.xml")) {
		    runOnUiThread(new Runnable() {
			public void run() {
			    ImageView imageView = (ImageView) findViewById(R.id.imageView_card);
			    imageView.setImageResource(R.drawable.npa);
			    // imageView.invalidate();
			    TextView textCardType = (TextView) findViewById(R.id.text_cardType);
			    textCardType.setText("Personalausweis");
			}
		    });
		} else if (ch.getRecognitionInfo().getCardType().equals("http://ws.gematik.de/egk/1.0.0")){
		    runOnUiThread(new Runnable() {
			public void run() {
			    ImageView imageView = (ImageView) findViewById(R.id.imageView_card);
			    imageView.setImageResource(R.drawable.egk);
			    // imageView.invalidate();
			    TextView textCardType = (TextView) findViewById(R.id.text_cardType);
			    textCardType.setText("elektronische Gesundheitskarte");
			}
		    });   
		} else {
		    runOnUiThread(new Runnable() {
			public void run() {
			    ImageView imageView = (ImageView) findViewById(R.id.imageView_card);
			    imageView.setImageResource(R.drawable.unknown_card);
			    // imageView.invalidate();
			    TextView textCardType = (TextView) findViewById(R.id.text_cardType);
			    textCardType.setText("unbekannte Karte");
			}
		    });
		}
	    }
	} else if (eventType.equals(EventType.CARD_REMOVED)) {
	    runOnUiThread(new Runnable() {
		public void run() {
		    ImageView imageView = (ImageView) findViewById(R.id.imageView_card);
		    imageView.setImageResource(R.drawable.no_card);
		    // imageView.invalidate();
		    TextView textCardType = (TextView) findViewById(R.id.text_cardType);
		    textCardType.setText(R.string.text_cardTye);
		}
	    });
	}
    }
}
