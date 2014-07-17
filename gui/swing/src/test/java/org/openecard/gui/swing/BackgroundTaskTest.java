/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

import org.openecard.gui.UserConsent;
import org.openecard.gui.definition.Step;
import org.openecard.gui.definition.Text;
import org.openecard.gui.definition.UserConsentDescription;
import org.openecard.gui.executor.BackgroundTask;
import org.openecard.gui.executor.ExecutionEngine;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;
import org.testng.annotations.Test;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class BackgroundTaskTest {

    @Test()
    public void testWait() {
	UserConsent uc = new SwingUserConsent(new SwingDialogWrapper());

	UserConsentDescription ucd = new UserConsentDescription("Test background wait");
	Step s = new Step("Wait Step");
	s.getInputInfoUnits().add(new Text("Please wait for the background task to complete ..."));
	s.setBackgroundTask(new BackgroundTask() {
	    @Override
	    public StepActionResult call() throws Exception {
		// first wait
		Thread.sleep(1000);
		// then repeat ;-)
		Step replacement = new Step("Replacement Step");
		replacement.getInputInfoUnits().add(new Text("Super cool it works."));
		replacement.setInstantReturn(true);
		return new StepActionResult(StepActionResultStatus.REPEAT, replacement);
	    }
	});
	ucd.getSteps().add(s);

	ExecutionEngine e = new ExecutionEngine(uc.obtainNavigator(ucd));
	e.process();
    }

}
