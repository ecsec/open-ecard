/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.ExecutionEngine;
import org.testng.annotations.Test;
import static org.testng.Assert.*;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class InstantReturnTest {

    private static final long DIFF_TIME = 2*1000; // 2 seconds
    private static final long VARIANCE = 1000;

    // TODO: make Selenium test which really proves, that the GUI works correctly
    /**
     * Test if the GUI closes itself after executing an action with instantreturn set.
     * There is no way to determine whether the GUI is displayed at all. This check must be part of a Selenium test.
     */
    @Test(enabled = true)
    public void testInstantReturn() {
	UserConsentNavigator nav = createNavigator();
	ExecutionEngine exec = new ExecutionEngine(nav);

	// create wait action
	WaitAction action = new WaitAction("step1", DIFF_TIME);
	exec.addCustomAction(action);

	exec.process();
	// eliminate most of the GUI overhead by retrieving start time from action
	long startTime = action.getStartTime();
	long stopTime = System.currentTimeMillis();

	long act = stopTime - startTime;
	long diff = act - DIFF_TIME;
	String msg = "Display time of dialog differs " + diff + "ms from reference value (" + VARIANCE + "ms allowed).";
	assertTrue(diff <= VARIANCE, msg);
    }


    private UserConsentNavigator createNavigator() {
	// create step
	UserConsentDescription ucd = new UserConsentDescription("consent title");

	Step s = new Step("step title");
	ucd.getSteps().add(s);
	s.setID("step1");
	s.setInstantReturn(true);

	Text desc1 = new Text();
	s.getInputInfoUnits().add(desc1);
	desc1.setText("This test opens a step with instantreturn set. An action waits for two seconds.");
	Text desc2 = new Text();
	s.getInputInfoUnits().add(desc2);
	desc2.setText("If the window is closed after these two seconds, the test is successful.");

	SwingUserConsent sc = new SwingUserConsent(new SwingDialogWrapper());
	return sc.obtainNavigator(ucd);
    }

}
