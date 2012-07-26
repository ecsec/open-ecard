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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
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

    private LinearLayout llGuiInterface;
    ArrayList<StepView> views = new ArrayList<StepView>();
    private ActionBar actionBar;
    private LinearLayout llOutwards;
    private ScrollView scrollView;
    private View view;
    private LinearLayout ll1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.gui_interface);
	actionBar = getActionBar();
	llGuiInterface = (LinearLayout) findViewById(R.id.linearLayoutGUIInterface);
	llOutwards = (LinearLayout) findViewById(R.id.linearLayoutOutwards);
	scrollView = (ScrollView) findViewById(R.id.scrollView1);
	view = (View)findViewById(R.id.view1);
	ll1 = (LinearLayout) findViewById(R.id.linearLayout1);
	AndroidNavigator.getInstance().setActivity(this);
	if(savedInstanceState!=null){
	    this.showStep(AndroidNavigator.steps.get(savedInstanceState.getInt("step")));
	}
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("step", AndroidNavigator.getInstance().getCurrentStep());
    }
    
    void showStep(final Step step) {
	views.clear();
	llOutwards.post(new Runnable() {
	    
	    @Override
	    public void run() {
		llOutwards.removeAllViews();
		int i = 0;
		for(Step p : AndroidNavigator.steps){
		    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		    layoutParams.setMargins(0, 2, 0, 2);
		    TextView test = new TextView(StepActivity.this);
		    test.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			test.setText(p.getTitle());
			test.setWidth(llOutwards.getWidth());
			//TODO dont use fixed colors -> get from theme
			test.setTextColor(Color.WHITE);
			test.setBackgroundColor(0xFF909090);
			llOutwards.addView(test, layoutParams);

			i++;
			if(p.getTitle().equals(step.getTitle())){
			    test.setTypeface(null,Typeface.BOLD);
			    test.setTextSize(TypedValue.COMPLEX_UNIT_PX, test.getTextSize()*1.2f);
			    break;
			}
			
		}
		
		llOutwards.addView(scrollView);
		
		for(;i<AndroidNavigator.steps.size();i++){
		    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			     LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		    layoutParams.setMargins(0, 2, 0, 2);
		    TextView test = new TextView(StepActivity.this);
		    test.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			test.setText(AndroidNavigator.steps.get(i).getTitle());
			test.setWidth(llOutwards.getWidth());
			//TODO dont use fixed colors -> get from theme
			test.setTextColor(Color.WHITE);
			test.setBackgroundColor(0xFF909090);
			
			llOutwards.addView(test, layoutParams);
		}
		
		llOutwards.addView(view);
		llOutwards.addView(ll1);
	    }
	});
	
	llGuiInterface.post(new Runnable() {
	    @Override
	    public void run() {
		llGuiInterface.removeAllViews();

		if ((step.getTitle() != null))
		    actionBar.setSubtitle(step.getTitle());
		org.openecard.client.gui.android.views.StepView t = null;
		for (InputInfoUnit infoUnitType : step.getInputInfoUnits()) {
		    if (infoUnitType.type().equals(InfoUnitElementType.TEXT)) {
			t = new org.openecard.client.gui.android.views.Text((Text) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.CHECK_BOX)) {
			t = new org.openecard.client.gui.android.views.Box((AbstractBox) infoUnitType, StepActivity.this, true);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.RADIO_BOX)) {
			t = new org.openecard.client.gui.android.views.Box((AbstractBox) infoUnitType, StepActivity.this, false);
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
	
		    llGuiInterface.addView(t.getView(), layoutParams);
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
		    b.setVisibility(View.GONE);
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
