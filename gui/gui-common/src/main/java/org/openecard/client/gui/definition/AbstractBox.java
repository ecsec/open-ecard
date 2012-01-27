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
public abstract class AbstractBox implements InputInfoUnit, OutputInfoUnit {

    private String groupText;
    private List<BoxItem> boxItems;

    /**
     * @return the groupText
     */
    public String getGroupText() {
	return groupText;
    }

    /**
     * @param groupText the groupText to set
     */
    public void setGroupText(String groupText) {
	this.groupText = groupText;
    }

    public List<BoxItem> getBoxItems() {
	if (boxItems == null) {
	    boxItems = new ArrayList<BoxItem>();
	}
	return boxItems;
    }

}
