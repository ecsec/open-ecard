package org.openecard.client.gui.executor;

import java.util.Map;
import org.openecard.client.gui.StepResult;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class StepAction {

    private final String stepName;

    public StepAction(String stepName) {
	this.stepName = stepName;
    }


    public String associatedStepName() {
	return stepName;
    }

    public abstract StepActionResult perform(Map<String,ExecutionResults> oldResults, StepResult result);

}
