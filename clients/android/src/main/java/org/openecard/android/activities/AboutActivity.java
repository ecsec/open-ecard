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

import android.app.ActivityGroup;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.AssetManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import iso.std.iso_iec._24727.tech.schema.Connect;
import iso.std.iso_iec._24727.tech.schema.EstablishContext;
import iso.std.iso_iec._24727.tech.schema.EstablishContextResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Locale;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.openecard.android.TCTokenService;
import org.openecard.common.I18n;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.common.interfaces.DispatcherException;
import org.openecard.scio.NFCCardTerminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple Activity used to show the About-Infos.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AboutActivity extends ActivityGroup {

    private static final Logger logger = LoggerFactory.getLogger(AboutActivity.class);
    private final I18n lang = I18n.getTranslation("about");
    private final I18n langAndroid = I18n.getTranslation("android");

    private static final String ASSET_PREFIX = "file:///android_asset/";

    private String[] tabIndexes = new String[] { "1", "2", "3", "4", "5", "6" };
    private ApplicationContext applicationContext;
    private AssetManager assetManager;
    private HashSet<String> assets = new HashSet<String>();
    private boolean usingNFC;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// Set up the window layout
	setContentView(R.layout.about);

	applicationContext = (ApplicationContext) getApplicationContext();
	applicationContext.initialize();
	usingNFC = applicationContext.usingNFC();

	// fill asset set
	assetManager = getAssets();
	try {
	    for (String asset : assetManager.list("")) {
		assets.add(asset);
	    }
	} catch (IOException e) {
	    logger.error("Coudn't get list of assets.", e);
	}

	Button b = (Button) findViewById(R.id.button_back);
	b.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		onDestroy();
		finish();
		System.exit(0);
	    }
	});

	b.setText(lang.translationForKey("about.button.close"));

	Intent i = new Intent(this, TCTokenService.class);
	this.startService(i);

	TabHost tabs = (TabHost) this.findViewById(R.id.my_tabhost);
	tabs.setup(this.getLocalActivityManager());

	TabContentFactory tabContentFactory = new TabContentFactory() {

	    @Override
	    public View createTabContent(String tag) {
		LinearLayout ll = new LinearLayout(AboutActivity.this);
		LinearLayout.LayoutParams fillLayoutParams = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
			LayoutParams.FILL_PARENT);
		ll.setLayoutParams(fillLayoutParams);
		WebView webView = new WebView(AboutActivity.this);
		webView.setLayoutParams(fillLayoutParams);
		try {
		    if (tag.equals(tabIndexes[0])) {
			webView.loadUrl(ASSET_PREFIX + getTranslatedAsset("about", "html"));
		    } else if (tag.equals(tabIndexes[2])) {
			webView.loadUrl(ASSET_PREFIX + getTranslatedAsset("demo", "html"));
		    } else if (tag.equals(tabIndexes[3])) {
			webView.loadUrl(ASSET_PREFIX + getTranslatedAsset("join", "html"));
		    } else if (tag.equals(tabIndexes[5])) {
			webView.loadUrl(ASSET_PREFIX + getTranslatedAsset("gpl-v3", "html"));
		    }
		} catch (IOException e) {
		    logger.error(e.getMessage(), e);
		}
		ll.addView(webView);
		return ll;
	    }
	};

	TabSpec tspec1 = tabs.newTabSpec(tabIndexes[0]);
	tspec1.setIndicator(lang.translationForKey("about.tab.about"));
	tspec1.setContent(tabContentFactory);
	tabs.addTab(tspec1);
	TabSpec tspec2 = tabs.newTabSpec(tabIndexes[1]);
	tspec2.setIndicator(langAndroid.translationForKey("about.tab.card"));
	Intent cardIntent = new Intent(this, CardInfoActivity.class);
	tspec2.setContent(cardIntent);
	tabs.addTab(tspec2);
	TabSpec tspec3 = tabs.newTabSpec(tabIndexes[2]);
	tspec3.setIndicator(langAndroid.translationForKey("about.tab.demo"));
	tspec3.setContent(tabContentFactory);
	tabs.addTab(tspec3);
	TabSpec tspec4 = tabs.newTabSpec(tabIndexes[3]);
	tspec4.setIndicator(langAndroid.translationForKey("about.tab.join"));
	tspec4.setContent(tabContentFactory);
	tabs.addTab(tspec4);
	TabSpec tspec5 = tabs.newTabSpec(tabIndexes[4]);
	tspec5.setIndicator(langAndroid.translationForKey("android.settings"));
	Intent settingsIntent = new Intent(this, SettingsActivity.class);
	tspec5.setContent(settingsIntent);
	tabs.addTab(tspec5);
	TabSpec tspec6 = tabs.newTabSpec(tabIndexes[5]);
	tspec6.setIndicator(lang.translationForKey("about.tab.license"));
	tspec6.setContent(tabContentFactory);
	tabs.addTab(tspec6);
    }

    @Override
    public void onNewIntent(Intent intent) {
	if (usingNFC) {
	    Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	    IsoDep tag = IsoDep.get(tagFromIntent);
	    NFCCardTerminal.getInstance().setTag(tag);
	    try {
		EstablishContext establishContext = new EstablishContext();
		ApplicationContext applicationContext = (ApplicationContext) this.getApplicationContext();
		Dispatcher d = applicationContext.getEnv().getDispatcher();
		EstablishContextResponse response = (EstablishContextResponse) d.deliver(establishContext);
		Connect c = new Connect();
		c.setContextHandle(response.getContextHandle());
		c.setIFDName("Integrated NFC");
		c.setSlot(new BigInteger("0"));
		d.deliver(c);
	    } catch (DispatcherException e) {
		logger.error("Failure in the dispatcher.", e);
	    } catch (InvocationTargetException e) {
		logger.error("The dispatched method threw an exception", e);
	    }
	}
    }

    @Override
    public synchronized void onResume() {
	super.onResume();

	if (usingNFC) {
	    Intent activityIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
	    NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, null, null);
	}
    }

    @Override
    public synchronized void onPause() {
	super.onPause();
	if (usingNFC) {
	    NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
	}
    }

    @Override
    protected void onDestroy() {
	logger.debug("onDestroy");
	applicationContext.shutdown();
	super.onDestroy();
    }

    private String getTranslatedAsset(String name, String fileEnding) throws IOException {
	fileEnding = fileEnding != null ? ("." + fileEnding) : "";
	Locale locale = Locale.getDefault();
	String lang = locale.getLanguage();
	String country = locale.getCountry();
	String fnameBase = name;
	// try to guess correct file to load
	if (!lang.isEmpty() && !country.isEmpty()) {
	    String fileName = fnameBase + "_" + lang + "_" + country + fileEnding;
	    if (assets.contains(fileName)) {
		return fileName;
	    }
	}
	if (!lang.isEmpty()) {
	    String fileName = fnameBase + "_" + lang + fileEnding;
	    if (assets.contains(fileName)) {
		return fileName;
	    }
	}
	// else
	String fileName = fnameBase + "_C" + fileEnding;
	if (assets.contains(fileName)) {
	    return fileName;
	}

	// no file found
	throw new IOException("No translation available for file '" + name + fileEnding + "'.");
    }

}
