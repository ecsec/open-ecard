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

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import java.util.List;
import java.util.Map;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.addon.manifest.AppExtensionSpecification;
import org.openecard.android.AddonManagerSingleton;
import org.openecard.common.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Extends {@code SimpleExpandableListAdapter} and overrides the getChildView method to be able to add the
 * StartAction-Button below the PluginAction's description.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
final class PluginActionsExpandableListAdapter extends SimpleExpandableListAdapter {

    // logger and translation
    private static final Logger logger = LoggerFactory.getLogger(PluginActionsExpandableListAdapter.class);
    private final I18n lang = I18n.getTranslation("android");

    private PluginActivity pluginActivity;
    private Context context;

    PluginActionsExpandableListAdapter(PluginActivity pluginActivity, Context context,
	    List<? extends Map<String, ?>> groupData, int groupLayout, String[] groupFrom, int[] groupTo,
	    List<? extends List<? extends Map<String, ?>>> childData, int childLayout, String[] childFrom, int[] childTo) {
	super(context, groupData, groupLayout, groupFrom, groupTo, childData, childLayout, childFrom, childTo);
	this.pluginActivity = pluginActivity;
	this.context = context;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
	    ViewGroup parent) {
	LinearLayout childView = (LinearLayout) super.getChildView(groupPosition, childPosition, isLastChild, 
		convertView, parent);

	// remove is necessary
	if (childView.getChildCount() > 1) {
	    childView.removeViewAt(1);
	}

	AppExtensionSpecification action = pluginActivity.getAddon().getApplicationActions().get(groupPosition);

	// set orientation to vertical so the button will be under the description text
	childView.setOrientation(LinearLayout.VERTICAL);

	Button btnStartAction = setUpStartActionButton(action);

	// add the button on the right side below the description text
	LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
		LinearLayout.LayoutParams.WRAP_CONTENT);
	buttonParams.gravity = Gravity.RIGHT;
	childView.addView(btnStartAction, buttonParams);

	return childView;
    }

    /**
     * Creates the Button to start the given action.
     * 
     * @param action The action to create a Button for.
     * @return The Button with a corresponding OnClickListener set.
     */
    private Button setUpStartActionButton(final AppExtensionSpecification action) {
	Button btnStartAction = new Button(context);
	btnStartAction.setText(lang.translationForKey("settings.plugins.actions.start"));
	btnStartAction.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		Thread actionThread = new Thread(new Runnable() {
		    @Override
		    public void run() {
			AddonSpecification addonSpec = pluginActivity.getAddon();
			String actionId = action.getId();
			AddonManagerSingleton.getInstance().getAppExtensionAction(addonSpec, actionId).execute();
		    }
		});
		actionThread.start();
	    }
	});
	return btnStartAction;
    }

}
