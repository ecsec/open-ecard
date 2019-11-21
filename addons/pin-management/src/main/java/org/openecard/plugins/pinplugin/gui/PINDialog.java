/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.plugins.pinplugin.gui;

import org.openecard.common.ThreadTerminateException;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.plugins.pinplugin.CardCapturer;


/**
 *
 * @author Hans-Martin Haase
 */
public class PINDialog {

    private final UserConsent gui;
    private final Dispatcher dispatcher;
    private final CardCapturer cardCapturer;

    public PINDialog(UserConsent gui, Dispatcher dispatcher, CardCapturer cardCapturer) {
	this.gui = gui;
	this.dispatcher = dispatcher;
	this.cardCapturer = cardCapturer;
    }

    /**
     * Shows this Dialog.
     * @return
     */
    public ResultStatus show() {
	UserConsentNavigator ucr = gui.obtainNavigator(createUserConsentDescription());
	ExecutionEngine exec = new ExecutionEngine(ucr);
	try {
	    return exec.process();
	} catch (ThreadTerminateException ex) {
	    return ResultStatus.INTERRUPTED;
	}
    }

    private UserConsentDescription createUserConsentDescription() {
	UserConsentDescription uc = new UserConsentDescription("PIN Operation", "pin_change_dialog");
	GenericPINStep gPINStep = new GenericPINStep("GenericPINStepID", "GenericPINStep", this.cardCapturer);
	gPINStep.setAction(new GenericPINAction("PIN Management", dispatcher, gPINStep, this.cardCapturer));
	uc.getSteps().add(gPINStep);

	return uc;
    }

}
