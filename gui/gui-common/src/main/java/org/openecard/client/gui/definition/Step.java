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


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class Step {

    private String name;
    private boolean reversible=true;
    private boolean instantReturn=false;
    private List<InputInfoUnit> inputInfoUnits;

    public Step(String name) {
	this.name = name;
    }


    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
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

}
