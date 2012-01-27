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
public class UserConsentDescription {

    private String title;
    private String dialogType;
    private ArrayList<Step> steps;

    public UserConsentDescription(String title, String dialogType) {
	this.title = title;
	this.dialogType = dialogType;
    }

    public UserConsentDescription(String title) {
	this.title = title;
    }


    /**
     * @return the title
     */
    public String getTitle() {
	return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
	this.title = title;
    }

    /**
     * @return the dialogType
     */
    public String getDialogType() {
	return dialogType;
    }

    /**
     * @param dialogType the dialogType to set
     */
    public void setDialogType(String dialogType) {
	this.dialogType = dialogType;
    }

    public List<Step> getSteps() {
	if (steps == null) {
	    steps = new ArrayList<Step>();
	}
	return steps;
    }

}
