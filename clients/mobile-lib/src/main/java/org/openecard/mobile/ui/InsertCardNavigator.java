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

package org.openecard.mobile.ui;

import java.util.Collections;
import java.util.concurrent.Future;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.StepResult;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.mobile.GuiIfaceReceiver;
import org.openecard.gui.mobile.MobileResult;


/**
 *
 * @author Tobias Wich
 */
public class InsertCardNavigator implements UserConsentNavigator {

    private final UserConsentDescription uc;
    private final GuiIfaceReceiver<Object> ifaceReceiver;

    private int idx = -1;

    public InsertCardNavigator(UserConsentDescription uc, GuiIfaceReceiver<Object> ifaceReceiver) {
	this.uc = uc;
	this.ifaceReceiver = ifaceReceiver;
    }


    @Override
    public boolean hasNext() {
	return idx < (uc.getSteps().size() - 1);
    }

    @Override
    public StepResult current() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult next() {
	idx++;
	Step s = uc.getSteps().get(idx);
	return new MobileResult(s, ResultStatus.OK, Collections.EMPTY_LIST);
    }

    @Override
    public StepResult previous() {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replaceCurrent(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replaceNext(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StepResult replacePrevious(Step step) {
	throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setRunningAction(Future<?> action) {
	// not interesting
    }

    @Override
    public void close() {
	// nothing to do as there is no UI
	ifaceReceiver.terminate();
    }

}
