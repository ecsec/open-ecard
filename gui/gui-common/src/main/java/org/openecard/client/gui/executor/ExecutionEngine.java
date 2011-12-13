package org.openecard.client.gui.executor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openecard.client.gui.ResultStatus;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.UserConsentNavigator;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecutionEngine {

    private final UserConsentNavigator navigator;
    private final TreeMap<String, ExecutionResults> results = new TreeMap<String, ExecutionResults>();

    private TreeMap<String,StepAction> customActions;

    public ExecutionEngine(UserConsentNavigator navigator) {
	this.navigator = navigator;
    }


    public void addCustomAction(StepAction action) {
	getCustomActions().put(action.associatedStepName(), action);
    }

    public ResultStatus process() {
	StepResult next = navigator.next(); // get first step
	ResultStatus result = ResultStatus.OK;
	// loop over steps. break inside loop
	while (true) {
	    result = next.status();
	    // close dialog on cancel
	    if (result == ResultStatus.CANCEL) {
		navigator.close();
		return result;
	    }

	    // get result and put it in resultmap
	    List<OutputInfoUnit> stepResults = next.results();
	    Map<String, ExecutionResults> oldResults = Collections.unmodifiableMap(results);
	    results.put(next.stepName(), new ExecutionResults(next.stepName(), stepResults));

	    // perform action
	    StepAction action = getAction(next.stepName());
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

    public Map<String,ExecutionResults> getResults() {
	return Collections.unmodifiableMap(results);
    }

    private TreeMap<String,StepAction> getCustomActions() {
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
		    switch (result.status()) {
			case BACK: return new StepActionResult(StepActionResultStatus.BACK);
			case OK: return new StepActionResult(StepActionResultStatus.NEXT);
			default: return new StepActionResult(StepActionResultStatus.REPEAT); // cancel performed before
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
	    case BACK: return ResultStatus.BACK;
	    case NEXT: return ResultStatus.OK;
	    default:   return ResultStatus.OK; // repeat undefined for this kind of status
	}
    }

}
