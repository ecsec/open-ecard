/* Copyright 2012, Hochschule fuer angewandte Wissenschaften Coburg 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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