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
