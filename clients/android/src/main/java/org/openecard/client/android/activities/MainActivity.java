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

import iso.std.iso_iec._24727.tech.schema.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import org.openecard.client.android.ApplicationContext;
import org.openecard.client.android.ObjectTagParser;
import org.openecard.client.android.R;
import org.openecard.client.android.TCTokenService;
import org.openecard.client.common.WSHelper;
import org.openecard.client.common.enums.EventType;
import org.openecard.client.common.interfaces.Environment;
import org.openecard.client.common.interfaces.EventCallback;
import org.openecard.client.connector.common.ConnectorConstants;
import org.openecard.client.connector.messages.TCTokenRequest;
import org.openecard.client.connector.tctoken.*;
import org.openecard.client.sal.TinySAL;
import org.openecard.client.transport.paos.PAOS;
import org.openecard.client.transport.tls.PSKTlsClientImpl;
import org.openecard.client.transport.tls.TlsClientSocketFactory;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.net.http.SslError;
import android.opengl.Visibility;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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
		String serverAddress = tcTokenRequest.getTCToken().getServerAddress().toExternalForm();
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
			URL url = new URL(serverAddress);
			PSKTlsClientImpl tlsClient = new PSKTlsClientImpl(sessionIdentifier.getBytes(), psk, url.getHost());
			tlsClient.removeClientExtension(0);
			TlsClientSocketFactory tlspskSocketFactory = new TlsClientSocketFactory(tlsClient);

			PAOS p = new PAOS(serverAddress + "?sessionid=" + sessionIdentifier, env.getDispatcher(), tlspskSocketFactory);
			StartPAOS sp = new StartPAOS();
			sp.getConnectionHandle().add(connectionHandle);
			sp.setSessionIdentifier(sessionIdentifier);
			p.sendStartPAOS(sp);
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(tcTokenRequest.getTCToken().getRefreshAddress()
				.toString()));
			startActivity(browserIntent);
			runOnUiThread(new Runnable() {

			    @Override
			    public void run() {
				TextView tv = (TextView) findViewById(R.id.textViewMain);
				tv.setText("");
			    }
			});

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
	applicationContext.shutdown();
	killPCSCD();
	// closing Entire Application

	Editor editor = getSharedPreferences("clear_cache", Context.MODE_PRIVATE).edit();
	editor.clear();
	editor.commit();
	trimCache(this);
	super.onDestroy();
	//android.os.Process.killProcess(android.os.Process.myPid());

    }

    public static void trimCache(Context context) {
	    try {
	        File dir = context.getCacheDir();
	        if (dir != null && dir.isDirectory()) {
	            deleteDir(dir);

	        }
	    } catch (Exception e) {
	        // TODO: handle exception
	    }
	}


	public static boolean deleteDir(File dir) {
	    if (dir != null && dir.isDirectory()) {
	        String[] children = dir.list();
	        for (int i = 0; i < children.length; i++) {
	            boolean success = deleteDir(new File(dir, children[i]));
	            if (!success) {
	                return false;
	            }
	        }
	    }

	    // <uses-permission
	    // android:name="android.permission.CLEAR_APP_CACHE"></uses-permission>
	    // The directory is now empty so delete it

	    return dir.delete();
	}

	
    private void killPCSCD() {
	File f = new File("/data/pcscd/pcscd.pid");
	if (f.exists()) { 
	    try {
		FileInputStream fis = new FileInputStream(f);
		byte[] pid = new byte[fis.available()];
		fis.read(pid);
		Process p = Runtime.getRuntime().exec("su");
		OutputStream os = p.getOutputStream();
		writeCommand(os, "kill -9 " + new String(pid));
	    } catch (Exception e) {
		e.printStackTrace();
		//TODO
	    } 
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

	super.onCreate(null);

	setContentView(R.layout.main);
	
	Intent i = new Intent(this, TCTokenService.class);
	this.startService(i);
	
	startPCSCD();
	
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

    private void startPCSCD(){
	killPCSCD();
	String pcscd_exec=getFilesDir().getParent() + "/lib/libpcscd.so";
	System.out.println(pcscd_exec);
	String files_dir = getFilesDir().getAbsolutePath();
	System.out.println(files_dir);
	
	String permission_string="logwrapper chmod 777 " + pcscd_exec;
	System.out.println(permission_string);
	String server_string= "su -c \"logwrapper " + pcscd_exec + " -f -d \"";
	System.out.println(server_string);
	
	
	try {
	    	    

	    //
	    //sh = Runtime.getRuntime().exec("su -c '" + server_string + "'",null,new File(files_dir)); 
		Process sh = Runtime.getRuntime().exec("su",null,new File(files_dir));
	    OutputStream os = sh.getOutputStream();
		writeCommand(os, permission_string);
		writeCommand(os, server_string);
	} catch (IOException e1) {
	    // TODO Auto-generated catch block
	    e1.printStackTrace();
	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
    
    static void writeCommand(OutputStream os, String command) throws Exception {
	os.write((command + "\n").getBytes("ASCII"));
    }

    /**
     * Handles the intent the application was startet with.</br> If it's action
     * equals Intent.ACTION_VIEW we've been startet through a link to localhost.
     * </br> If It's action equals Intent.MAIN we've been explicitely startet
     * through the user
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
	    System.out.println("action: " + action);
	    if (action == Intent.ACTION_VIEW) {
		this.uri = intent.getData();
	    } else {
		WebView webView = new WebView(this);

		webView.getSettings().setJavaScriptEnabled(true);
		webView.addJavascriptInterface(new ObjectTagParser(applicationContext.getEnv(), webView), "HTMLOUT");

		this.applicationContext.setWebView(webView);

		/* Testserver */
		// webView.loadUrl("https://test.governikus-eid.de/Autent-DemoApplication/");
		// // funktioniert
		// mWebView.loadUrl("http://willow.mtg.de/eidavs/static/bigbunny.html");
		// //funktioniert
		// webView.loadUrl("https://eid.services.ageto.net/gw"); //
		// funktioniert

		webView.loadUrl("https://willow.mtg.de/eid-server-demo-app/index.html");
		/* Produktivserver */
		// mWebView.loadUrl("https://www.bos-bremen.de/login/");
		// mWebView.loadUrl("https://eid.vx4.net/webapp/test.jsp");
		// mWebView.loadUrl("http://www.mein-cockpit.de");

		webView.setWebViewClient(new WebViewClient() {

		    @Override
		    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			// proceed on ssl error, since the webview doesn't show
			// a dialog
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

		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutMainActivity);
		linearLayout.addView(webView);
	    }

	} else {
	    // should never happen
	    return;
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
		TCToken token = parseTCToken(new URL(value));
		tcTokenRequest.setTCToken(token);
	    } else {
		throw new IllegalArgumentException("Malformed tcTokenURL");
	    }

	}
	return tcTokenRequest;
    }

    /**
     * Parses the TCToken.
     * 
     * @throws TCTokenException
     */
    private TCToken parseTCToken(URL tokenURI) throws TCTokenException {
	// Get TCToken from the given url
	TCTokenGrabber grabber = new TCTokenGrabber();
	String data = grabber.getResource(tokenURI.toString());
	data = data.substring(data.indexOf("<TCTokenType"), data.indexOf("</TCTokenType>") + 14);

	// Parse the TCToken
	TCTokenParser parser = new TCTokenParser();

	List<TCToken> tokens = parser.parse(data);

	if (tokens.isEmpty()) {
	    throw new TCTokenException(ConnectorConstants.ConnectorError.TC_TOKEN_NOT_AVAILABLE.toString());
	}

	// Verify the TCToken
	TCTokenVerifier ver = new TCTokenVerifier(tokens);
	ver.verify();

	return tokens.get(0);
    }

    /**
     * Overridden to inflate our own option menu.
     */
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
