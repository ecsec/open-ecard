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

package org.openecard.client.gui.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.openecard.client.gui.android.views.StepView;
import org.openecard.client.gui.definition.*;


/**
 * This activity displays a specific step when showStep is called
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class StepActivity extends Activity {

    private TextView mTitle = null;
    LinearLayout ll;
    ArrayList<StepView> views = new ArrayList<StepView>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	setContentView(R.layout.gui_interface);
	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
	mTitle = (TextView) findViewById(R.id.title_left_text);
	mTitle.setText(R.string.app_name);
	mTitle = (TextView) findViewById(R.id.title_right_text);
	ll = (LinearLayout) findViewById(R.id.linearLayoutGUIInterface);
	AndroidNavigator.getInstance().setActivity(this);
    }

    /**
     * Get result for all components on the frame that support result values.
     * 
     * @return List containg all result values. As a matter of fact this list
     *         can be empty.
     */
    public List<OutputInfoUnit> getResultContent() {
	ArrayList<OutputInfoUnit> result = new ArrayList<OutputInfoUnit>();
	for (StepView next : views) {
	    if (next.isValueType()) {
		result.add(next.getValue());
	    }
	}
	return result;
    }

    void showStep(final Step step2) {
	ll.post(new Runnable() {
	    @Override
	    public void run() {
		ll.removeAllViews();
		org.openecard.client.gui.definition.Step a = step2;
		if (a.getID() != null)
		    mTitle.setText(a.getID());
		org.openecard.client.gui.android.views.StepView t = null;
		for (InputInfoUnit infoUnitType : a.getInputInfoUnits()) {
		    if (infoUnitType.type().equals(InfoUnitElementType.TEXT)) {
			t = new org.openecard.client.gui.android.views.Text((Text) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.CHECK_BOX)) {
			t = new org.openecard.client.gui.android.views.Checkbox((Checkbox) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.RADIO_BOX)) {
			t = new org.openecard.client.gui.android.views.Radiobutton((Radiobox) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.PASSWORD_FIELD)) {
			t = new org.openecard.client.gui.android.views.AbstractInput((PasswordField) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.TEXT_FIELD)) {
			t = new org.openecard.client.gui.android.views.AbstractInput((TextField) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.HYPERLINK)) {
			t = new org.openecard.client.gui.android.views.Hyperlink((Hyperlink) infoUnitType, StepActivity.this);
		    }
		    views.add(t);
		    ll.addView(t.getView());
		}

		Button cancel = (Button) findViewById(R.id.button_cancel);
		cancel.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			try {
			    AndroidNavigator.getInstance().setStepResult(false, true, null);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		});

		Button b = (Button) findViewById(R.id.button_back);
		b.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {

			try {
			    AndroidNavigator.getInstance().setStepResult(true, false, null);
			} catch (InterruptedException e) {

			    e.printStackTrace();
			}

		    }
		});

		Button next = (Button) findViewById(R.id.button_next);
		next.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			try {
			    AndroidNavigator.getInstance().setStepResult(false, false, getResultContent());
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		});
	    }
	});
    }

}
