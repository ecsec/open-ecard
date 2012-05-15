/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openecard.client.gui.definition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.openecard.client.gui.StepResult;
import org.openecard.client.gui.executor.ExecutionResults;
import org.openecard.client.gui.executor.StepAction;
import org.openecard.client.gui.executor.StepActionResult;
import org.openecard.client.gui.executor.StepActionResultStatus;

/**
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Step {

    private String id;
    private String title;
    private String description;
    private StepAction action;
    private boolean reversible = true;
    private boolean instantReturn = false;
    private List<InputInfoUnit> inputInfoUnits;

    public Step(String title) {
	this(UUID.randomUUID().toString(), title);
    }

    public Step(String id, String title) {
	this.id = id;
	this.title = title;
    }

    public String getID() {
	return id;
    }

    public void setID(String id) {
	this.id = id;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public boolean isReversible() {
	return reversible;
    }

    public void setReversible(boolean reversible) {
	this.reversible = reversible;
    }

    public boolean isInstantReturn() {
	return instantReturn;
    }

    public void setInstantReturn(boolean instantReturn) {
	this.instantReturn = instantReturn;
    }

    public List<InputInfoUnit> getInputInfoUnits() {
	if (inputInfoUnits == null) {
	    inputInfoUnits = new ArrayList<InputInfoUnit>();
	}
	return inputInfoUnits;
    }

    public boolean isMetaStep() {
	return getInputInfoUnits().isEmpty();
    }

    public StepAction getAction() {
	if (action == null) {
	    return new StepAction(getID()) {

		@Override
		public StepActionResult perform(Map<String, ExecutionResults> oldResults, StepResult result) {
		    switch (result.getStatus()) {
			case BACK:
			    return new StepActionResult(StepActionResultStatus.BACK);
			case OK:
			    return new StepActionResult(StepActionResultStatus.NEXT);
			default:
			    return new StepActionResult(StepActionResultStatus.REPEAT);
		    }
		}
	    };
	}
	return action;
    }

    public void setAction(StepAction action) {
	this.action = action;
    }
}
