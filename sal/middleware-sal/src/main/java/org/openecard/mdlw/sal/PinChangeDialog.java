/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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

package org.openecard.mdlw.sal;

import org.openecard.gui.ResultStatus;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.mdlw.sal.exceptions.CryptokiException;


/**
 *
 * @author Tobias Wich
 */
public class PinChangeDialog {

    private final UserConsent gui;
    private final boolean protectedAuthPath;
    private final MwSession session;
    private PinChangeStep pinStep;

    public PinChangeDialog(UserConsent gui, boolean protectedAuthPath, MwSession session) {
	this.gui = gui;
	this.protectedAuthPath = protectedAuthPath;
	this.session = session;
    }

    public ResultStatus show() throws CryptokiException {
	UserConsentNavigator ucr = gui.obtainNavigator(createUserConsentDescription());
	ExecutionEngine exec = new ExecutionEngine(ucr);
	ResultStatus result = exec.process();
	return result;
    }

    private UserConsentDescription createUserConsentDescription() throws CryptokiException {
	UserConsentDescription uc = new UserConsentDescription("PIN Management", "pin_change_dialog");
	pinStep = new PinChangeStep(protectedAuthPath, session);
	pinStep.setAction(new PinChangeStepAction(pinStep));
	uc.getSteps().add(pinStep);

	return uc;
    }

}
