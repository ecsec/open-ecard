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
		//mTitle = (TextView) findViewById(R.id.title_right_text);
		
		// Set up the webview 
		WebView mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setJavaScriptEnabled(true);
		appState = ((ApplicationContext) getApplicationContext());
		mWebView.addJavascriptInterface(new ObjectTagParser(appState.getEnv()), "HTMLOUT");
		mWebView.loadUrl("https://www.cosmos-direkt.de/");
		
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				// proceed on ssl error, since the webview doesn't show a dialog
				// for accepting or refusing a certificate
				handler.proceed();
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				view.loadUrl("javascript:window.HTMLOUT.showHTML(document.getElementsByTagName('object')[0].innerHTML);");
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
		appState.getEnv().getIFD().connect(c);
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
		switch (item.getItemId()) {
		case R.id.about:
			// TODO start activity for 'about'-stuff
			return true;
		}
		return false;
	}

}