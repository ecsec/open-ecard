/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
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

import iso.std.iso_iec._24727.tech.schema.ConnectionHandleType;
import java.util.ArrayList;
import java.util.List;
import org.openecard.common.I18n;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepAction;
import org.openecard.plugins.pinplugin.RecognizedState;


/**
 * Implements a dialog for changing the PIN.
 * <br/> This dialog guides the user through the process needed for changing the PIN.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ChangePINDialog {

    private final I18n lang = I18n.getTranslation("pinplugin");

    // translation constants
    private static final String ERRORSTEP_UNKNOWN = "action.changepin.userconsent.errorstep.unknown";
    private static final String ERRORSTEP_DEACTIVATED = "action.changepin.userconsent.errorstep.deactivated";
    private static final String ERRORSTEP_BLOCKED = "action.changepin.userconsent.errorstep.blocked";
    private static final String SUCCESSSTEP_DESCRIPTION = "action.changepin.userconsent.successstep.description";
    private static final String CANSTEP_TITLE = "action.changepin.userconsent.canstep.title";
    private static final String PINSTEP_TITLE = "action.changepin.userconsent.pinstep.title";
    private static final String ERRORSTEP_TITLE = "action.changepin.userconsent.errorstep.title";
    private static final String SUCCESSSTEP_TITLE = "action.changepin.userconsent.successstep.title";
    private static final String TITLE = "action.changepin.userconsent.title";

    private final UserConsent gui;
    private final ConnectionHandleType conHandle;
    private RecognizedState state;
    private boolean capturePin;
    private Dispatcher dispatcher;

    /**
     * Creates a new instance of ChangePINDialog.
     * 
     * @param gui The UserConsent to show on
     * @param capturePin True if the PIN has to be captured by software else false
     * @param conHandle to get the requested card type from
     * @param dispatcher The Dispatcher to use
     * @param state The State of the PIN
     */
    public ChangePINDialog(UserConsent gui, Dispatcher dispatcher, ConnectionHandleType conHandle,
	    RecognizedState state, boolean capturePin) {
	this.gui = gui;
	this.conHandle = conHandle;
	this.state = state;
	this.capturePin = capturePin;
	this.dispatcher = dispatcher;
    }

    private UserConsentDescription createUserConsentDescription() {
	UserConsentDescription uc = new UserConsentDescription(lang.translationForKey(TITLE));

	uc.getSteps().addAll(createSteps());

	return uc;
    }

    /**
     * Create the list of steps depending on the state of the pin.
     * 
     * @return list of steps for the Dialog
     */
    private List<Step> createSteps() {
	List<Step> steps = new ArrayList<Step>();

	if (state.equals(RecognizedState.PIN_blocked) || state.equals(RecognizedState.PIN_deactivated)
		|| state.equals(RecognizedState.UNKNOWN)) {
	    Step errorStep = createErrorStep();
	    steps.add(errorStep);
	    return steps;
	}

	Step canStep = createCANStep();
	steps.add(canStep);

	Step changePINStep = createChangePINStep();
	steps.add(changePINStep);

	Step successStep = createSuccessStep();
	steps.add(successStep);
	return steps;
    }

    /**
     * Create the step that informs the user that everything went fine.
     *
     * @return Step showing success message
     */
    private Step createSuccessStep() {
	Step successStep = new Step("success", lang.translationForKey(SUCCESSSTEP_TITLE));
	successStep.setReversible(false);
	Text i1 = new Text();
	i1.setText(lang.translationForKey(SUCCESSSTEP_DESCRIPTION));
	successStep.getInputInfoUnits().add(i1);

	return successStep;
    }

    /**
     * Create the step that informs the user that something went wrong.
     * 
     * @return Step with error description
     */
    private Step createErrorStep() {
	Step errorStep = new Step("error", lang.translationForKey(ERRORSTEP_TITLE));
	errorStep.setReversible(false);
	Text i1 = new Text();
	switch (state) {
	    case PIN_blocked:
		i1.setText(lang.translationForKey(ERRORSTEP_BLOCKED));
		break;
	    case PIN_deactivated:
		i1.setText(lang.translationForKey(ERRORSTEP_DEACTIVATED));
		break;
	    default:
		i1.setText(lang.translationForKey(ERRORSTEP_UNKNOWN));
	}

	errorStep.getInputInfoUnits().add(i1);
	return errorStep;
    }

    /**
     * Create the step that asks the user to insert the old and new pins.
     * 
     * @return Step for PIN entry
     */
    private Step createChangePINStep() {
	int retryCounter = getRetryCounterFromState(state);
	String title = lang.translationForKey(PINSTEP_TITLE);
	Step changePINStep = new ChangePINStep("pin-entry", title, capturePin, retryCounter, false, false);
	StepAction pinAction = new PINStepAction(capturePin, conHandle, dispatcher, changePINStep, retryCounter);
	changePINStep.setAction(pinAction);
	return changePINStep;
    }

    /**
     * Create the step that asks the user to insert the CAN.
     * 
     * @return Step for CAN entry
     */
    private Step createCANStep() {
	String title = lang.translationForKey(CANSTEP_TITLE);
	CANEntryStep canStep = new CANEntryStep("can-entry", title , capturePin, state, false, false);
	StepAction pinAction = new CANStepAction(capturePin, conHandle, dispatcher, canStep, state);
	canStep.setAction(pinAction);
	return canStep;
    }

    /**
     * Shows this Dialog.
     */
    public void show() {
	UserConsentNavigator ucr = gui.obtainNavigator(createUserConsentDescription());
	ExecutionEngine exec = new ExecutionEngine(ucr);
	exec.process();
    }

    /**
     * Converts the state to the corresponding retry counter.
     * 
     * @param state The current state of the PIN.
     * @return The corresponding retry counter.
     */
    private int getRetryCounterFromState(RecognizedState state) {
	if (state.equals(RecognizedState.PIN_activated_RC3)) {
	    return 3;
	} else if (state.equals(RecognizedState.PIN_activated_RC2)) {
	    return 2;
	} else if (state.equals(RecognizedState.PIN_suspended)) {
	    return 1;
	} else {
	    return 0;
	}
    }

}
