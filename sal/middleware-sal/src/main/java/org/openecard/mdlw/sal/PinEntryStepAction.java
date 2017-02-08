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

import java.util.Map;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.mdlw.sal.cryptoki.CryptokiLibrary;
import org.openecard.mdlw.sal.enums.UserType;
import org.openecard.mdlw.sal.exceptions.AuthenticationException;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.PinIncorrectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class PinEntryStepAction extends StepAction {

    private static final Logger LOG = LoggerFactory.getLogger(PinEntryStepAction.class);
    private final PinEntryStep pinStep;

    public PinEntryStepAction(PinEntryStep pinStep) {
	super(pinStep);
	this.pinStep = pinStep;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	try {
	    if (pinStep.isProtectedAuthPath()) {
		pinStep.getSession().loginExternal(UserType.User);
	    } else {
		char[] pPin = getPin();
		pinStep.getSession().login(UserType.User, pPin);
	    }
	    pinStep.setPinAuthenticated();
	    return new StepActionResult(StepActionResultStatus.NEXT);
	} catch (PinIncorrectException ex) {
	    if (LOG.isDebugEnabled()) {
		LOG.debug("PIN incorrect.", ex);
	    } else {
		LOG.info("PIN incorrect.");
	    }
	    pinStep.setLastTryFailed();
	    try {
		pinStep.updateState();
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } catch (CryptokiException ex1) {
		// I suspect user removed card
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} catch (AuthenticationException ex) {
	    LOG.error("Authentication error while entering the PIN.", ex);
	    pinStep.setLastTryFailed();
	    long code = ex.getErrorCode();
	    if (code == CryptokiLibrary.CKR_PIN_LOCKED || code == CryptokiLibrary.CKR_PIN_EXPIRED) {
		pinStep.setPinBlocked();
	    } else {
		pinStep.setUnkownError();
	    }
	    try {
		pinStep.updateState();
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } catch (CryptokiException ex1) {
		// I suspect user removed card
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} catch (CryptokiException ex) {
	    LOG.error("Unkonw error while entering the PIN.", ex);
	    pinStep.setLastTryFailed();
	    pinStep.setUnkownError();
	    try {
		pinStep.updateState();
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } catch (CryptokiException ex1) {
		// I suspect user removed card
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	}
    }

    private char[] getPin() {
	for (InputInfoUnit info : pinStep.getInputInfoUnits()) {
	    if (PinEntryStep.PIN_FIELD.equals(info.getID())) {
		return ((PasswordField) info).getValue();
	    }
	}

	return null;
    }

}
