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
import org.openecard.common.AppVersion;
import org.openecard.common.I18n;
import org.openecard.common.interfaces.Dispatcher;
import org.openecard.gui.UserConsent;
import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepAction;
import org.openecard.plugins.pinplugin.RecognizedState;


/**
 * Implements a dialog for unblocking the PIN.
 * This dialog guides the user through the process needed for unblocking the PIN.
 * 
 * @author Dirk Petrautzki
 */
public class UnblockPINDialog {

    private final I18n lang = I18n.getTranslation("pinplugin");

    private static final String TITLE = "action.unblockpin.userconsent.title";
    private static final String PUKSTEP_DESCRIPTION = "action.unblockpin.userconsent.pukstep.description";
    private static final String PUKSTEP_NATIVE_DESCRIPTION = "action.unblockpin.userconsent.pukstep.native_description";
    private static final String PUKSTEP_TITLE = "action.unblockpin.userconsent.pukstep.title";
    private static final String PUKSTEP_PUK = "action.unblockpin.userconsent.pukstep.puk";
    private static final String ERRORSTEP_TITLE = "action.unblockpin.userconsent.errorstep.title";
    private static final String ERRORSTEP_DESCRIPTION = "action.unblockpin.userconsent.errorstep.description";
    private static final String SUCCESSSTEP_TITLE = "action.unblockpin.userconsent.successstep.title";
    private static final String SUCCESSSTEP_DESCRIPTION = "action.unblockpin.userconsent.successstep.description";

    private final UserConsent gui;
    private final ConnectionHandleType conHandle;
    private RecognizedState state;
    private boolean capturePin;
    private Dispatcher dispatcher;

    // GUI element IDs
    public static final String PUK_FIELD = "PUK_FIELD";

    /**
     * Creates a new instance of UnblockPINUserConsent.
     * 
     * @param gui The UserConsent to show on
     * @param capturePin True if the PIN has to be captured by software else false
     * @param conHandle to get the requested card type from
     * @param dispatcher The Dispatcher to use
     * @param state The State of the PIN
     */
    public UnblockPINDialog(UserConsent gui, Dispatcher dispatcher, ConnectionHandleType conHandle,
	    RecognizedState state, boolean capturePin) {
	this.gui = gui;
	this.conHandle = conHandle;
	this.state = state;
	this.capturePin = capturePin;
	this.dispatcher = dispatcher;
    }

    private UserConsentDescription createUserConsentDescription() {
	UserConsentDescription uc = new UserConsentDescription(lang.translationForKey(TITLE, AppVersion.getName()));

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

	if (state.equals(RecognizedState.PIN_blocked)) {
	    Step pukStep = createPUKStep();
	    steps.add(pukStep);

	    Step successStep = createSuccessStep();
	    steps.add(successStep);
	} else {
	    Step errorStep = createErrorStep();
	    steps.add(errorStep);
	}

	return steps;
    }

    /**
     * Create the step that informs the user that everything went fine.
     *
     * @return Step showing success message
     */
    private Step createSuccessStep() {
	Step successStep = new Step("success", lang.translationForKey(SUCCESSSTEP_TITLE));

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
	Step errorStep = new Step("insert-card", lang.translationForKey(ERRORSTEP_TITLE));
	Text i1 = new Text();
	i1.setText(lang.translationForKey(ERRORSTEP_DESCRIPTION));
	errorStep.getInputInfoUnits().add(i1);
	return errorStep;
    }

    /**
     * Create the step that asks the user to insert the PUK.
     * 
     * @return Step for PUK entry
     */
    private Step createPUKStep() {
	Step pukStep = new Step("insert-card", lang.translationForKey(PUKSTEP_TITLE));
	Text i1 = new Text();
	pukStep.getInputInfoUnits().add(i1);
	if (!capturePin) {
	    pukStep.setInstantReturn(true);
	    i1.setText(lang.translationForKey(PUKSTEP_NATIVE_DESCRIPTION));
	} else {
	    i1.setText(lang.translationForKey(PUKSTEP_DESCRIPTION));
	    PasswordField pukField = new PasswordField(PUK_FIELD);
	    pukField.description = lang.translationForKey(PUKSTEP_PUK);
	    pukStep.getInputInfoUnits().add(pukField);
	}

	StepAction pinAction = new PUKStepAction(capturePin, conHandle.getSlotHandle(), dispatcher, pukStep);
	pukStep.setAction(pinAction);
	return pukStep;
    }

    /**
     * Shows this Dialog.
     */
    public void show() {
	UserConsentNavigator ucr = gui.obtainNavigator(createUserConsentDescription());
	ExecutionEngine exec = new ExecutionEngine(ucr);
	exec.process();
    }

}
