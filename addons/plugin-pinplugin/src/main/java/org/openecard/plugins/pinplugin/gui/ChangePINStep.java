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


/**
 * The Step for changing the PIN.
 * This step simply requests the user to enter the old and the new PIN.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class ChangePINStep extends Step {

    private final I18n lang = I18n.getTranslation("pinplugin");

    // translation constants
    private static final String PINSTEP_NEWPINREPEAT = "action.changepin.userconsent.pinstep.newpinrepeat";
    private static final String PINSTEP_NEWPIN = "action.changepin.userconsent.pinstep.newpin";
    private static final String PINSTEP_OLDPIN = "action.changepin.userconsent.pinstep.oldpin";
    private static final String PINSTEP_DESCRIPTION = "action.changepin.userconsent.pinstep.description";
    private static final String PINSTEP_NATIVE_DESCRIPTION = "action.changepin.userconsent.pinstep.native_description";
    private static final String UNBLOCKING_REQUIRED = "action.changepin.userconsent.pinstep.unblocking_required";
    private static final String REMAINING_ATTEMPTS = "action.changepin.userconsent.pinstep.remaining_attempts";
    private static final String WRONG_ENTRY = "action.changepin.userconsent.pinstep.wrong_entry";
    private static final String INCORRECT_INPUT = "action.changepin.userconsent.pinstep.incorrect_input";

    // GUI element IDs
    public static final String OLD_PIN_FIELD = "OLD_PIN_FIELD";
    public static final String NEW_PIN_FIELD = "NEW_PIN_FIELD";
    public static final String NEW_PIN_REPEAT_FIELD = "NEW_PIN_REPEAT_FIELD";

    /**
     * Creates the Step for entering the new and old PIN.
     * 
     * @param id The ID to initialize the step with.
     * @param title Title string of the step.
     * @param capturePin True if the PIN has to be captured by software else false.
     * @param retryCounter The current retry counter for the PIN.
     * @param enteredWrong True if the user entered the PIN wrong before and a corresponding text should be displayed.
     * @param verifyFailed 
     */
    public ChangePINStep(String id, String title, boolean capturePin, int retryCounter, boolean enteredWrong,
	    boolean verifyFailed) {
	super(id, title);
	setReversible(false);

	if (retryCounter < 1) {
	    // show unblocking required message and return
	    Text description = new Text();
	    description.setText(lang.translationForKey(UNBLOCKING_REQUIRED));
	    getInputInfoUnits().add(description);
	    return;
	}

	Text i1 = new Text();
	getInputInfoUnits().add(i1);

	if (!capturePin) {
	    setInstantReturn(true);
	    i1.setText(lang.translationForKey(PINSTEP_NATIVE_DESCRIPTION));
	} else {
	    i1.setText(lang.translationForKey(PINSTEP_DESCRIPTION));

	    PasswordField oldPIN = new PasswordField(OLD_PIN_FIELD);
	    oldPIN.setDescription(lang.translationForKey(PINSTEP_OLDPIN));
	    getInputInfoUnits().add(oldPIN);

	    PasswordField newPIN = new PasswordField(NEW_PIN_FIELD);
	    newPIN.setDescription(lang.translationForKey(PINSTEP_NEWPIN));
	    getInputInfoUnits().add(newPIN);

	    PasswordField newPINRepeat = new PasswordField(NEW_PIN_REPEAT_FIELD);
	    newPINRepeat.setDescription(lang.translationForKey(PINSTEP_NEWPINREPEAT));
	    getInputInfoUnits().add(newPINRepeat);
	}

	if (enteredWrong) {
	    // add note for mistyped PIN
	    Text noteWrongEntry = new Text();
	    noteWrongEntry.setText(lang.translationForKey(WRONG_ENTRY));
	    getInputInfoUnits().add(noteWrongEntry);
	}

	if (verifyFailed) {
	    // add note for incorrect input
	    Text incorrectInput = new Text();
	    incorrectInput.setText(lang.translationForKey(INCORRECT_INPUT));
	    getInputInfoUnits().add(incorrectInput);
	}

	if (retryCounter < 3) {
	    // display the remaining attempts
	    Text txtRemainingAttempts = new Text();
	    txtRemainingAttempts.setText(lang.translationForKey(REMAINING_ATTEMPTS, retryCounter));
	    getInputInfoUnits().add(txtRemainingAttempts);
	}
    }

}
