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
import java.util.concurrent.Future;
import org.openecard.binding.tctoken.TR03112Keys;
import org.openecard.common.DynamicContext;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.android.AndroidResult;
import org.openecard.gui.android.GuiIfaceReceiver;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.OutputInfoUnit;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.sal.protocol.eac.EACProtocol;
import org.openecard.sal.protocol.eac.gui.EacPinStatus;


/**
 *
 * @author Tobias Wich
 */
public class EacNavigator implements UserConsentNavigator {

    private final List<Step> steps;
    private final GuiIfaceReceiver<EacGuiImpl> ifaceReceiver;
    private final EacGuiImpl guiService;

    private int idx = -1;
    private boolean pinFirstUse = true;


    public EacNavigator(UserConsentDescription uc, GuiIfaceReceiver<EacGuiImpl> ifaceReceiver) {
	this.steps = new ArrayList<>(uc.getSteps());
	this.ifaceReceiver = ifaceReceiver;
	this.guiService = ifaceReceiver.getUiInterface().derefNonblocking();
    }

    @Override
    public boolean hasNext() {
	return idx < (steps.size() - 1);
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
	    int i = idx == -1 ? 0 : idx > steps.size() ? steps.size() - 1 : idx;
	    return new AndroidResult(steps.get(i), ResultStatus.CANCEL, Collections.EMPTY_LIST);
	}

	// handle step display
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
		return new AndroidResult(chatStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
	    }
	} else if (idx == 1) {
	    idx++;
	    Step pinStep = steps.get(2);

	    if (pinFirstUse) {
		pinFirstUse = false;
	    } else {
		this.guiService.setPinCorrect(false);
	    }

	    // get blocked status from dynamic context
	    DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    EacPinStatus blockedStatus = (EacPinStatus) ctx.get(EACProtocol.PIN_STATUS);
	    if (blockedStatus == EacPinStatus.BLOCKED || blockedStatus == EacPinStatus.DEACTIVATED) {
		this.guiService.sendPinStatus(blockedStatus);
		return new AndroidResult(pinStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
	    }

	    // ask user for the pin
	    try {
		List<OutputInfoUnit> outInfo = this.guiService.getPinResult(pinStep);
		writeBackValues(pinStep.getInputInfoUnits(), outInfo);
		return new AndroidResult(pinStep, ResultStatus.OK, outInfo);
	    } catch (InterruptedException ex) {
		return new AndroidResult(pinStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
	    }
	} else if (idx == 2) {
	    idx++;
	    Step s = steps.get(idx);

	    // get blocked status from dynamic context
	    DynamicContext ctx = DynamicContext.getInstance(TR03112Keys.INSTANCE_KEY);
	    EacPinStatus blockedStatus = (EacPinStatus) ctx.get(EACProtocol.PIN_STATUS);
	    if (blockedStatus == EacPinStatus.BLOCKED || blockedStatus == EacPinStatus.DEACTIVATED) {
		this.guiService.setPinCorrect(false);
		this.guiService.sendPinStatus(blockedStatus);
		return new AndroidResult(s, ResultStatus.CANCEL, Collections.EMPTY_LIST);
	    }

	    if ("PROTOCOL_GUI_STEP_PROCESSING".equals(s.getID())) {
		this.guiService.setPinCorrect(true);
		return new AndroidResult(s, ResultStatus.OK, Collections.EMPTY_LIST);
	    } else {
		this.guiService.setPinCorrect(false);
		return new AndroidResult(s, ResultStatus.CANCEL, Collections.EMPTY_LIST);
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
	ifaceReceiver.terminate();
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
