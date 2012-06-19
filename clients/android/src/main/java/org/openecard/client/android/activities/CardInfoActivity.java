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

import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.math.BigInteger;
import org.openecard.client.android.ApplicationContext;
import org.openecard.client.android.ClientEventCallBack;
import org.openecard.client.android.R;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.scio.NFCCardTerminal;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * This Activity shows information about the currently used ecard.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class CardInfoActivity extends Activity implements EventCallback {

    private ApplicationContext appState;

    @Override
    public void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	// Set up the window layout and the cusom title
	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	setContentView(R.layout.cardinfo);
	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
	TextView mTitle = (TextView) findViewById(R.id.title_left_text);
	mTitle.setText(R.string.app_name);
	// mTitle = (TextView) findViewById(R.id.title_right_text);

	// register for events
	appState = ((ApplicationContext) getApplicationContext());
	appState.getEnv().getEventManager().registerAllEvents(this);

	Button b = (Button) findViewById(R.id.button_back);
	b.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		finish();
	    }
	});

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

    @Override
    public synchronized void onResume() {
	super.onResume();
	PendingIntent intent = PendingIntent
		.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, intent, null, null);

	this.signalEvent(ClientEventCallBack.eventType, ClientEventCallBack.eventData);
    }

    @Override
    public synchronized void onPause() {
	super.onPause();
	NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }

    public void onNewIntent(Intent intent) {
	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	IsoDep tag = IsoDep.get(tagFromIntent);
	NFCCardTerminal.getInstance().setTag(tag);

	Connect c = new Connect();
	c.setContextHandle(appState.getCTX());
	c.setIFDName("Integrated NFC");
	c.setSlot(new BigInteger("0"));
	appState.getEnv().getIFD().connect(c);
    }

}
