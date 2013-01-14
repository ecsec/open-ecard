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
import org.openecard.android.R;
import org.openecard.android.TCTokenService;
import org.openecard.common.I18n;


/**
 * Simple Activity used to show the About-Infos.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AboutActivity extends Activity {

    private String[] tags = new String[] { "1", "2", "3" };
    private final I18n lang = I18n.getTranslation("about");

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	Intent i = new Intent(this, TCTokenService.class);
	this.startService(i);

	// Set up the window layout
	setContentView(R.layout.about);

	Button b = (Button) findViewById(R.id.button_back);
	b.setOnClickListener(new OnClickListener() {
	    public void onClick(View v) {
		finish();
	    }
	});

	TabHost tabs = (TabHost) this.findViewById(R.id.my_tabhost);
	tabs.setup();

	TabContentFactory tabContentFactory = new TabContentFactory() {

	    @Override
	    public View createTabContent(String tag) {
		LinearLayout ll = new LinearLayout(AboutActivity.this);
		ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		WebView webView = new WebView(AboutActivity.this);
		if (tag.equals(tags[0])) {
		    webView.loadUrl("file:///android_asset/about_de.html");
		} else if (tag.equals(tags[1])) {
		    webView.loadUrl("file:///android_asset/feedback_de.html");
		} else {
		    webView.loadUrl("file:///android_asset/join_de.html");
		}
		webView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		ll.addView(webView);
		return ll;
	    }
	};

	TabSpec tspec1 = tabs.newTabSpec(tags[0]);
	tspec1.setIndicator(lang.translationForKey("about.tab.about"));
	tspec1.setContent(tabContentFactory);
	tabs.addTab(tspec1);
	TabSpec tspec2 = tabs.newTabSpec(tags[1]);
	tspec2.setIndicator(lang.translationForKey("about.tab.feedback"));
	tspec2.setContent(tabContentFactory);
	tabs.addTab(tspec2);
	TabSpec tspec3 = tabs.newTabSpec(tags[2]);
	tspec3.setIndicator(lang.translationForKey("about.tab.join"));
	tspec3.setContent(tabContentFactory);
	tabs.addTab(tspec3);
	// pad as much as the tabs are high
	int topPadding = tabs.getTabWidget().getChildAt(0).getLayoutParams().height;
	tabs.getTabContentView().setPadding(0, topPadding, 0, 0);
    }


}
