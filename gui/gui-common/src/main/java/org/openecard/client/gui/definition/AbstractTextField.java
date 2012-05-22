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

/**
 * Implements a abstract text field.
 * A field can be identified by an ID.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class AbstractTextField implements InputInfoUnit, OutputInfoUnit {

    private String id;
    private String description;
    private String value;
    private int minLength = 0;
    private int maxLength = Integer.MAX_VALUE;

    /**
     * Returns the ID of the text field.
     *
     * @return ID
     */
    public String getID() {
	return id;
    }

    /**
     * Sets the ID of the text field.
     *
     * @param id ID
     */
    public void setID(String id) {
	this.id = id;
    }

    /**
     *
     * Returns the description of the text field.
     *
     * @return Description
     */
    public String getDescription() {
	return description;
    }

    /**
     * Sets the description of the text field.
     *
     * @param description Description
     */
    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * @return the value
     */
    public String getValue() {
	return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
	this.value = value;
    }

    /**
     * @return the minLength
     */
    public int getMinLength() {
	return minLength;
    }

    /**
     * @param minLength the minLength to set
     */
    public void setMinLength(int minLength) {
	this.minLength = minLength;
    }

    /**
     * @return the maxLength
     */
    public int getMaxLength() {
	return maxLength;
    }

    /**
     * @param maxLength the maxLength to set
     */
    public void setMaxLength(int maxLength) {
	this.maxLength = maxLength;
    }
}
