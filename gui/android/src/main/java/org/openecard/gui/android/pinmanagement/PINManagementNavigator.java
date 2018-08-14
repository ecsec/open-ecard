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

package org.openecard.gui.android.pinmanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
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
import org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction;
import org.openecard.plugins.pinplugin.RecognizedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Sebastian Schuberth
 * @author Tobias Wich
 */
public class PINManagementNavigator implements UserConsentNavigator {

    private static final Logger LOG = LoggerFactory.getLogger(PINManagementNavigator.class);

    private final List<Step> steps;
    private final GuiIfaceReceiver<PINManagementGuiImpl> ifaceReceiver;
    private final PINManagementGuiImpl guiService;

    private int idx = -1;


    public PINManagementNavigator(UserConsentDescription uc, GuiIfaceReceiver<PINManagementGuiImpl> ifaceReceiver) {
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
	idx++;
	Step pinStep = steps.get(0);

	DynamicContext ctx = DynamicContext.getInstance(GetCardsAndPINStatusAction.DYNCTX_INSTANCE_KEY);
	RecognizedState uiPinState = (RecognizedState) ctx.get(GetCardsAndPINStatusAction.PIN_STATUS);
	Boolean pinCorrect = (Boolean) ctx.get(GetCardsAndPINStatusAction.PIN_CORRECT);
	Boolean canCorrect = (Boolean) ctx.get(GetCardsAndPINStatusAction.CAN_CORRECT);
	Boolean pukCorrect = (Boolean) ctx.get(GetCardsAndPINStatusAction.PUK_CORRECT);

	if (uiPinState == null || uiPinState == RecognizedState.UNKNOWN) {
	    LOG.error("No pin state received from UI.");
	    return new AndroidResult(pinStep, ResultStatus.CANCEL, Collections.EMPTY_LIST);
	}

	// set pin state
	this.guiService.sendPinStatus(uiPinState);

	// set result values if any
	if (pinCorrect != null) {
	    this.guiService.setPinCorrect(pinCorrect);
	} else if (canCorrect != null) {
	    this.guiService.setCanCorrect(canCorrect);
	} else if (pukCorrect != null) {
	    this.guiService.setPukCorrect(pukCorrect);
	}

	// pin accepted or card blocked
	if ("success".equals(pinStep.getID())) {
	    return new AndroidResult(pinStep, ResultStatus.OK, Collections.EMPTY_LIST);
	} else if ("error".equals(pinStep.getID())) {
	    this.guiService.waitForUserCancel();
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
