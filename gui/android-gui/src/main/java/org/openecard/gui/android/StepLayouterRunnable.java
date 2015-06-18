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
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import java.util.ArrayList;
import org.openecard.common.I18n;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.android.views.AbstractInput;
import org.openecard.gui.android.views.StepView;
import org.openecard.gui.definition.AbstractBox;
import org.openecard.gui.definition.Hyperlink;
import org.openecard.gui.definition.ImageBox;
import org.openecard.gui.definition.InfoUnitElementType;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.TextField;
import org.openecard.gui.definition.ToggleText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Runnable layouts the current step in the StepActivity on the UI-Thread.
 *
 * @author Dirk Petrautzki
 *
 */
final class StepLayouterRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StepLayouterRunnable.class.getName());
    private final I18n lang = I18n.getTranslation("gui");

    private final Step step;
    private final StepActivity stepActivity;
    private final Button btnNext;
    private final Button btnBack;
    private final LinearLayout llGuiInterface;
    private final LinearLayout llOutwards;
    private final LinearLayout linearLayoutSidebar;
    private final AndroidNavigator navigator;
    private final ActionBar actionBar;
    private final ScrollView scrollView;
    private final LinearLayout ll1;
    private final View view;
    private final ArrayList<StepView> views;

    StepLayouterRunnable(Step step, StepActivity stepActivity) {
	this.step = step;
	this.stepActivity = stepActivity;
	btnNext = stepActivity.getBtnNext();
	btnBack = stepActivity.getBtnBack();
	llGuiInterface = stepActivity.getLlGuiInterface();
	llOutwards = stepActivity.getLlOutwards();
	navigator = stepActivity.getNavigator();
	actionBar = stepActivity.getActionBar();
	scrollView = stepActivity.getScrollView();
	linearLayoutSidebar = stepActivity.getLinearLayoutSidebar();
	ll1 = stepActivity.getLl1();
	view = stepActivity.getView();
	views = stepActivity.getViews();
    }

    TextView createStepTitleBar(String title) {
	TextView tv = new TextView(stepActivity);
	tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
	tv.setText(title);
	tv.setWidth(llOutwards.getWidth());
	// TODO dont use fixed colors -> get from theme
	tv.setTextColor(Color.WHITE);
	tv.setBackgroundColor(0xFF909090);
	return tv;
    }

    @Override
    public void run() {
	llOutwards.removeAllViews();
	if (linearLayoutSidebar != null) {
	    linearLayoutSidebar.removeAllViews();
	}

	LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT);
	layoutParams.setMargins(0, 2, 0, 2);

	for (Step p : navigator.getSteps()) {
	    TextView titleBar = createStepTitleBar(p.getTitle());
	    TextView stepSide = new TextView(stepActivity);
	    stepSide.setPadding(10, 10, 10, 10);
	    stepSide.setText(p.getTitle());
	    if (linearLayoutSidebar == null) {
		llOutwards.addView(titleBar, layoutParams);
	    }

	    if (p.getTitle().equals(step.getTitle())) {
		titleBar.setTypeface(null, Typeface.BOLD);
		titleBar.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleBar.getTextSize() * 1.2f);
		llOutwards.addView(scrollView);
		stepSide.setTypeface(null, Typeface.BOLD);
		stepSide.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleBar.getTextSize() * 1.2f);
	    }
	    if (linearLayoutSidebar != null) {
		linearLayoutSidebar.addView(stepSide);
	    }
	}


	llOutwards.addView(view);
	llOutwards.addView(ll1);

	llGuiInterface.removeAllViews();

	if ((step.getTitle() != null)) {
	    actionBar.setSubtitle(step.getTitle());
	}
	org.openecard.gui.android.views.StepView t = null;
	for (InputInfoUnit infoUnitType : step.getInputInfoUnits()) {
	    if (infoUnitType.type().equals(InfoUnitElementType.TEXT)) {
		t = new org.openecard.gui.android.views.Text((Text) infoUnitType, stepActivity);
	    } else if (infoUnitType.type().equals(InfoUnitElementType.CHECK_BOX)) {
		t = new org.openecard.gui.android.views.Box((AbstractBox) infoUnitType, stepActivity, true);
	    } else if (infoUnitType.type().equals(InfoUnitElementType.RADIO_BOX)) {
		t = new org.openecard.gui.android.views.Box((AbstractBox) infoUnitType, stepActivity, false);
	    } else if (infoUnitType.type().equals(InfoUnitElementType.PASSWORD_FIELD)) {
		t = new org.openecard.gui.android.views.AbstractInput((PasswordField) infoUnitType, stepActivity);
		((AbstractInput) t).getEditText().setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
			    stepActivity.hideSoftwareKeyboard();
			    try {
				navigator.setStepResult(ResultStatus.OK, stepActivity.getResultContent());
			    } catch (InterruptedException ignore) {
			    }
			}
			return false;
		    }
		});
	    } else if (infoUnitType.type().equals(InfoUnitElementType.TEXT_FIELD)) {
		t = new org.openecard.gui.android.views.AbstractInput((TextField) infoUnitType, stepActivity);
	    } else if (infoUnitType.type().equals(InfoUnitElementType.HYPERLINK)) {
		t = new org.openecard.gui.android.views.Hyperlink((Hyperlink) infoUnitType, stepActivity);
	    } else if (infoUnitType.type().equals(InfoUnitElementType.IMAGE_BOX)) {
		t = new org.openecard.gui.android.views.ImageBox((ImageBox) infoUnitType, stepActivity);
	    } else if (infoUnitType.type().equals(InfoUnitElementType.TOGGLE_TEXT)) {
		t = new org.openecard.gui.android.views.ToggleText((ToggleText) infoUnitType, stepActivity);
	    } else {
		logger.warn("Not adding currently unsupported InputInfoUnit of type: {}", infoUnitType.type());
		continue;
	    }
	    views.add(t);

	    // add a margin of 20dp after every InputInfoUnit
	    layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
		    LinearLayout.LayoutParams.WRAP_CONTENT);
	    layoutParams.setMargins(0, 0, 0, 20);

	    llGuiInterface.addView(t.getView(), layoutParams);
	}

	if ((navigator.hasPrevious() && step.isReversible())) {
	    btnBack.setVisibility(View.VISIBLE);
	} else {
	    btnBack.setVisibility(View.GONE);
	}

	if (!navigator.hasNext()) {
	    btnNext.setText(lang.translationForKey("button.finish"));
	} else {
	    btnNext.setText(lang.translationForKey("button.next"));
	}

	if (step.isInstantReturn()) {
	    btnNext.setVisibility(View.GONE);
	    try {
		navigator.setStepResult(ResultStatus.OK, stepActivity.getResultContent());
	    } catch (InterruptedException ignore) {

	    }
	} else {
	    btnNext.setVisibility(View.VISIBLE);
	}
    }

}
