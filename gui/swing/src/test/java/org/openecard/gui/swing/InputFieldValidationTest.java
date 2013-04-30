/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
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

package org.openecard.gui.swing;

import org.openecard.gui.UserConsentNavigator;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.TextField;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepAction;
import org.testng.annotations.Test;


/**
 * Test class for manual test of the input field validation.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class InputFieldValidationTest {

    private static final long WAIT_TIME = 2 * 1000; // 2 seconds

    @Test(enabled = false)
    public void test() {
	// create wait action
	WaitAction action = new WaitAction("step1", WAIT_TIME);
	// create GUI
	UserConsentNavigator nav = createNavigator(action);
	ExecutionEngine exec = new ExecutionEngine(nav);

	exec.process();
    }


    private UserConsentNavigator createNavigator(StepAction waitAction) {
	// create step
	UserConsentDescription ucd = new UserConsentDescription("consent title");

	Step s = new Step("step title");
	ucd.getSteps().add(s);
	s.setID("step1");

	s.setAction(waitAction);
	Text desc1 = new Text();
	s.getInputInfoUnits().add(desc1);
	desc1.setText("This test shows a text input field to the user with a minimum length of 4 and a maximum " +
			"length of 6 to test the input validation.");

	TextField input = new TextField("input1");
	input.setMaxLength(6);
	input.setMinLength(4);
	s.getInputInfoUnits().add(input);

	SwingUserConsent sc = new SwingUserConsent(new SwingDialogWrapper());
	return sc.obtainNavigator(ucd);
    }

}
