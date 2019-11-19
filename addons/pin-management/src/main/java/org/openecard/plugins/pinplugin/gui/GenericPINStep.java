/****************************************************************************
 * Copyright (C) 2014-2018 ecsec GmbH.
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

import org.openecard.common.DynamicContext;
import org.openecard.common.I18n;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.CAN_CORRECT;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.DYNCTX_INSTANCE_KEY;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.PIN_CORRECT;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.PIN_STATUS;
import static org.openecard.plugins.pinplugin.GetCardsAndPINStatusAction.PUK_CORRECT;
import org.openecard.plugins.pinplugin.RecognizedState;


/**
 *
 * @author Hans-Martin Haase
 * @author Tobias Wich
 */
public class GenericPINStep extends Step {

    private final boolean capturePin;
    private final I18n lang = I18n.getTranslation("pinplugin");

    // translation constants PIN Change
    private static final String CHANGE_PIN_TITLE = "action.changepin.userconsent.pinstep.title";
    private static final String PINSTEP_NEWPINREPEAT = "action.changepin.userconsent.pinstep.newpinrepeat";
    private static final String PINSTEP_NEWPIN = "action.changepin.userconsent.pinstep.newpin";
    private static final String PINSTEP_OLDPIN = "action.changepin.userconsent.pinstep.oldpin";
    private static final String PINSTEP_DESCRIPTION = "action.changepin.userconsent.pinstep.description";
    private static final String PINSTEP_DESCRIPTION_AFTER_CAN = "action.changepin.userconsent.pinstep.description_after_can";
    private static final String REMAINING_ATTEMPTS = "action.changepin.userconsent.pinstep.remaining_attempts";
    private static final String WRONG_ENTRY = "action.changepin.userconsent.pinstep.wrong_entry";
    private static final String INCORRECT_INPUT = "action.changepin.userconsent.pinstep.incorrect_input";
    private static final String PINSTEP_NATIV_CHANGE_DESCRIPTION = "action.changepin.userconsent.pinstep.native_start_description";
    private static final String PINSTEP_NATIV_CHANGE_DESCRIPTION_AFTER_CAN = "action.changepin.userconsent.pinstep.native_start_description_after_can";

    // translation constants PUK entring
    private static final String PUKSTEP_DESCRIPTION = "action.unblockpin.userconsent.pukstep.description";
    private static final String PUKSTEP_TITLE = "action.unblockpin.userconsent.pukstep.title";
    private static final String PUKSTEP_PUK = "action.unblockpin.userconsent.pukstep.puk";
    private static final String PUKSTEP_START_NATIV_DESCRIPTION = "action.unblockpin.userconsent.pukstep.nativ_start_description";

    // translation constants CAN entering
    private static final String CANSTEP_TITLE = "action.changepin.userconsent.canstep.title";
    private static final String CANSTEP_NOTICE = "action.changepin.userconsent.canstep.notice";
    private static final String CANSTEP_CAN = "action.changepin.userconsent.canstep.can";
    private static final String CANSTEP_DESCRIPTION = "action.changepin.userconsent.canstep.description";
    private static final String WRONG_CAN = "action.changepin.userconsent.canstepaction.wrong_can";
    private static final String CANSTEP_START_NATIV_DESCRIPTION = "action.changepin.userconsent.canstepaction.nativ_start_description";

    private static final String ERROR_TITLE = "action.changepin.userconsent.errorstep.title";
    private static final String ERRORSTEP_DEACTIVATED = "action.changepin.userconsent.errorstep.deactivated";
    private static final String ERRORSTEP_PUK_BLOCKED = "action.changepin.userconsent.errorstep.puk_blocked";
    private static final String ERRORSTEP_UNKNOWN = "action.changepin.userconsent.errorstep.unknown";

    // protected GUI element IDs
    public static final String OLD_PIN_FIELD = "OLD_PIN_FIELD";
    public static final String NEW_PIN_FIELD = "NEW_PIN_FIELD";
    public static final String NEW_PIN_REPEAT_FIELD = "NEW_PIN_REPEAT_FIELD";
    public static final String PUK_FIELD = "PUK_FIELD";
    public static final String CAN_FIELD = "CAN_FIELD";

    // indicators set by the action
    private boolean wrongPINFormat;
    private boolean failedPINVerify;
    private boolean wrongCANFormat;
    private boolean failedCANVerify;
    private boolean wrongPUKFormat;
    private boolean failedPUKVerify;
    private boolean canSuccess;

    private int retryCounterPIN;
    private int retryCounterPUK = 10;

    private RecognizedState pinState;


    public GenericPINStep(String id, String title, boolean capturePin, RecognizedState state) {
	super(id, title);
	this.capturePin = capturePin;
	pinState = state;
	generateGenericGui();
    }

    public RecognizedState getPinState() {
	return pinState;
    }

    private void generateGenericGui() {
	switch(pinState) {
	    case PIN_activated_RC3:
		setTitle(lang.translationForKey(CHANGE_PIN_TITLE));
		retryCounterPIN = 3;
		if (capturePin) {
		    createPINChangeGui();
		} else {
		    createPINChangeGuiNativ();
		}
		break;
	    case PIN_activated_RC2:
		setTitle(lang.translationForKey(CHANGE_PIN_TITLE));
		retryCounterPIN = 2;
		if (capturePin) {
		    createPINChangeGui();
		} else {
		    createPINChangeGuiNativ();
		}
		break;
	    case PIN_blocked:
		setTitle(lang.translationForKey(PUKSTEP_TITLE));
		retryCounterPIN = 0;
		if (capturePin) {
		    createPUKGui();
		} else {
		    createPUKGuiNativ();
		}
		break;
	    case PIN_suspended:
		setTitle(lang.translationForKey(CANSTEP_TITLE));
		retryCounterPIN = 1;
		if (capturePin) {
		    createCANGui();
		} else {
		    createCANGuiNativ();
		}
		break;
	    case PIN_resumed:
		setTitle(lang.translationForKey(CHANGE_PIN_TITLE));
		retryCounterPIN = 1;
		canSuccess = true;
		if (capturePin) {
		    createPINChangeGui();
		} else {
		    createPINChangeGuiNativ();
		}
		break;
	    case PIN_deactivated:
		setTitle(lang.translationForKey(ERROR_TITLE));
		retryCounterPIN = -1;
		createErrorGui();
		break;
	    case UNKNOWN:
		setTitle(lang.translationForKey(ERROR_TITLE));
		retryCounterPIN = -2;
		createErrorGui();
		break;
	    case PUK_blocked:
		setTitle(lang.translationForKey(ERROR_TITLE));
		createErrorGui();
		retryCounterPUK = 0;
	}
    }

    protected void updateState(RecognizedState newState) {
	pinState = newState;
	getInputInfoUnits().clear();
	generateGenericGui();

	// update state
	DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);
	ctx.put(PIN_STATUS, newState);
    }

    private void createPINChangeGuiNativ() {

	String nativPinChangeDescriptionText;

	if (canSuccess) {
	    nativPinChangeDescriptionText = lang.translationForKey(PINSTEP_NATIV_CHANGE_DESCRIPTION_AFTER_CAN);
	} else {
	    nativPinChangeDescriptionText = lang.translationForKey(PINSTEP_NATIV_CHANGE_DESCRIPTION);
	}

	Text nativPinChangeDescription = new Text(nativPinChangeDescriptionText);
	getInputInfoUnits().add(nativPinChangeDescription);

	if (failedPINVerify || canSuccess) {
	    addVerifyFailed("PIN");
	}

	addRemainingAttempts();
    }

    private void createPINChangeGui() {

	String pinChangeDescriptionText;

	if (canSuccess) {
	    pinChangeDescriptionText = lang.translationForKey(PINSTEP_DESCRIPTION_AFTER_CAN, "PIN");
	} else {
	    pinChangeDescriptionText = lang.translationForKey(PINSTEP_DESCRIPTION, "PIN");
	}

	Text pinChangeDescription = new Text(pinChangeDescriptionText);
	getInputInfoUnits().add(pinChangeDescription);

	Text dummy = new Text(" ");
	getInputInfoUnits().add(dummy);

	Text pinText = new Text(lang.translationForKey(PINSTEP_OLDPIN));
	getInputInfoUnits().add(pinText);

	PasswordField oldPIN = new PasswordField(OLD_PIN_FIELD);
	oldPIN.setMinLength(5); // in case of transport pin
	oldPIN.setMaxLength(6);
	getInputInfoUnits().add(oldPIN);

	Text newPinText = new Text(lang.translationForKey(PINSTEP_NEWPIN));
	getInputInfoUnits().add(newPinText);

	PasswordField newPIN = new PasswordField(NEW_PIN_FIELD);
	newPIN.setMaxLength(6);
	newPIN.setMinLength(6);
	getInputInfoUnits().add(newPIN);

	Text newPinAgainText = new Text(lang.translationForKey(PINSTEP_NEWPINREPEAT));
	getInputInfoUnits().add(newPinAgainText);

	PasswordField newPINRepeat = new PasswordField(NEW_PIN_REPEAT_FIELD);
	newPINRepeat.setMaxLength(6);
	newPINRepeat.setMinLength(6);
	getInputInfoUnits().add(newPINRepeat);

	if (wrongPINFormat) {
	    // add note for mistyped PIN
	    Text noteWrongEntry = new Text();
	    noteWrongEntry.setText(lang.translationForKey(WRONG_ENTRY, "PIN"));
	    getInputInfoUnits().add(noteWrongEntry);
	}

	if (failedPINVerify) {
	    // add note for incorrect input
	    addVerifyFailed("PIN");
	}

	addRemainingAttempts();
    }

    private void createPUKGuiNativ() {
	Text nativPUKText = new Text(lang.translationForKey(PUKSTEP_START_NATIV_DESCRIPTION));
	getInputInfoUnits().add(nativPUKText);

	// show the puk try counter
//	Text pukTryCounter = new Text();
//	pukTryCounter.setText(lang.translationForKey(REMAINING_ATTEMPTS, retryCounterPUK));
//	getInputInfoUnits().add(pukTryCounter);

	if (failedPUKVerify) {
	    addVerifyFailed("PUK");
	}
    }

    private void createPUKGui() {
	Text i1 = new Text();
	getInputInfoUnits().add(i1);

	i1.setText(lang.translationForKey(PUKSTEP_DESCRIPTION));
	PasswordField pukField = new PasswordField(PUK_FIELD);
	pukField.setMaxLength(10);
	pukField.setMinLength(10);
	pukField.setDescription(lang.translationForKey(PUKSTEP_PUK));
	getInputInfoUnits().add(pukField);

	// show the puk try counter
//	Text pukTryCounter = new Text();
//	pukTryCounter.setText(lang.translationForKey(REMAINING_ATTEMPTS, retryCounterPUK));
//	getInputInfoUnits().add(pukTryCounter);

	if (wrongPUKFormat) {
	    // add note for mistyped PUK
	    Text noteWrongEntry = new Text();
	    noteWrongEntry.setText(lang.translationForKey(WRONG_ENTRY, "PUK"));
	    getInputInfoUnits().add(noteWrongEntry);
	}

	if (failedPUKVerify) {
	    // add note for incorrect input
	    addVerifyFailed("PUK");
	}
    }

    private void createCANGuiNativ() {
	Text nativCANText = new Text(lang.translationForKey(CANSTEP_START_NATIV_DESCRIPTION));
	getInputInfoUnits().add(nativCANText);

	if (failedCANVerify) {
	    addVerifyFailed("CAN");
	}
    }

    private void createCANGui() {
	Text i1 = new Text();
	i1.setText(lang.translationForKey(CANSTEP_NOTICE));
	getInputInfoUnits().add(i1);
	Text i2 = new Text();
	getInputInfoUnits().add(i2);

	// add description and input fields depending on terminal type
	i2.setText(lang.translationForKey(CANSTEP_DESCRIPTION));
	PasswordField canField = new PasswordField(CAN_FIELD);
	canField.setMinLength(6);
	canField.setMaxLength(6);
	canField.setDescription(lang.translationForKey(CANSTEP_CAN));
	getInputInfoUnits().add(canField);

	if (wrongCANFormat) {
	    // add note for mistyped CAN
	    Text retryText = new Text();
	    retryText.setText(lang.translationForKey(WRONG_CAN));
	    getInputInfoUnits().add(retryText);
	}

	if (failedCANVerify) {
	    // add note for incorrect input
	    addVerifyFailed("CAN");
	}
    }

    private void createErrorGui() {
	setID("error");
	setReversible(false);
	Text errorText = new Text();
	switch (pinState) {
	    case PIN_deactivated:
		errorText.setText(lang.translationForKey(ERRORSTEP_DEACTIVATED));
		break;
	    case PUK_blocked:
		errorText.setText(lang.translationForKey(ERRORSTEP_PUK_BLOCKED));
		break;
	    case UNKNOWN:
		errorText.setText(lang.translationForKey(ERRORSTEP_UNKNOWN));
		break;
	}
	getInputInfoUnits().add(errorText);
    }

    protected void setFailedPINVerify(boolean wrongFormat, boolean failedVerify) {
	wrongPINFormat = wrongFormat;
	failedPINVerify = failedVerify;
	exportPinCorrect(! wrongFormat && ! failedVerify);
    }

    protected void setFailedCANVerify(boolean wrongFormat, boolean failedVerify) {
	wrongCANFormat = wrongFormat;
	failedCANVerify = failedVerify;
	exportCanCorrect(! wrongFormat && ! failedVerify);
    }

    protected void setFailedPUKVerify(boolean wrongFormat, boolean failedVerify) {
	wrongPUKFormat = wrongFormat;
	failedPUKVerify = failedVerify;
	exportPukCorrect(! wrongFormat && ! failedVerify);
    }

    protected void exportPinCorrect(boolean isCorrect) {
	DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);
	ctx.put(PIN_CORRECT, isCorrect);
    }

    private void exportCanCorrect(boolean isCorrect) {
	DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);
	ctx.put(CAN_CORRECT, isCorrect);
    }

    private void exportPukCorrect(boolean isCorrect) {
	DynamicContext ctx = DynamicContext.getInstance(DYNCTX_INSTANCE_KEY);
	ctx.put(PUK_CORRECT, isCorrect);
    }

    protected void decreasePUKCounter() {
	retryCounterPUK--;
	getInputInfoUnits().clear();
	generateGenericGui();
    }

    private void addRemainingAttempts() {
	Text txtRemainingAttempts = new Text();
	txtRemainingAttempts.setText(lang.translationForKey(REMAINING_ATTEMPTS, retryCounterPIN));
	getInputInfoUnits().add(txtRemainingAttempts);
    }

    private void addVerifyFailed(String did) {
	Text incorrectInput = new Text();
	incorrectInput.setText(lang.translationForKey(INCORRECT_INPUT, did));
	getInputInfoUnits().add(incorrectInput);
    }

}
