/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.client.gui.definition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Implements a abstract text field.
 * A field can be identified by an ID.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class AbstractTextField implements InputInfoUnit, OutputInfoUnit {

    private static final Logger _logger = LoggerFactory.getLogger(AbstractTextField.class);

    private String description;
    private String value;
    private int minLength = 0;
    private int maxLength = Integer.MAX_VALUE;


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


    @Override
    public void copyContentFrom(InfoUnit origin) {
	if (!(this.getClass().equals(origin.getClass()))) {
	    _logger.warn("Trying to copy content from type {} to type {}.", origin.getClass(), this.getClass());
	    return;
	}
	AbstractTextField other = (AbstractTextField) origin;
	// do copy
	this.description = other.description;
	this.value = other.value;
	this.minLength = other.minLength;
	this.maxLength = other.maxLength;
    }

}
