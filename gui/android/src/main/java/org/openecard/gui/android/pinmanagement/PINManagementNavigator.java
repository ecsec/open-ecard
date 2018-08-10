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
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.android.AndroidResult;
import org.openecard.gui.android.GuiIfaceReceiver;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;

/**
 *
 * @author Sebastian Schuberth
 */
public class PINManagementNavigator implements UserConsentNavigator {

    private final List<Step> steps;
    private final GuiIfaceReceiver<PINManagementGuiImpl> ifaceReceiver;
    private final PINManagementGuiImpl guiService;

    private int idx = -1;
    private boolean pinFirstUse = true;


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
	if (this.guiService.isCancelled()) {
	    // prevent index out of bounds
	    int i = idx == -1 ? 0 : idx > steps.size() ? steps.size() - 1 : idx;
	    return new AndroidResult(steps.get(i), ResultStatus.CANCEL, Collections.EMPTY_LIST);
	}

	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StepResult previous() {
	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

}
