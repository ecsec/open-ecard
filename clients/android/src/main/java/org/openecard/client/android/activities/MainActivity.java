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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.ProgressBar;
import android.widget.TextView;
import generated.TCTokenType;
import iso.std.iso_iec._24727.tech.schema.*;
import java.io.*;
import java.net.*;
import java.util.List;
import org.openecard.client.android.*;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.control.module.tctoken.TCTokenException;
import org.openecard.client.control.module.tctoken.TCTokenFactory;
import org.openecard.client.control.module.tctoken.TCTokenRequest;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;

/**
 * This is the main Activity. It is the first activity to open when the
 * Application is started.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class MainActivity extends Activity implements EventCallback {

    private ApplicationContext applicationContext;
    Uri uri;

    class MyRunnable implements Runnable {

	private Uri uri;

	public MyRunnable(Uri uri) {
	    this.uri = uri;
	}

	@Override
	public void run() {
	    try {
		TCTokenRequest tcTokenRequest = handleActionIntent(uri);
		String serverAddress = tcTokenRequest.getTCToken().getServerAddress();
		byte[] psk = tcTokenRequest.getTCToken().getPathSecurityParameters().getPSK();
		String sessionIdentifier = tcTokenRequest.getTCToken().getSessionIdentifier();
		Environment env = applicationContext.getEnv();
		try {
		    TinySAL sal = (TinySAL) env.getSAL();
		    List<ConnectionHandleType> cHandles = sal.getConnectionHandles();
		    if (cHandles.size() == 0)
			return;

		    runOnUiThread(new Runnable() {

			@Override
			public void run() {
			    TextView tv = (TextView) findViewById(R.id.textViewMain);
			    tv.setText("Verbindung zum Server wird aufgebaut und Identitätsnachweis wird durchgeführt.\n\nSie werden anschließend auf die Seite des Diensteanbieters weitergeleitet.");
			}
		    });

		    ConnectionHandleType connectionHandle = cHandles.get(0);

		    // Perform a CardApplicationPath and CardApplicationConnect
		    // to connect to the card application
		    CardApplicationPath cardApplicationPath = new CardApplicationPath();
		    cardApplicationPath.setCardAppPathRequest(new CardApplicationPathType());
		    CardApplicationPathResponse cardApplicationPathResponse = sal.cardApplicationPath(cardApplicationPath);

		    try {
			// Check CardApplicationPathResponse
			WSHelper.checkResult(cardApplicationPathResponse);
		    } catch (WSHelper.WSException ex) {
			ex.printStackTrace();
		    }

		    CardApplicationConnect cardApplicationConnect = new CardApplicationConnect();
		    cardApplicationConnect.setCardApplicationPath(cardApplicationPathResponse.getCardAppPathResultSet()
			    .getCardApplicationPathResult().get(0));
		    CardApplicationConnectResponse cardApplicationConnectResponse = sal.cardApplicationConnect(cardApplicationConnect);

		    try {
			// Check CardApplicationPathResponse
			WSHelper.checkResult(cardApplicationConnectResponse);
		    } catch (WSHelper.WSException ex) {
			ex.printStackTrace();
		    }

		    // Update ConnectionHandle. It now includes a SlotHandle.
		    connectionHandle = cardApplicationConnectResponse.getConnectionHandle();

		    if (cHandles.size() > 0) {
			URL url = new URL(serverAddress + "?sessionid=" + sessionIdentifier);
			PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionIdentifier.getBytes(), psk, url.getHost());
			tlsClient.removeClientExtension(0);
			TlsClientSocketFactory tlspskSocketFactory = new TlsClientSocketFactory(tlsClient);

			PAOS p = new PAOS(url, env.getDispatcher(), tlspskSocketFactory);
			StartPAOS sp = new StartPAOS();
			sp.getConnectionHandle().add(connectionHandle);
			sp.setSessionIdentifier(sessionIdentifier);
			p.sendStartPAOS(sp);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tcTokenRequest.getTCToken().getRefreshAddress()
				.toString()));
			startActivity(browserIntent);
			Intent i = new Intent(MainActivity.this, AboutActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			onDestroy();
		    } else {

		    }
		} catch (Exception e) {
		    e.printStackTrace();
		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}

    }

    @Override
    protected void onDestroy() {
	RootHelper.killPCSCD();
	Editor editor = getSharedPreferences("clear_cache", Context.MODE_PRIVATE).edit();
	editor.clear();
	editor.commit();
	trimCache(this);
	System.exit(0);
    }

    public static void trimCache(Context context) {
	try {
	    File dir = context.getCacheDir();
	    if (dir != null && dir.isDirectory()) {
		ApplicationContext.deleteDir(dir);
	    }
	} catch (Exception e) {
	    // TODO: handle exception
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

	super.onCreate(null);

	setContentView(R.layout.main);
	RootHelper.startPCSCD(getFilesDir());

	applicationContext = ((ApplicationContext) getApplicationContext());
	applicationContext.initialize();
	applicationContext.getEnv().getEventManager().registerAllEvents(this);
	final Intent intent = getIntent();
	try {
	    handleIntent(intent);
	} catch (Exception e) {
	    // TODO
	}
    }

    /**
     * Handles the intent the MainActivity was started with.</br> It's action
     * should equal Intent.ACTION_VIEW because we've been started through a link
     * to localhost.
     * 
     * @param intent
     *            The intent the application was started with.
     * @throws TCTokenException
     * @throws MalformedURLException
     * @throws UnsupportedEncodingException
     */
    private void handleIntent(Intent intent) throws UnsupportedEncodingException, MalformedURLException, TCTokenException {
	if (intent != null) {
	    String action = intent.getAction();
	    if (action == Intent.ACTION_VIEW) {
		this.uri = intent.getData();
	    }
	}
    }

    private TCTokenRequest handleActionIntent(Uri data) throws UnsupportedEncodingException, MalformedURLException, TCTokenException {
	TCTokenRequest tcTokenRequest = new TCTokenRequest();

	String q = data.toString();
	q = q.substring(q.indexOf("tcTokenURL"), q.length());
	String name = q.substring(0, q.indexOf("="));
	String value = q.substring(q.indexOf("=") + 1, q.length());

	if (name.startsWith("tcTokenURL")) {
	    if (!value.isEmpty()) {
		value = URLDecoder.decode(value, "UTF-8");
		TCTokenType token = TCTokenFactory.generateTCToken(new URL(value));
		tcTokenRequest.setTCToken(token);
	    } else {
		throw new IllegalArgumentException("Malformed tcTokenURL");
	    }

	}
	return tcTokenRequest;
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

    private void showProgressBar() {
	runOnUiThread(new Runnable() {

	    @Override
	    public void run() {
		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.VISIBLE);
	    }
	});
    }

    private void hideProgressBar() {
	runOnUiThread(new Runnable() {

	    @Override
	    public void run() {
		ProgressBar pb = (ProgressBar) findViewById(R.id.progressBar1);
		pb.setVisibility(View.INVISIBLE);
	    }
	});
    }
    
    @Override
    public void signalEvent(EventType eventType, Object eventData) {
	if (eventType.equals(EventType.CARD_INSERTED)) {
	    displayText("Kartenerkennung wird durchgeführt.");
	    showProgressBar();
	} else if (eventType.equals(EventType.CARD_RECOGNIZED)) {
	    if (this.uri == null)
		return;
	    if (eventData instanceof ConnectionHandleType) {
		ConnectionHandleType ch = (ConnectionHandleType) eventData;
		if ("http://bsi.bund.de/cif/npa.xml".equals(ch.getRecognitionInfo().getCardType())) {
		    Thread t = new Thread(new MyRunnable(this.uri));
		    t.start();
		} else {
		    displayText("Es wurde eine andere Karte als der Personalausweis erkannt.\n\nMomentan unterstützt diese App nur den Personalausweis.");
		    hideProgressBar();
		}
	    }

	}
    }
}
