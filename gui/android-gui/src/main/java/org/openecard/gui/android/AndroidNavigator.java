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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Exchanger;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;


/**
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidNavigator implements UserConsentNavigator {

    private int curStep = -1;
    private final int numSteps;
    private Context context;
    private StepActivity activity;
    public static List<Step> steps;
    public static AndroidStepResult stepResult;
    private static AndroidNavigator instance = null;

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
    public void close() {
	activity.finish();
    }

    public void setStepResult(boolean back, boolean cancelled, List<OutputInfoUnit> results) throws InterruptedException {
	AndroidNavigator.stepResult.done = true;
	stepResult.back = back;
	stepResult.cancelled = cancelled;
	stepResult.results = results;
	AndroidNavigator.stepResult.syncPoint.exchange(null);
    }

    public StepResult getStepResult() {
	return stepResult;
    }

    public int getCurrentStep(){
	return curStep;
    }

    @Override
    public StepResult current() {
	AndroidNavigator.stepResult.done = false;
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

    public boolean hasPrevious(){
	return curStep>=1;
    }

    @Override
    public boolean hasNext() {
	return curStep < numSteps - 1;
    }

    @Override
    public StepResult next() {
	AndroidNavigator.stepResult.done = false;
	curStep++;
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

    @Override
    public StepResult previous() {
	AndroidNavigator.stepResult.done = false;
	curStep--;
	activity.showStep(steps.get(curStep));
	return this.getStepResult();
    }

    @Override
    public StepResult replaceCurrent(Step arg0) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replaceNext(Step arg0) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replacePrevious(Step arg0) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    class AndroidStepResult implements StepResult {

	public Exchanger syncPoint = new Exchanger();
	public boolean done = false;
	public boolean cancelled = false;
	public boolean back = false;
	public List<OutputInfoUnit> results = null;

	@Override
	public Step getStep() {
	    return steps.get(curStep);
	}

	@Override
	public String getStepID() {
	    return getStep().getID();
	}

	@Override
	public ResultStatus getStatus() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    // return appropriate result
	    synchronized (this) {
		if (cancelled) {
		    return ResultStatus.CANCEL;
		} else if (back) {
		    return ResultStatus.BACK;
		} else {
		    return ResultStatus.OK;
		}
	    }
	}

	@Override
	public boolean isOK() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    synchronized (this) {
		return getStatus() == ResultStatus.OK;
	    }
	}

	@Override
	public boolean isBack() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    synchronized (this) {
		return getStatus() == ResultStatus.BACK;
	    }
	}

	@Override
	public boolean isCancelled() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    synchronized (this) {
		return getStatus() == ResultStatus.CANCEL;
	    }
	}

	@Override
	public List<OutputInfoUnit> getResults() {
	    if (!done) {
		try {
		    syncPoint.exchange(null);
		} catch (InterruptedException ex) {
		}
	    }
	    synchronized (this) {
		if (results == null && stepResult.results!=null) {
		    results = Collections.unmodifiableList(stepResult.results);
		}
		return results;
	    }
	}
    }

}
