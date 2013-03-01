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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import org.openecard.android.ApplicationContext;
import org.openecard.android.R;
import org.openecard.common.I18n;


/**
 * This Activity shows a list of available plugins. By selecting an item on the list the corresponding Activity for the
 * plugin will be started.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class TerminalFactoryActivity extends Activity {

    private final I18n lang = I18n.getTranslation("android");

    private static final String FACTORY_IMPL = "org.openecard.ifd.scio.factory.impl";

    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> factories = new ArrayList<String>(2);
    private ArrayList<String> implementations = new ArrayList<String>(2);

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.plugins);
	fillLists();
	setUpListViewPlugins();
	checkFirstStart();
    }

    private void checkFirstStart() {
	boolean firstStart = false;
	if (getIntent().getExtras() != null) {
	    firstStart = getIntent().getExtras().getBoolean("firstStart");
	}
	if (firstStart) {
	    AlertDialog ad = new AlertDialog.Builder(this).create();

	    // add description of the error
	    ad.setMessage(lang.translationForKey("android.dialogs.terminal_selection"));

	    // Add close button
	    ad.setButton(lang.translationForKey("android.dialogs.quit"), new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
		    dialog.dismiss();
		}
	    });
	    ad.show();
	}
    }

    /**
     * Fills the lists containing the name of the settings and the intents.
     */
    private void fillLists() {
	if (checkUsbHost()) {
	    factories.add("USB");
	    implementations.add("org.openecard.scio.AndroidPCSCFactory");
	}
	if (checkNFCSupport()) {
	    factories.add("NFC");
	    implementations.add("org.openecard.scio.NFCFactory");
	}
    }

    /**
     * Check if NFC is supported by this Smartphone.
     * 
     * @return True if NFC is supported, else false.
     */
    private boolean checkNFCSupport() {
	NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
	NfcAdapter adapter = manager.getDefaultAdapter();
	if (adapter == null || !adapter.isEnabled()) {
	    return false;
	}
	return true;
    }

    /**
     * Check if Phone is rooted by checking if super user binaries exist.
     * 
     * @return True if rooted, else false.
     */
    private boolean checkUsbHost() {
	// TODO find a way to check for usb host support
	return true;
    }

    /**
     * Fills the ListView with the available plugins.
     */
    private void setUpListViewPlugins() {
	ListView listViewPlugins = (ListView) findViewById(R.id.listViewPlugins);

	listAdapter = new ArrayAdapter<String>(this, R.layout.plugin_row, factories);
	listViewPlugins.setAdapter(listAdapter);

	// start the activity to display details of the selected plugin
	listViewPlugins.setOnItemClickListener(new OnItemClickListener() {

	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(FACTORY_IMPL, implementations.get(position));
		editor.commit();
		showRestartDialog();
	    }
	});
    }

    private void showRestartDialog() {
	runOnUiThread(new Runnable() {

	    @Override
	    public void run() {
		AlertDialog ad = new AlertDialog.Builder(TerminalFactoryActivity.this).create();
		ad.setCancelable(false); // This blocks the 'BACK' button

		// add description
		ad.setMessage(lang.translationForKey("android.settings.restart"));

		// Add restart button
		ad.setButton(lang.translationForKey("android.dialogs.restart"),
			new DialogInterface.OnClickListener() {
			    @Override
			    public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				ApplicationContext appContext = (ApplicationContext) getApplicationContext();
				appContext.shutdown();
				System.exit(0);
			    }
			});

		// finally show the dialog
		ad.show();
	    }
	});
    }

}
