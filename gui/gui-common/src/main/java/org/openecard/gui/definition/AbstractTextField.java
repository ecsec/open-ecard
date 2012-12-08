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

package org.openecard.gui.definition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base definition for text fields.
 * A field can be identified by an ID.
 *
 * @see TextField
 * @see PasswordField
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public abstract class AbstractTextField extends IDTrait implements InputInfoUnit, OutputInfoUnit {

    private static final Logger _logger = LoggerFactory.getLogger(AbstractTextField.class);

    private String description;
    private String value;
    private int minLength = 0;
    private int maxLength = Integer.MAX_VALUE;

    /**
     * Creates an instance initialized with a given ID.
     *
     * @param id The id to initialize the instance with.
     */
    public AbstractTextField(String id) {
	super(id);
    }


    /**
     * Returns the description of the text field.
     * The description can be used as a label in front of the field.
     *
     * @return String describing the text field.
     */
    public String getDescription() {
	return description;
    }

    /**
     * Sets the description of the text field.
     * The description can be used as a label in front of the field.
     *
     * @param description String describing the text field.
     */
    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * Gets the value of the text field.
     *
     * @return The value of the text field.
     */
    public String getValue() {
	return value;
    }

    /**
     * Sets the value of the text field.
     *
     * @param value The value of the text field.
     */
    public void setValue(String value) {
	this.value = value;
    }

    /**
     * Gets the minimum length of the text field value.
     * The length of the value should be checked in the GUI implementation. If the length is wrong, the implementation
     * can notify the user and let him correct the value.
     *
     * @see #setMaxLength(int)
     * @return The minimum length of the text value.
     */
    public int getMinLength() {
	return minLength;
    }

    /**
     * Sets the minimum length of the text field value.
     * The length of the value should be checked in the GUI implementation. If the length is wrong, the implementation
     * can notify the user and let him correct the value.
     *
     * @see #setMaxLength(int)
     * @return The minimum length of the text value.
     */
    public void setMinLength(int minLength) {
	this.minLength = minLength;
    }

    /**
     * Gets the maximum length of the text field value.
     * The length of the value should be checked in the GUI implementation. If the length is wrong, the implementation
     * can notify the user and let him correct the value.
     *
     * @see #getMinLength()
     * @return The maximum length of the text value.
     */
    public int getMaxLength() {
	return maxLength;
    }

    /**
     * Sets the maximum length of the text field value.
     * The length of the value should be checked in the GUI implementation. If the length is wrong, the implementation
     * can notify the user and let him correct the value.
     *
     * @see #setMinLength()
     * @return The maximum length of the text value.
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
