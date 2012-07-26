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

package org.openecard.client.gui.executor;

import java.util.*;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.InputInfoUnit;
import org.openecard.client.gui.definition.OutputInfoUnit;
import org.openecard.client.gui.definition.Step;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecutionEngine {

    private final UserConsentNavigator navigator;
    private final TreeMap<String, ExecutionResults> results = new TreeMap<String, ExecutionResults>();
    private TreeMap<String, StepAction> customActions;

    public ExecutionEngine(UserConsentNavigator navigator) {
	this.navigator = navigator;
    }

    public void addCustomAction(StepAction action) {
	getCustomActions().put(action.getStepID(), action);
    }

    public ResultStatus process() {
	StepResult next = navigator.next(); // get first step
	// loop over steps. break inside loop
	while (true) {
	    ResultStatus result = next.getStatus();
	    // close dialog on cancel
	    if (result == ResultStatus.CANCEL) {
		navigator.close();
		return result;
	    }

	    // get result and put it in resultmap
	    List<OutputInfoUnit> stepResults = next.getResults();
	    Map<String, ExecutionResults> oldResults = Collections.unmodifiableMap(results);
	    results.put(next.getStepID(), new ExecutionResults(next.getStepID(), stepResults));

	    // replace InfoInputUnit values in live list
	    if (! next.getStep().isResetOnLoad()) {
		Step s = next.getStep();
		List<InputInfoUnit> inputInfo = s.getInputInfoUnits();
		Map<String,InputInfoUnit> infoMap = new HashMap<String, InputInfoUnit>();
		// create index over infos
		for (InputInfoUnit nextInfo : inputInfo) {
		    infoMap.put(nextInfo.getID(), nextInfo);
		}
		for (OutputInfoUnit nextOut : stepResults) {
		    InputInfoUnit matchingInfo = infoMap.get(nextOut.getID());
		    // an entry must exist, otherwise this is an error in the GUI implementation
		    // this type of error should be found in tests
		    matchingInfo.copyContentFrom(nextOut);
		}
	    }

	    // perform action
	    StepAction action = getAction(next.getStepID());
	    StepActionResult actionResult = action.perform(oldResults, next);

	    // replace step if told by result value
	    if (actionResult.getReplacement() != null) {
		switch (actionResult.getStatus()) {
		    case BACK:
			next = navigator.replacePrevious(actionResult.getReplacement());
			break;
		    case NEXT:
			if (navigator.hasNext()) {
			    next = navigator.replaceNext(actionResult.getReplacement());
			} else {
			    navigator.close();
			    return convertStatus(StepActionResultStatus.NEXT);
			}
			break;
		    case REPEAT:
			next = navigator.replaceCurrent(actionResult.getReplacement());
			break;
		}
		// no replacement just proceed
	    } else {
		switch (actionResult.getStatus()) {
		    case BACK:
			next = navigator.previous();
			break;
		    case NEXT:
			if (navigator.hasNext()) {
			    next = navigator.next();
			} else {
			    navigator.close();
			    return convertStatus(StepActionResultStatus.NEXT);
			}
			break;
		    case REPEAT:
			next = navigator.current();
			break;
		}
	    }
	}
    }

    public Map<String, ExecutionResults> getResults() {
	return Collections.unmodifiableMap(results);
    }

    private TreeMap<String, StepAction> getCustomActions() {
	if (customActions == null) {
	    customActions = new TreeMap<String, StepAction>();
	}
	return customActions;
    }

    private StepAction getAction(String stepName) {
	if (hasCustomAction(stepName)) {
	    return getCustomActions().get(stepName);
	} else {
	    return new StepAction(stepName) {

		@Override
		public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
		    switch (result.getStatus()) {
			case BACK:
			    return new StepActionResult(StepActionResultStatus.BACK);
			case OK:
			    return new StepActionResult(StepActionResultStatus.NEXT);
			default:
			    return new StepActionResult(StepActionResultStatus.REPEAT); // cancel performed before
		    }
		}
	    };
	}
    }

    private boolean hasCustomAction(String stepName) {
	return getCustomActions().containsKey(stepName);
    }

    private ResultStatus convertStatus(StepActionResultStatus in) {
	switch (in) {
	    case BACK:
		return ResultStatus.BACK;
	    case NEXT:
		return ResultStatus.OK;
	    default:
		return ResultStatus.OK; // repeat undefined for this kind of status
	}
    }

}
