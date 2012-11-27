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

import java.util.Map;
import org.openecard.gui.StepResult;
import org.openecard.gui.definition.Step;
import org.openecard.gui.executor.ExecutionResults;
import org.openecard.gui.executor.StepAction;
import org.openecard.gui.executor.StepActionResult;
import org.openecard.gui.executor.StepActionResultStatus;


public class WaitAction extends StepAction {

    private final long sleepTime;

    public WaitAction(Step step, long sleepTime) {
	this(step.getID(), sleepTime);
    }
    public WaitAction(String stepId, long sleepTime) {
	super(stepId);
	this.sleepTime = sleepTime;
    }

    @Override
    public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
	System.out.println("sleeping for " + sleepTime + " ms.");
	try {
	    Thread.sleep(sleepTime);
	} catch (InterruptedException e) {
	    // ignore in test
	}
	StepActionResult actionResult = new StepActionResult(StepActionResultStatus.NEXT);
	return actionResult;
    }

}
