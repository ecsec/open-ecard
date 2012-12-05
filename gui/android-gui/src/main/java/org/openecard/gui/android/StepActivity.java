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

package org.openecard.gui.android;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.I18n;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.android.views.StepView;
import org.openecard.gui.definition.AbstractBox;
import org.openecard.gui.definition.Hyperlink;
import org.openecard.gui.definition.ImageBox;
import org.openecard.gui.definition.InfoUnitElementType;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.TextField;
import org.openecard.gui.definition.ToggleText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This activity displays a specific step when showStep is called.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class StepActivity extends Activity {

    private static final Logger logger = LoggerFactory.getLogger(StepActivity.class.getName());
    private final I18n lang = I18n.getTranslation("gui");

    private LinearLayout llGuiInterface;
    private ArrayList<StepView> views = new ArrayList<StepView>();
    private ActionBar actionBar;
    private LinearLayout llOutwards;
    private ScrollView scrollView;
    private View view;
    private LinearLayout ll1;
    private AndroidNavigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.gui_interface);
	actionBar = getActionBar();
	llGuiInterface = (LinearLayout) findViewById(R.id.linearLayoutGUIInterface);
	llOutwards = (LinearLayout) findViewById(R.id.linearLayoutOutwards);
	scrollView = (ScrollView) findViewById(R.id.scrollView1);
	view = findViewById(R.id.view1);
	ll1 = (LinearLayout) findViewById(R.id.linearLayout1);
	navigator = AndroidNavigator.getInstance();
	navigator.setActivity(this);
	if (savedInstanceState != null) {
	    showStep(navigator.getSteps().get(savedInstanceState.getInt("step")));
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
	outState.putInt("step", navigator.getCurrentStep());
    }

    void showStep(final Step step) {
	views.clear();
	llOutwards.post(new Runnable() {

	    @Override
	    public void run() {
		llOutwards.removeAllViews();

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
			LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(0, 2, 0, 2);

		for (Step p : navigator.getSteps()) {
		    TextView titleBar = createStepTitleBar(p.getTitle());
		    llOutwards.addView(titleBar, layoutParams);

		    if (p.getTitle().equals(step.getTitle())) {
			titleBar.setTypeface(null, Typeface.BOLD);
			titleBar.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleBar.getTextSize() * 1.2f);
			llOutwards.addView(scrollView);
		    }

		}

		llOutwards.addView(view);
		llOutwards.addView(ll1);
	    }
	});

	llGuiInterface.post(new Runnable() {
	    @Override
	    public void run() {
		llGuiInterface.removeAllViews();

		if ((step.getTitle() != null)) {
		    actionBar.setSubtitle(step.getTitle());
		}
		org.openecard.gui.android.views.StepView t = null;
		for (InputInfoUnit infoUnitType : step.getInputInfoUnits()) {
		    if (infoUnitType.type().equals(InfoUnitElementType.TEXT)) {
			t = new org.openecard.gui.android.views.Text((Text) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.CHECK_BOX)) {
			t = new org.openecard.gui.android.views.Box((AbstractBox) infoUnitType, StepActivity.this, true);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.RADIO_BOX)) {
			t = new org.openecard.gui.android.views.Box((AbstractBox) infoUnitType, StepActivity.this, false);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.PASSWORD_FIELD)) {
			t = new org.openecard.gui.android.views.AbstractInput((PasswordField) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.TEXT_FIELD)) {
			t = new org.openecard.gui.android.views.AbstractInput((TextField) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.HYPERLINK)) {
			t = new org.openecard.gui.android.views.Hyperlink((Hyperlink) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.IMAGE_BOX)) {
			t = new org.openecard.gui.android.views.ImageBox((ImageBox) infoUnitType, StepActivity.this);
		    } else if (infoUnitType.type().equals(InfoUnitElementType.TOGGLE_TEXT)) {
			t =  new org.openecard.gui.android.views.ToggleText((ToggleText) infoUnitType, StepActivity.this);
		    } else {
			logger.warn("Not adding currently unsupported InputInfoUnit of type: {}", infoUnitType.type());
			continue;
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
			// in case there is a running action, kill it and bail out
			if (navigator.getRunningAction() != null && !navigator.getRunningAction().isDone()) {
			    logger.debug("Canceling execution of the currently running StepAction.");
			    navigator.getRunningAction().cancel(true);
			    return;
			}
			try {
			    navigator.setStepResult(ResultStatus.CANCEL, getResultContent());
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		});

		Button b = (Button) findViewById(R.id.button_back);
		if (!(navigator.hasPrevious())) {
		    b.setVisibility(View.GONE);
		} else {
		    b.setVisibility(View.VISIBLE);
		}

		b.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {

			try {
			    navigator.setStepResult(ResultStatus.BACK, getResultContent());
			} catch (InterruptedException e) {

			    e.printStackTrace();
			}

		    }
		});

		Button next = (Button) findViewById(R.id.button_next);

		if (!navigator.hasNext()) {
		    next.setText(lang.translationForKey("button.finish"));
		} else {
		    next.setText(lang.translationForKey("button.next"));
		}
		next.setOnClickListener(new OnClickListener() {
		    public void onClick(View v) {
			try {
			    navigator.setStepResult(ResultStatus.OK, getResultContent());
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		});
	    }
	});

	if (step.isInstantReturn()) {
	    llGuiInterface.post(new Runnable() {
		@Override
		public void run() {
		    try {
			Button next = (Button) findViewById(R.id.button_next);
			next.setVisibility(View.GONE);
			navigator.setStepResult(ResultStatus.OK, getResultContent());
		    } catch (InterruptedException ignore) {
		    }
		}
	    });
	}
    }

    private TextView createStepTitleBar(String title) {
	TextView tv = new TextView(StepActivity.this);
	tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
	tv.setText(title);
	tv.setWidth(llOutwards.getWidth());
	// TODO dont use fixed colors -> get from theme
	tv.setTextColor(Color.WHITE);
	tv.setBackgroundColor(0xFF909090);
	return tv;
    }

}
