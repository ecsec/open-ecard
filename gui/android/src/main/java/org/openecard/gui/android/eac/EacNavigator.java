/****************************************************************************
 * Copyright (C) 2017 ecsec GmbH.
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

package org.openecard.gui.android.eac;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.android.AndroidResult;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;


/**
 *
 * @author Tobias Wich
 */
public class EacNavigator implements UserConsentNavigator {

    private final List<Step> steps;

    private EacGuiImpl guiService = null;
    private int idx = -1;
    private boolean pinFirstUse = true;


    public EacNavigator(EacGuiImpl guiService, List<Step> steps) {
	this.guiService = guiService;
	this.steps = steps;
    }
    
    public static EacNavigator createFrom(Context androidCtx, UserConsentDescription ucd) throws UnsupportedOperationException {
	ArrayList<Step> steps = new ArrayList<>(ucd.getSteps());
	EacGuiImpl guiService;
	
	String dialogType = ucd.getDialogType();
	if ("EAC".equals(dialogType)) {
	    // get GUI service
	    guiService = new EacGuiImpl();
	    EacGuiService.setGuiImpl(guiService);
	} else {
	    throw new UnsupportedOperationException("Unsupported Dialog type requested.");
	}
	return new EacNavigator(guiService, steps);
    }
    
    @Override
    public boolean hasNext() {
	return idx < (steps.size() - 1);
    }

    @Override
    public StepResult current() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult next() {
	if (idx == -1) {
	    idx++;
	    return new AndroidResult(steps.get(idx), ResultStatus.OK, Collections.EMPTY_LIST);
	} else if (idx == 0) {
	    idx++;
	    Step cvcStep = steps.get(0);
	    Step chatStep = steps.get(1);
	    try {
		this.guiService.loadValuesFromSteps(cvcStep, chatStep);
		List<OutputInfoUnit> outInfo = this.guiService.getSelection();
		return new AndroidResult(chatStep, ResultStatus.OK, outInfo);
	    } catch (InterruptedException ex) {
		return new AndroidResult(chatStep, ResultStatus.INTERRUPTED, Collections.EMPTY_LIST);
	    }
	} else if (idx == 1) {
	    if (pinFirstUse) {
		pinFirstUse = false;
	    } else {
		this.guiService.setPinCorrect(false);
	    }

	    idx++;
	    Step pinStep = steps.get(2);
	    try {
		List<OutputInfoUnit> outInfo = this.guiService.getPinResult(pinStep);
		writeBackValues(pinStep.getInputInfoUnits(), outInfo);
		return new AndroidResult(pinStep, ResultStatus.OK, outInfo);
	    } catch (InterruptedException ex) {
		return new AndroidResult(pinStep, ResultStatus.INTERRUPTED, Collections.EMPTY_LIST);
	    }
	} else if (idx == 2) {
	    idx++;
	    Step s = steps.get(idx);
	    if ("PROTOCOL_GUI_STEP_PROCESSING".equals(s.getID())) {
		this.guiService.setPinCorrect(true);
		return new AndroidResult(s, ResultStatus.OK, Collections.EMPTY_LIST);
	    } else {
		this.guiService.setPinCorrect(false);
		try {
		    this.guiService.getPinResult(s);
		    return new AndroidResult(s, ResultStatus.OK, Collections.EMPTY_LIST);
		} catch (InterruptedException ex) {
		    return new AndroidResult(s, ResultStatus.INTERRUPTED, Collections.EMPTY_LIST);
		}
	    }
	} else {
	    Step s = steps.get(idx);
	    idx++;
	    return new AndroidResult(s, ResultStatus.OK, Collections.EMPTY_LIST);
	}
    }

    @Override
    public StepResult previous() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replaceCurrent(Step step) {
	steps.set(idx, step);
	return current();
    }

    @Override
    public StepResult replaceNext(Step step) {
	steps.set(idx+1, step);
	return next();
    }

    @Override
    public StepResult replacePrevious(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRunningAction(Future<?> action) {
	// don't care about the action
    }

    @Override
    public void close() {
	EacGuiService.prepare();
    }

    private void writeBackValues(List<InputInfoUnit> inInfo, List<OutputInfoUnit> outInfo) {
	for (InputInfoUnit infoInUnit : inInfo) {
	    for (OutputInfoUnit infoOutUnit : outInfo) {
		if (infoInUnit.getID().equals(infoOutUnit.getID())) {
		    infoInUnit.copyContentFrom(infoOutUnit);
		}
	    }
	}
    }

}
