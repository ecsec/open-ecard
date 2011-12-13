package org.openecard.client.gui.executor;

import org.openecard.client.gui.definition.Step;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class StepActionResult {

    private final StepActionResultStatus status;
    private final Step replacement;

    public StepActionResult(StepActionResultStatus status) {
	this(status, null);
    }

    public StepActionResult(StepActionResultStatus status, Step replacement) {
	this.status = status;
	this.replacement = replacement;
    }

    /**
     * @return the status
     */
    public StepActionResultStatus getStatus() {
	return status;
    }

    /**
     * @return the replacement
     */
    public Step getReplacement() {
	return replacement;
    }

}
