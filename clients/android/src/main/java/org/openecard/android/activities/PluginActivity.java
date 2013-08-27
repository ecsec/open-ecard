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
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.android.AddonManagerSingleton;
import org.openecard.android.R;
import org.openecard.common.I18n;


/**
 * This Activity shows the information for a plugin.
 * Furthermore PluginActions can be started and settings can be configured.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class PluginActivity extends Activity {

    private final I18n lang = I18n.getTranslation("android");

    // gui constants
    private static final String SETTINGS_NOSETTINGS = "settings.plugins.settings.nosettings";
    private static final String SETTINGS = "settings.plugins.settings";
    private static final String ACTIONS = "settings.plugins.actions";
    private static final String DESCRIPTION = "settings.plugins.description";
    private static final String DESC = "desc";
    private static final String NAME = "name";

    private static final String[] tabIndexes = new String[] { "1", "2", "3" };
    private static final String LANGUAGE_CODE = System.getProperty("user.language");

    private AddonSpecification plugin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.plugin);

	Set<AddonSpecification> listPlugins = AddonManagerSingleton.getInstance().getRegistry().listAddons();
	List<String> pluginNames = new ArrayList<String>();

	for (AddonSpecification addon : listPlugins) {
	    pluginNames.add(addon.getLocalizedName(LANGUAGE_CODE));
	}
	int index = (Integer) getIntent().getExtras().get(PluginsActivity.PLUGIN_INDEX);
	plugin = AddonManagerSingleton.getInstance().getRegistry().searchByName(pluginNames.get(index)).iterator().next();

	setUpTabHost();
    }

    public AddonSpecification getAddon() {
	return plugin;
    }

    /**
     * Sets up each tab of the TabHost with the corresponding contents.
     */
    private void setUpTabHost() {
	TabHost tabs = (TabHost) this.findViewById(R.id.my_tabhost);
	tabs.setup();

	TabContentFactory tabContentFactory = new TabContentFactory() {

	    @Override
	    public View createTabContent(String tag) {
		LinearLayout ll = new LinearLayout(PluginActivity.this);
		ll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		View v;
		if (tag.equals(tabIndexes[0])) {
		    v = createDescriptionView();
		} else if (tag.equals(tabIndexes[1])) {
		    v = createActionsView();
		} else {
		    v = createSettingsView();
		}
		ll.addView(v);
		return ll;
	    }
	};

	TabSpec tspec1 = tabs.newTabSpec(tabIndexes[0]);
	tspec1.setIndicator(lang.translationForKey(DESCRIPTION));
	tspec1.setContent(tabContentFactory);
	tabs.addTab(tspec1);
	TabSpec tspec2 = tabs.newTabSpec(tabIndexes[1]);
	tspec2.setIndicator(lang.translationForKey(ACTIONS));
	tspec2.setContent(tabContentFactory);
	tabs.addTab(tspec2);
	TabSpec tspec3 = tabs.newTabSpec(tabIndexes[2]);
	tspec3.setIndicator(lang.translationForKey(SETTINGS));
	tspec3.setContent(tabContentFactory);
	tabs.addTab(tspec3);
    }

    /**
     * Creates the View containing the Plugin's settings.
     * 
     * @return The View containing the Plugin's settings.
     */
    private View createSettingsView() {
	TextView txtNoSettings = new TextView(PluginActivity.this);
	txtNoSettings.setText(lang.translationForKey(SETTINGS_NOSETTINGS));
	return txtNoSettings;
    }

    /**
     * Creates the View containing the Plugin's actions.
     * 
     * @return The View containing the Plugin's actions.
     */
    private ScrollView createActionsView() {
	LayoutParams fillParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	int padding = this.getResources().getDimensionPixelSize(R.dimen.padding);

	ScrollView sv = new ScrollView(PluginActivity.this);
	sv.setLayoutParams(fillParams);
	sv.setPadding(padding, padding, padding, padding);

	ExpandableListView elv = new ExpandableListView(PluginActivity.this) {
	    // workaround to get a ExpandableListView displayed in a ScrollView
	    @Override
	    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Calculate entire height by providing a very large height hint.
		// But do not use the highest 2 bits of this integer; those are
		// reserved for the MeasureSpec mode.
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

		android.view.ViewGroup.LayoutParams params = getLayoutParams();
		params.height = getMeasuredHeight();
	    }
	};

	elv.setLayoutParams(fillParams);
	SimpleExpandableListAdapter sela = new PluginActionsExpandableListAdapter(this, PluginActivity.this,
		createGroupList(), R.layout.group_row, new String[] { NAME }, new int[] { R.id.row_name },
		createChildList(), R.layout.child_row, new String[] { DESC }, new int[] { R.id.grp_child });
	elv.setAdapter(sela);
	sv.addView(elv);
	return sv;
    }

    /**
     * Creates the View containing the Plugin description.
     * 
     * @return The View containing the Plugin description.
     */
    private TextView createDescriptionView() {
	TextView tv = new TextView(PluginActivity.this);
	tv.setText(plugin.getLocalizedDescription(LANGUAGE_CODE));
	int padding = this.getResources().getDimensionPixelSize(R.dimen.padding);
	tv.setPadding(padding, padding, padding, padding);
	return tv;
    }

    /**
     * Generates the childs list. The outer list has entries for every group. The inner list has entries for every child
     * of that group.
     * 
     * @return The list of childs
     */
    private List<? extends List<? extends Map<String, ?>>> createChildList() {
	ArrayList<ArrayList<HashMap<String, String>>> result = new ArrayList<ArrayList<HashMap<String, String>>>();
	for (AppExtensionSpecification action : plugin.getApplicationActions()) {
	    ArrayList<HashMap<String, String>> secList = new ArrayList<HashMap<String, String>>();
	    HashMap<String, String> child = new HashMap<String, String>();
	    child.put(DESC, action.getLocalizedDescription(LANGUAGE_CODE));
	    secList.add(child);
	    result.add(secList);
	}
	return result;
    }

    /**
     * Generates the groups list. This list contains an entry for every PluginAction.
     * 
     * @return The list of groups
     */
    private List<? extends Map<String, ?>> createGroupList() {
	ArrayList<HashMap<String, String>> result = new ArrayList<HashMap<String, String>>();
	for (AppExtensionSpecification action : plugin.getApplicationActions()) {
	    HashMap<String, String> m = new HashMap<String, String>();
	    m.put(NAME, action.getLocalizedName(LANGUAGE_CODE));
	    result.add(m);
	}
	return result;
    }

}
