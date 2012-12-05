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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Exchanger;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;


/**
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 * 
 */
class AndroidStepResult implements StepResult {

    private Exchanger syncPoint = new Exchanger();
    private ResultStatus status;
    private List<OutputInfoUnit> results;
    private AndroidNavigator navigator;

    public AndroidStepResult() {
	navigator = AndroidNavigator.getInstance();
    }

    public void setStatus(ResultStatus status) {
	this.status = status;
    }

    public void setResults(List<OutputInfoUnit> results) {
	this.results = results;
    }

    @Override
    public Step getStep() {
	int curStep = navigator.getCurrentStep();
	return navigator.getSteps().get(curStep);
    }

    @Override
    public String getStepID() {
	return getStep().getID();
    }

    @Override
    public ResultStatus getStatus() {
	synchronize();
	return status;
    }

    @Override
    public boolean isOK() {
	// wait until values are present (blocks until triggered
	synchronize();
	synchronized (this) {
	    return getStatus() == ResultStatus.OK;
	}
    }

    @Override
    public boolean isBack() {
	// wait until values are present
	synchronize();
	synchronized (this) {
	    return getStatus() == ResultStatus.BACK;
	}
    }

    @Override
    public boolean isCancelled() {
	// wait until values are present
	synchronize();
	synchronized (this) {
	    return getStatus() == ResultStatus.CANCEL;
	}
    }

    @Override
    public List<OutputInfoUnit> getResults() {
	// wait until values are present
	synchronize();
	synchronized (this) {
	    if (results == null && navigator.getStepResult().getResults() != null) {
		results = Collections.unmodifiableList(navigator.getStepResult().getResults());
	    }
	    return results;
	}
    }

    public void synchronize() {
	if (status == null) {
	    try {
		syncPoint.exchange(null);
	    } catch (InterruptedException ignore) {
		// TODO: maybe setting status to cancel makes sense here
	    }
	}
    }

}
