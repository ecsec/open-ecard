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
import android.app.PendingIntent;
import android.content.Intent;
import android.net.http.SslError;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.ConnectResponse;
import java.math.BigInteger;
import org.openecard.client.android.ApplicationContext;
import org.openecard.client.android.ObjectTagParser;
import org.openecard.client.android.R;
import org.openecard.client.scio.NFCCardTerminal;


/**
 * This is the main Activity. It the first activity to open when the Application
 * is started.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class MainActivity extends Activity {

    private ApplicationContext appState;

    @Override
    public void onCreate(Bundle savedInstanceState) {

	super.onCreate(savedInstanceState);

	// Set up the window layout and the cusom title
	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	setContentView(R.layout.main);
	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
	TextView mTitle = (TextView) findViewById(R.id.title_left_text);
	mTitle.setText(R.string.app_name);
	// mTitle = (TextView) findViewById(R.id.title_right_text);

	// Set up the webview
	WebView mWebView = (WebView) findViewById(R.id.webview);
	mWebView.getSettings().setJavaScriptEnabled(true);
	appState = ((ApplicationContext) getApplicationContext());
	mWebView.addJavascriptInterface(new ObjectTagParser(appState.getEnv(), mWebView), "HTMLOUT");

	this.appState.setWebView(mWebView);

	/* Testserver */
	//mWebView.loadUrl("https://test.governikus-eid.de/Autent-DemoApplication/"); // funktioniert
	// mWebView.loadUrl("http://willow.mtg.de/eidavs/static/bigbunny.html");
	// //funktioniert
	 mWebView.loadUrl("https://eid.services.ageto.net/gw"); //funktioniert
	/* Produktivserver */
	// mWebView.loadUrl("https://www.bos-bremen.de/login/");
	// mWebView.loadUrl("https://eid.vx4.net/webapp/test.jsp");
	// mWebView.loadUrl("http://www.mein-cockpit.de");

	mWebView.setWebViewClient(new WebViewClient() {

	    @Override
	    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
		// proceed on ssl error, since the webview doesn't show a dialog
		// for accepting or refusing a certificate
		handler.proceed();
	    }

	    @Override
	    public void onPageFinished(final WebView view, String url) {
		super.onPageFinished(view, url);

		runOnUiThread(new Runnable() {
		    @Override
		    public void run() {

			view.loadUrl("javascript:window.HTMLOUT.showHTML(document.getElementsByTagName('object')[0].innerHTML);");
		    }
		});

	    }

	});

	// If the adapter is null, then NFC is not supported
	if (NfcAdapter.getDefaultAdapter(this) == null) {
	    Toast.makeText(this, R.string.error_no_nfc, Toast.LENGTH_LONG).show();
	    finish();
	    return;
	}
    }

    public void onNewIntent(Intent intent) {

	System.out.println("tag discovered");
	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	IsoDep tag = IsoDep.get(tagFromIntent);
	NFCCardTerminal.getInstance().setTag(tag);

	Connect c = new Connect();
	c.setContextHandle(appState.getCTX());
	c.setIFDName("Integrated NFC");
	c.setSlot(new BigInteger("0"));
	ConnectResponse cr = appState.getEnv().getIFD().connect(c);

    }

    @Override
    public synchronized void onResume() {
	super.onResume();
	PendingIntent intent = PendingIntent
		.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, intent, null, null);
    }

    @Override
    public synchronized void onPause() {
	super.onPause();
	NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	MenuInflater inflater = getMenuInflater();
	inflater.inflate(R.menu.option_menu, menu);
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	Intent i;
	switch (item.getItemId()) {
	case R.id.about:
	    i = new Intent(this, AboutActivity.class);
	    startActivity(i);
	    return true;
	case R.id.cardinfo:
	    i = new Intent(this, CardInfoActivity.class);
	    startActivity(i);
	    return true;
	case R.id.pinmanagement:
	    i = new Intent(this, PINManagementActivity.class);
	    startActivity(i);
	    return true;
	}

	return false;
    }

}
