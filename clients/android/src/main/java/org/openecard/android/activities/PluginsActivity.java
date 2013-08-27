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
import java.util.List;
import java.util.Set;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.android.AddonManagerSingleton;
import org.openecard.android.R;


/**
 * This Activity shows a list of available plugins.
 * By selecting an item on the list the corresponding Activity for the plugin will be started.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PluginsActivity extends Activity {

    public static final String PLUGIN_INDEX = "PLUGIN_INDEX";
    private static final String LANGUAGE_CODE = System.getProperty("user.language");
    private ArrayAdapter<String> listAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.plugins);
	setUpListViewPlugins();
    }

    /**
     * Fills the ListView with the available plugins.
     */
    private void setUpListViewPlugins() {
	ListView listViewPlugins = (ListView) findViewById(R.id.listViewPlugins);
	Set<AddonSpecification> listPlugins = AddonManagerSingleton.getInstance().getRegistry().listAddons();
	List<String> pluginNames = new ArrayList<String>();

	for (AddonSpecification addon : listPlugins) {
	    pluginNames.add(addon.getLocalizedName(LANGUAGE_CODE));
	}

	listAdapter = new ArrayAdapter<String>(this, R.layout.plugin_row, pluginNames);
	listViewPlugins.setAdapter(listAdapter);

	// start the activity to display details of the selected plugin
	listViewPlugins.setOnItemClickListener(new OnItemClickListener() {

	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent i = new Intent(PluginsActivity.this, PluginActivity.class);
		i.putExtra(PLUGIN_INDEX, position);
		startActivity(i);
	    }
	});
    }

}
