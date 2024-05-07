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

import javax.annotation.Nonnull;
import org.openecard.common.I18n;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.mdlw.sal.enums.PinState;
import org.openecard.mdlw.sal.exceptions.CryptokiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich
 */
public class PinChangeStep extends Step {

    private static final Logger LOG = LoggerFactory.getLogger(PinChangeStep.class);
    private static final I18n LANG = I18n.getTranslation("pinplugin");

    public static final String STEP_ID = "sal.middleware.pin-compare.gui-change.pin-step.id";

    protected static final String PUK_FIELD      = "PUK_FIELD";
    protected static final String OLD_PIN_FIELD  = "OLD_PIN_FIELD";
    protected static final String NEW_PIN_FIELD1 = "NEW_PIN_FIELD1";
    protected static final String NEW_PIN_FIELD2 = "NEW_PIN_FIELD2";

    private final PasswordField oldPin;
    private final PasswordField newPin1;
    private final PasswordField newPin2;
    private final PasswordField puk;

    private final boolean protectedAuthPath;
    private final MwSession session;
    private PinState pinState;
    private boolean pinsDoNotMatch = false;
    private boolean lastTryFailed = false;
    private boolean lastTryPukFailed = false;
    private boolean capturePuk = false;
    private boolean unkownError = false;
    private boolean pinChangeSuccessful = false;

    public PinChangeStep(boolean protectedAuthPath, @Nonnull MwSession session) throws CryptokiException {
	super(STEP_ID);

	this.protectedAuthPath = protectedAuthPath;
	this.session = session;

	oldPin = new PasswordField(OLD_PIN_FIELD);
	oldPin.setDescription(LANG.translationForKey("action.changepin.userconsent.pinstep.oldpin"));
	// TODO: set length restrictions based on DID description
	oldPin.setMinLength(4);

	newPin1 = new PasswordField(NEW_PIN_FIELD1);
	newPin1.setDescription(LANG.translationForKey("action.changepin.userconsent.pinstep.newpin"));
	// TODO: set length restrictions based on DID description
	newPin1.setMinLength(4);

	newPin2 = new PasswordField(NEW_PIN_FIELD2);
	newPin2.setDescription(LANG.translationForKey("action.changepin.userconsent.pinstep.newpinrepeat"));
	// TODO: set length restrictions based on DID description
	newPin2.setMinLength(4);

	puk = new PasswordField(PUK_FIELD);
	puk.setDescription(LANG.translationForKey("action.unblockpin.userconsent.pukstep.puk"));
	// TODO: set length restrictions based on DID description
	puk.setMinLength(4);

	updateState();
    }

    protected MwSession getSession() {
	return session;
    }

    boolean isProtectedAuthPath() {
	return protectedAuthPath;
    }

    boolean isCapturePuk() {
	return capturePuk;
    }

    public void setPinsDoNotMatch() {
	this.pinsDoNotMatch = true;
    }

    protected void setLastTryFailed() {
	this.lastTryFailed = true;
    }

    protected void setLastTryPukFailed() {
	this.lastTryPukFailed = true;
    }

    void setUnkownError() {
	this.unkownError = true;
    }

    public void setPinChangeSuccessful() {
	this.pinChangeSuccessful = true;
    }

    private void generateGui() {
	if (unkownError) {
	    setTitle(LANG.translationForKey("action.error.title"));
	    setAction(null);
	    createErrorGui();
	    return;
	}

	if (pinChangeSuccessful) {
	    setTitle(LANG.translationForKey("action.changepin.userconsent.successstep.title"));
	    setAction(null);
	    createSuccessGui();
	    return;
	}

//	if (lastTryFailed && pinState == PinState.PIN_LOCKED) {
//	    setTitle(LANG.translationForKey("action.error.title"));
//	    setAction(null);
//	    createErrorGui();
//	    return;
//	}

	switch (pinState) {
	    case PIN_OK:
	    case PIN_COUNT_LOW:
	    case PIN_FINAL_TRY:
	    case PIN_NEEDS_CHANGE:
		setTitle(LANG.translationForKey("action.changepin.userconsent.pinstep.title"));
		if (protectedAuthPath) {
		    createPinChangeNativeGui();
		} else {
		    createPinChangeGui();
		}
		break;
	    case PIN_LOCKED:
		setTitle(LANG.translationForKey("action.unblockpin.userconsent.pukstep.title"));
		capturePuk = true;
		if (protectedAuthPath) {
		    createPukChangeNativeGui();
		} else {
		    createPukChangeGui();
		}
		break;
	    case PIN_NOT_INITIALIZED:
		setTitle(LANG.translationForKey("action.error.title"));
		setAction(null);
		createErrorGui();
		break;
	    default:
		String msg = "Invalid pin state found.";
		LOG.error(msg);
		throw new IllegalStateException(msg);
	}
    }

    protected final void updateState() throws CryptokiException {
	capturePuk = false;
	PinState oldState = pinState;
	pinState = PinState.getUserPinState(session.getSlot().getTokenInfo());
	LOG.debug("PinState detection: {} -> {}", oldState, pinState);

	getInputInfoUnits().clear();
	if (pinsDoNotMatch) {
	    newPin1.setValue(null);
	    newPin2.setValue(null);
	} else {
	    oldPin.setValue(null);
	    newPin1.setValue(null);
	    newPin2.setValue(null);
	    puk.setValue(null);
	}

	generateGui();

	pinsDoNotMatch = false;
	lastTryFailed = false;
	lastTryPukFailed = false;
    }

    private void createPinChangeGui() {
	setInstantReturn(false);

	if (lastTryFailed) {
	    addVerifyFailed("PIN");
	} else {
	    String desc = LANG.translationForKey("action.changepin.userconsent.pinstep.description", "PIN");
	    Text descText = new Text(desc);
	    getInputInfoUnits().add(descText);
	}

	getInputInfoUnits().add(oldPin);
	getInputInfoUnits().add(newPin1);
	getInputInfoUnits().add(newPin2);

	if (pinsDoNotMatch) {
	    addPinsDoNotMatch();
	}
	if (pinState == PinState.PIN_COUNT_LOW) {
	    String pinLowStr = LANG.translationForKey("action.changepin.userconsent.pinstep.remaining_attempts", 2);
	    Text pinLowText = new Text(pinLowStr);
	    getInputInfoUnits().add(pinLowText);
	} else if (pinState == PinState.PIN_FINAL_TRY) {
	    String noteStr = LANG.translationForKey("action.pinentry.userconsent.pinstep.final_try_note");
	    Text noteText = new Text(noteStr);
	    getInputInfoUnits().add(noteText);
	}
    }

    private void createPinChangeNativeGui() {
	setInstantReturn(true);

	if (lastTryFailed) {
	    addVerifyFailed("PIN");
	} else {
	    String desc = LANG.translationForKey("action.changepin.userconsent.pinstep.description", "PIN");
	    Text descText = new Text(desc);
	    getInputInfoUnits().add(descText);
	}

	if (pinState == PinState.PIN_COUNT_LOW) {
	    String pinLowStr = LANG.translationForKey("action.changepin.userconsent.pinstep.remaining_attempts", 2);
	    Text pinLowText = new Text(pinLowStr);
	    getInputInfoUnits().add(pinLowText);
	} else if (pinState == PinState.PIN_FINAL_TRY) {
	    String noteStr = LANG.translationForKey("action.pinentry.userconsent.pinstep.final_try_note");
	    Text noteText = new Text(noteStr);
	    getInputInfoUnits().add(noteText);
	}
    }


    private void createPukChangeGui() {
	setInstantReturn(false);

	if (lastTryFailed) {
	    addVerifyFailed("PIN");
	} else if(lastTryPukFailed) {
	    addVerifyFailed("PUK");
	} else {
	    String desc = LANG.translationForKey("action.unblockpin.userconsent.pukstep.description");
	    Text descText = new Text(desc);
	    getInputInfoUnits().add(descText);
	}

	getInputInfoUnits().add(puk);
	getInputInfoUnits().add(newPin1);
	getInputInfoUnits().add(newPin2);

	if (pinsDoNotMatch) {
	    addPinsDoNotMatch();
	}
    }

    private void createPukChangeNativeGui() {
	setInstantReturn(true);

	if (lastTryFailed) {
	    addVerifyFailed("PIN");
	} else if(lastTryPukFailed) {
	    addVerifyFailed("PUK");
	} else {
	    String desc = LANG.translationForKey("action.unblockpin.userconsent.pukstep.native_description");
	    Text descText = new Text(desc);
	    getInputInfoUnits().add(descText);
	}
    }


    private void createErrorGui() {
	setInstantReturn(false);

	setReversible(false);
	if (lastTryFailed) {
	    addVerifyFailed("PIN");
	}

	String errorStr = LANG.translationForKey("action.error.internal");
	Text errorText = new Text(errorStr);
	getInputInfoUnits().add(errorText);
    }

    private void createSuccessGui() {
	setInstantReturn(false);

	setReversible(false);

	String successStr = LANG.translationForKey("action.changepin.userconsent.successstep.description");
	Text successText = new Text(successStr);
	getInputInfoUnits().add(successText);
    }

    private void addVerifyFailed(String secretName) {
	Text incorrectInput = new Text();
	incorrectInput.setText(LANG.translationForKey("action.changepin.userconsent.pinstep.incorrect_input", secretName));
	getInputInfoUnits().add(incorrectInput);
    }

    private void addPinsDoNotMatch() {
	Text incorrectInput = new Text();
	incorrectInput.setText(LANG.translationForKey("action.error.missing_password_match"));
	getInputInfoUnits().add(incorrectInput);
    }

}
