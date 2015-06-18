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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import org.openecard.android.AndroidUtils;
import org.openecard.android.R;
import org.openecard.common.I18n;


/**
 * This Activity shows a list of available logging options. Logging types can be (de-)activated by (un-)checking the
 * corresponding checkbox.
 * 
 * @author Dirk Petrautzki
 */
public class LoggingActivity extends Activity {

    private final I18n lang = I18n.getTranslation("android");

    private static final String LOGGINGTYPE = AndroidUtils.LOGGINGTYPE;
    private static final String SDCARD = "android.logging.sdcard";
    private static final String LOGCAT = "android.logging.logcat";

    private ArrayAdapter<String> listAdapter;
    private String[] logTypes = new String[2];
    private Boolean[] status = new Boolean[2];

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.plugins);
	fillArrays();
	setUpListViewLogging();
    }

    /**
     * Fills the arrays containing the name of the logging types and their activation status.
     */
    private void fillArrays() {
	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	int loggingTypeIndex = preferences.getInt(LOGGINGTYPE, AndroidUtils.DEFAULT);
	LoggingTypes loggingType = LoggingTypes.values()[loggingTypeIndex];
	logTypes[0] = lang.translationForKey(LOGCAT);
	logTypes[1] = lang.translationForKey(SDCARD);

	if (loggingType.equals(LoggingTypes.LOGCAT_SDCARD)) {
	    status[0] = true;
	    status[1] = true;
	} else if (loggingType.equals(LoggingTypes.LOGCAT)) {
	    status[0] = true;
	    status[1] = false;
	} else if (loggingType.equals(LoggingTypes.SDCARD)) {
	    status[0] = false;
	    status[1] = true;
	} else {
	    status[0] = false;
	    status[1] = false;
	}
    }

    /**
     * Fills the ListView with the available logging types.
     */
    private void setUpListViewLogging() {
	ListView listViewPlugins = (ListView) findViewById(R.id.listViewPlugins);
	listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, logTypes);
	listViewPlugins.setAdapter(listAdapter);
	listViewPlugins.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	listViewPlugins.setItemChecked(0, status[0]);
	listViewPlugins.setItemChecked(1, status[1]);

	// reconfigure logging after an item is clicked
	listViewPlugins.setOnItemClickListener(new OnItemClickListener() {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		CheckedTextView checkedTextView = (CheckedTextView) view;
		status[position] = checkedTextView.isChecked();
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit();
		if (status[0] && status[1]) {
		    editor.putInt(LOGGINGTYPE, LoggingTypes.LOGCAT_SDCARD.ordinal());
		} else if (status[0]) {
		    editor.putInt(LOGGINGTYPE, LoggingTypes.LOGCAT.ordinal());
		} else if (status[1]) {
		    editor.putInt(LOGGINGTYPE, LoggingTypes.SDCARD.ordinal());
		} else {
		    editor.putInt(LOGGINGTYPE, LoggingTypes.NONE.ordinal());
		}
		editor.commit();
		AndroidUtils.initLogging(LoggingActivity.this);
	    }
	});
    }

}
