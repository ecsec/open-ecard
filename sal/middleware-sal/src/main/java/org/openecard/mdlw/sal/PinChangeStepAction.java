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

import java.util.Arrays;
import java.util.Map;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.InputInfoUnit;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.openecard.mdlw.sal.enums.UserType;
import org.openecard.mdlw.sal.exceptions.AuthenticationException;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.openecard.mdlw.sal.exceptions.PinBlockedException;
import org.openecard.mdlw.sal.exceptions.PinIncorrectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class PinChangeStepAction extends StepAction {

    private static final Logger LOG = LoggerFactory.getLogger(PinChangeStepAction.class);
    private final PinChangeStep pinStep;

    public PinChangeStepAction(PinChangeStep pinStep) {
	super(pinStep);
	this.pinStep = pinStep;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	try {
	    if (pinStep.isCapturePuk()) {
		if (pinStep.isProtectedAuthPath()) {
		    pinStep.getSession().loginExternal(UserType.Security_Officer);
		    pinStep.getSession().initPinExternal();
		} else {
		    char[] puk = getPuk();
		    pinStep.getSession().login(UserType.Security_Officer, puk);
		    char[] newPin = getNewPin();
		    pinStep.getSession().initPin(newPin);
		}
	    } else {
		if (pinStep.isProtectedAuthPath()) {
		    //pinStep.getSession().loginExternal(UserType.User);
		    pinStep.getSession().changePinExternal();
		} else {
		    char[] oldPin = getOldPin();
		    char[] newPin = getNewPin();
		    pinStep.getSession().changePin(oldPin, newPin);
		}
	    }
	    pinStep.setPinChangeSuccessful();
	    pinStep.updateState();
	    return new StepActionResult(StepActionResultStatus.REPEAT);
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
	} catch (PinsDoNotMatchException ex) {
	    LOG.debug("Mismatching PINs entered.", ex);
	    try {
		pinStep.setPinsDoNotMatch();
		pinStep.updateState();
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } catch (CryptokiException ex2) {
		// I suspect user removed card
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} catch (PinBlockedException ex) {
	    // let the UI take care of producing a blocked error
	    try  {
		pinStep.updateState();
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } catch (CryptokiException ex2) {
		// I suspect user removed card
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} catch (AuthenticationException ex) {
	    LOG.error("Authentication error while entering the PIN.", ex);
	    try  {
		pinStep.setUnkownError();
		pinStep.updateState();
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } catch (CryptokiException ex2) {
		// I suspect user removed card
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	} catch (CryptokiException ex) {
	    LOG.error("Unknown error while entering the PIN.", ex);
	    try {
		pinStep.setUnkownError();
		pinStep.updateState();
		return new StepActionResult(StepActionResultStatus.REPEAT);
	    } catch (CryptokiException ex2) {
		// I suspect user removed card
		return new StepActionResult(StepActionResultStatus.CANCEL);
	    }
	}
    }

    private char[] getOldPin() {
	for (InputInfoUnit info : pinStep.getInputInfoUnits()) {
	    if (PinChangeStep.OLD_PIN_FIELD.equals(info.getID())) {
		return ((PasswordField) info).getValue();
	    }
	}

	return null;
    }

    private char[] getNewPin() throws PinsDoNotMatchException {
	char[] pin1 = null;
	char[] pin2 = null;
	for (InputInfoUnit info : pinStep.getInputInfoUnits()) {
	    if (PinChangeStep.NEW_PIN_FIELD1.equals(info.getID())) {
		pin1 = ((PasswordField) info).getValue();
	    } else if (PinChangeStep.NEW_PIN_FIELD2.equals(info.getID())) {
		pin2 = ((PasswordField) info).getValue();
	    }
	}

	if (pin1 != null && Arrays.equals(pin1, pin2)) {
	    return pin1;
	} else {
	    throw new PinsDoNotMatchException("The PINs entered in the UI do not match.");
	}
    }

    private char[] getPuk() {
	for (InputInfoUnit info : pinStep.getInputInfoUnits()) {
	    if (PinChangeStep.PUK_FIELD.equals(info.getID())) {
		return ((PasswordField) info).getValue();
	    }
	}

	return null;
    }

}
