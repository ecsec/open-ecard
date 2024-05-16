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

import org.openecard.common.I18n;
import org.openecard.gui.definition.PasswordField;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.plugins.pinplugin.RecognizedState;


/**
 * The Step for entering the CAN.
 * This step simply requests the user to enter the CAN.
 * 
 * @author Dirk Petrautzki
 */
public class CANEntryStep extends Step {

    private final I18n lang = I18n.getTranslation("pinplugin");

    // translation constants
    private static final String CANSTEP_NOTICE = "action.changepin.userconsent.canstep.notice";
    private static final String CANSTEP_CAN = "action.changepin.userconsent.canstep.can";
    private static final String CANSTEP_DESCRIPTION = "action.changepin.userconsent.canstep.description";
    private static final String CANSTEP_NATIVE_DESCRIPTION = "action.changepin.userconsent.canstep.native_description";
    private static final String WRONG_CAN = "action.changepin.userconsent.canstepaction.wrong_can";
    private static final String INCORRECT_INPUT = "action.changepin.userconsent.canstepaction.incorrect_input";

    // GUI element id's
    public static final String CAN_FIELD = "CAN_FIELD";

    /**
     * Creates the Step for entering the CAN.
     * 
     * @param id The ID to initialize the step with.
     * @param title Title string of the step.
     * @param capturePin True if the PIN has to be captured by software else false.
     * @param state The current state of the PIN.
     * @param enteredWrong True if the user entered the CAN wrong before and a corresponding text should be displayed.
     * @param verifyFailed True if user input verification failed else false.
     */
    public CANEntryStep(String id, String title, boolean capturePin, RecognizedState state, boolean enteredWrong,
	    boolean verifyFailed) {
	super(id, title);
	Text i1 = new Text();
	i1.setText(lang.translationForKey(CANSTEP_NOTICE));
	getInputInfoUnits().add(i1);
	Text i2 = new Text();
	getInputInfoUnits().add(i2);

	// add description and input fields depending on terminal type
	if (!capturePin) {
	    setInstantReturn(true);
	    i2.setText(lang.translationForKey(CANSTEP_NATIVE_DESCRIPTION));
	} else {
	    i2.setText(lang.translationForKey(CANSTEP_DESCRIPTION));
	    PasswordField canField = new PasswordField(CAN_FIELD);
	    canField.setDescription(lang.translationForKey(CANSTEP_CAN));
	    getInputInfoUnits().add(canField);
	}

	// return instant if PIN is not suspended
	if (!state.equals(RecognizedState.PIN_suspended)) {
	    setInstantReturn(true);
	}

	if (enteredWrong) {
	    // add note for mistyped CAN
	    Text retryText = new Text();
	    retryText.setText(lang.translationForKey(WRONG_CAN));
	    getInputInfoUnits().add(retryText);
	}

	if (verifyFailed) {
	    // add note for incorrect input
	    Text incorrectInput = new Text();
	    incorrectInput.setText(lang.translationForKey(INCORRECT_INPUT));
	    getInputInfoUnits().add(incorrectInput);
	}
    }

}
