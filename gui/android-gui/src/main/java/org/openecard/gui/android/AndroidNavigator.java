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

import android.content.Context;
import android.content.Intent;
import java.util.List;
import java.util.concurrent.Future;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;


/**
 *
 * @author Dirk Petrautzki
 */
public class AndroidNavigator implements UserConsentNavigator {

    private int curStep = -1;
    private final int numSteps;
    private Context context;
    private StepActivity activity;
    private static List<Step> steps;
    private static AndroidStepResult stepResult;
    private static AndroidNavigator instance;
    private static Future action;

    public static AndroidNavigator getInstance() {
	return instance;
    }

    public AndroidNavigator(List<Step> s, Context context) {
	instance = this;
	steps = s;
	numSteps = steps.size();
	this.context = context;
	stepResult = new AndroidStepResult();
	Intent i = new Intent(this.context, StepActivity.class);
	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	context.startActivity(i);
	while (!(activity instanceof StepActivity)) {
	    try {
		Thread.sleep(500);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
    }

    public void setActivity(StepActivity activity) {
	this.activity = activity;
    }

    @Override
    public void setRunningAction(Future action) {
	this.action = action;
    }

    public Future getRunningAction() {
	return action;
    }

    @Override
    public void close() {
	activity.finish();
    }

    public void setStepResult(ResultStatus status, List<OutputInfoUnit> results) throws InterruptedException {
	stepResult.setStatus(status);
	stepResult.setResults(results);
	// don't call synchronize because of the set status it wouldn't call exchange
	stepResult.getSyncPoint().exchange(null);
    }

    public StepResult getStepResult() {
	return stepResult;
    }

    public int getCurrentStep() {
	return curStep;
    }

    public List<Step> getSteps() {
	return steps;
    }

    @Override
    public StepResult current() {
	stepResult.setStatus(null);
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

    public boolean hasPrevious() {
	return curStep >= 1;
    }

    @Override
    public boolean hasNext() {
	return curStep < numSteps - 1;
    }

    @Override
    public StepResult next() {
	stepResult.setStatus(null);
	curStep++;
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

    @Override
    public StepResult previous() {
	stepResult.setStatus(null);
	curStep--;
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

    @Override
    public StepResult replaceCurrent(Step replacementStep) {
	steps.remove(curStep);
	steps.add(curStep, replacementStep);
	stepResult.setStatus(null);
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

    @Override
    public StepResult replaceNext(Step replacementStep) {
	curStep = curStep + 1;
	if (curStep < steps.size()) {
	    steps.remove(curStep);
	}
	steps.add(curStep, replacementStep);
	stepResult.setStatus(null);
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

    @Override
    public StepResult replacePrevious(Step replacementStep) {
	if (curStep > 0) {
	    curStep = curStep - 1;
	    steps.remove(curStep);
	}
	steps.add(curStep, replacementStep);
	stepResult.setStatus(null);
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

}
