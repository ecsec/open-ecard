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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import org.openecard.android.R;
import org.openecard.common.I18n;


/**
 * This Activity shows a list of available things that can be configured, for example plugins.
 * By selecting an item on the list the corresponding Activity for configuration will be started.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class SettingsActivity extends Activity {

    private final I18n lang = I18n.getTranslation("android");

    public static final String PLUGIN_INDEX = "PLUGIN_INDEX";

    private ArrayAdapter<String> listAdapter;
    private ArrayList<Intent> intents = new ArrayList<Intent>(1);
    private ArrayList<String> settings = new ArrayList<String>(1);

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.settings);
	fillLists();
	setUpListViewSettings();
    }

    /**
     * Fills the lists containing the name of the settings and the intents.
     */
    private void fillLists() {
	settings.add("Plugins");
	intents.add(new Intent(SettingsActivity.this, PluginsActivity.class));
	settings.add(lang.translationForKey("android.settings.comm_interface"));
	intents.add(new Intent(SettingsActivity.this, TerminalFactoryActivity.class));
    }

    /**
     * Fills the ListView with the available settings.
     */
    private void setUpListViewSettings() {
	ListView listViewSettings = (ListView) findViewById(R.id.listViewSettings);

	listAdapter = new ArrayAdapter<String>(this, R.layout.setting_row, settings);
	listViewSettings.setAdapter(listAdapter);

	// start the activity to display details of the selected setting
	listViewSettings.setOnItemClickListener(new OnItemClickListener() {

	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent i = intents.get(position);
		startActivity(i);
	    }
	});
    }

}
