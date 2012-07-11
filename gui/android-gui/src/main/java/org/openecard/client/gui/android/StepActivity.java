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

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
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

    LinearLayout ll;
    ArrayList<StepView> views = new ArrayList<StepView>();
    ActionBar actionBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.gui_interface);
	actionBar = getActionBar();
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

    void showStep(final Step step) {
	ll.post(new Runnable() {
	    @Override
	    public void run() {
		ll.removeAllViews();
		if ((step.getTitle() != null))
		    actionBar.setSubtitle(step.getTitle());
		org.openecard.client.gui.android.views.StepView t = null;
		for (InputInfoUnit infoUnitType : step.getInputInfoUnits()) {
		    if (infoUnitType.type().equals(InfoUnitElementType.TEXT)) {
			t = new org.openecard.client.gui.android.views.Text((Text) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.CHECK_BOX)) {
			t = new org.openecard.client.gui.android.views.Box(infoUnitType, StepActivity.this, true);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.RADIO_BOX)) {
			t = new org.openecard.client.gui.android.views.Box(infoUnitType, StepActivity.this, false);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.PASSWORD_FIELD)) {
			t = new org.openecard.client.gui.android.views.AbstractInput((PasswordField) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.TEXT_FIELD)) {
			t = new org.openecard.client.gui.android.views.AbstractInput((TextField) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.HYPERLINK)) {
			t = new org.openecard.client.gui.android.views.Hyperlink((Hyperlink) infoUnitType, StepActivity.this);
		    } else if(infoUnitType.type().equals(InfoUnitElementType.ToggleText)){
			t =  new org.openecard.client.gui.android.views.ToggleText((ToggleText) infoUnitType, StepActivity.this);
		    } else {
			// TODO: log warning
		    }
		    views.add(t);
		    
		    // add a margin of 20dp after every InputInfoUnit
		    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		    layoutParams.setMargins(0, 0, 0, 20);
	
		    ll.addView(t.getView(), layoutParams);
		}

		Button cancel = (Button) findViewById(R.id.button_cancel);
		cancel.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			try {
			    AndroidNavigator.getInstance().setStepResult(false, true, getResultContent());
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		});

		Button b = (Button) findViewById(R.id.button_back);
		if(!(AndroidNavigator.getInstance().hasPrevious())){
		    b.setVisibility(View.INVISIBLE);
		} else {
		    b.setVisibility(View.VISIBLE);
		}
		
		b.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			
			try {
			    AndroidNavigator.getInstance().setStepResult(true, false, getResultContent());
			} catch (InterruptedException e) {

			    e.printStackTrace();
			}

		    }
		});

		Button next = (Button) findViewById(R.id.button_next);
		//TODO internationalization
		if(!AndroidNavigator.getInstance().hasNext()){
		    next.setText("Senden");
		} else {
		    next.setText("Weiter");
		}
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
