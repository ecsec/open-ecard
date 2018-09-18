/****************************************************************************
 * Copyright (C) 2017-2018 ecsec GmbH.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.android.AndroidNavigator;
import org.openecard.gui.android.AndroidResult;
import org.openecard.gui.android.GuiIfaceReceiver;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.openecard.sal.protocol.eac.gui.CHATStep;
import org.openecard.sal.protocol.eac.gui.CVCStep;
import org.openecard.sal.protocol.eac.gui.EacPinStatus;
import org.openecard.sal.protocol.eac.gui.ErrorStep;
import org.openecard.sal.protocol.eac.gui.PINStep;
import org.openecard.sal.protocol.eac.gui.ProcessingStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class EacNavigator extends AndroidNavigator {

    private static final Logger LOG = LoggerFactory.getLogger(EacNavigator.class);

    private final List<Step> steps;
    private final GuiIfaceReceiver<EacGuiImpl> ifaceReceiver;
    private final EacGuiImpl guiService;

    private int idx = 0;
    private boolean pinFirstUse = true;
    private boolean finalPinStatusDelivered = false;


    public EacNavigator(UserConsentDescription uc, GuiIfaceReceiver<EacGuiImpl> ifaceReceiver) {
	this.steps = new ArrayList<>(uc.getSteps());
	this.ifaceReceiver = ifaceReceiver;
	this.guiService = ifaceReceiver.getUiInterface().derefNonblocking();
    }

    @Override
    public boolean hasNext() {
	return idx < steps.size();
    }

    @Override
    public StepResult current() {
	// reduce index by one and call next which increases idx by one
	// --> (-1 + 1 = 0)
	idx--;
	return next();
    }

    @Override
    public StepResult next() {
	// if cancel call has been issued, abort the whole process
	if (this.guiService.isCancelled()) {
	    // prevent index out of bounds
	    int i = idx > steps.size() ? steps.size() - 1 : idx;
	    return new AndroidResult(steps.get(i), ResultStatus.CANCEL, Collections.emptyList());
	}

	// get current step
	Step curStep = steps.get(idx);

	// handle step display
	if (CVCStep.STEP_ID.equals(curStep.getID())) {
	    idx++;
	    // step over CVC step, its data is processed in the next step
	    return new AndroidResult(curStep, ResultStatus.OK, Collections.emptyList());
	} else if (CHATStep.STEP_ID.equals(curStep.getID())) {
	    idx++;
	    Step cvcStep = steps.get(0);
	    Step chatStep = steps.get(1);
	    return displayAndExecuteBackground(chatStep, () -> {
		try {
		    this.guiService.loadValuesFromSteps(cvcStep, chatStep);
		    List<OutputInfoUnit> outInfo = this.guiService.getSelection();
		    return new AndroidResult(chatStep, ResultStatus.OK, outInfo);
		} catch (InterruptedException ex) {
		    return new AndroidResult(cvcStep, ResultStatus.INTERRUPTED, Collections.emptyList());
		}
	    });
	} else if (PINStep.STEP_ID.equals(curStep.getID())) {
	    idx++;
	    Step pinStep = curStep;

	    if (pinFirstUse) {
		pinFirstUse = false;
	    } else {
		this.guiService.setPinCorrect(false);
	    }

	    return displayAndExecuteBackground(pinStep, () -> {
		// ask user for the pin
		try {
		    List<OutputInfoUnit> outInfo = this.guiService.getPinResult(pinStep);
		    writeBackValues(pinStep.getInputInfoUnits(), outInfo);
		    return new AndroidResult(pinStep, ResultStatus.OK, outInfo);
		} catch (InterruptedException ex) {
		    return new AndroidResult(pinStep, ResultStatus.CANCEL, Collections.emptyList());
		}
	    });
	} else if (ProcessingStep.STEP_ID.equals(curStep.getID())) {
	    idx++;

	    return displayAndExecuteBackground(curStep, () -> {
		LOG.debug("Delivering final PIN status in ProcessingStep.");
		// notify user that PIN entry was successful
		this.finalPinStatusDelivered = true;
		this.guiService.setPinCorrect(true);
		return new AndroidResult(curStep, ResultStatus.OK, Collections.emptyList());
	    });
	} else if (ErrorStep.STEP_ID.equals(curStep.getID())) {
	    idx++;

	    return displayAndExecuteBackground(curStep, () -> {
		if (! finalPinStatusDelivered) {
		    LOG.debug("Delivering final PIN status in ErrorStep.");
		    finalPinStatusDelivered = true;
		    // get blocked status from dynamic context
		    DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
		    EacPinStatus blockedStatus = (EacPinStatus) ctx.get(EACProtocol.PIN_STATUS);
		    LOG.debug("Final PIN status is {}.", blockedStatus);
		    if (blockedStatus == EacPinStatus.BLOCKED || blockedStatus == EacPinStatus.DEACTIVATED) {
			this.guiService.setPinCorrect(false);
			this.guiService.sendPinStatus(blockedStatus);
		    }
		}
		// errors always end in cancel
		return new AndroidResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
	    });
	} else {
	    idx++;
	    return new AndroidResult(curStep, ResultStatus.CANCEL, Collections.emptyList());
	}
    }

    @Override
    public StepResult previous() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replaceCurrent(Step step) {
	steps.set(idx - 1, step);
	return current();
    }

    @Override
    public StepResult replaceNext(Step step) {
	steps.set(idx, step);
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
	ifaceReceiver.terminate();
    }

    @Override
    protected StepResult displayAndExecuteBackground(Step stepObj, Callable<StepResult> step) {
	StepResult r = super.displayAndExecuteBackground(stepObj, step);
	switch (r.getStatus()) {
	    case CANCEL:
	    case INTERRUPTED:
		guiService.cancel();
		break;
	}
	return r;
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
