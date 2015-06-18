/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.gui.swing;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Exchanger;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;


/**
 * Blocking StepResult implementation for the Swing GUI.
 * After the values are set, the {@link #synchronize()} method can be called, so that any listeners can proceed and
 * fetch the values.
 *
 * @author Tobias Wich
 */
public class SwingStepResult implements StepResult {

    public Exchanger<Void> syncPoint = new Exchanger<>();
    private Step step;
    private ResultStatus status;
    private Step replacement;
    private List<OutputInfoUnit> results;

    public SwingStepResult(Step step) {
	this(step, null);
    }

    public SwingStepResult(Step step, ResultStatus status) {
	this.step = step;
	this.status = status;
    }

    public void setResultStatus(ResultStatus status) {
	this.status = status;
    }

    public void setResult(List<OutputInfoUnit> results) {
	this.results = results;
    }

    @Override
    public Step getStep() {
	return step;
    }
    @Override
    public String getStepID() {
	return step.getID();
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
    public boolean isReload() {
	// wait until values are present
	synchronize();
	synchronized (this) {
	    return getStatus() == ResultStatus.RELOAD;
	}    }

    @Override
    public List<OutputInfoUnit> getResults() {
	// wait until values are present
	synchronize();
	synchronized (this) {
	    if (results == null) {
		results = Collections.unmodifiableList(new LinkedList<OutputInfoUnit>());
	    }
	    return results;
	}
    }

    public void setReplacement(Step replacement) {
	this.replacement = replacement;
    }

    @Override
    public Step getReplacement() {
	return replacement;
    }

    private void synchronize() {
	if (status == null) {
	    try {
		syncPoint.exchange(null);
	    } catch (InterruptedException ignore) {
		// TODO: maybe setting status to cancel makes sense here
	    }
	}
    }

}
