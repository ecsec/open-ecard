package org.openecard.client.gui.executor;

import java.util.List;
import org.openecard.client.gui.definition.OutputInfoUnit;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ExecutionResults {

    private final String stepName;
    private final List<OutputInfoUnit> results;

    public ExecutionResults(String stepName, List<OutputInfoUnit> results) {
	this.stepName = stepName;
	this.results = results;
    }


    /**
     * @return the stepName
     */
    public String getStepName() {
	return stepName;
    }

    /**
     * @return the results
     */
    public List<OutputInfoUnit> getResults() {
	return results;
    }

}
