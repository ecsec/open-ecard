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


/**
 * Definition of a box item for use in selection box elements.
 * A box item has a name to identify it, a text which is shown to the user and it is either checked or unchecked. The
 * item can be disabled if the user has no choice to check or uncheck it.
 *
 * @see AbstractBox
 * @see Checkbox
 * @see Radiobox
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class BoxItem {

    private String name;
    private String text;
    private boolean checked;
    private boolean disabled;

    /**
     * Creates a box item.
     */
    public BoxItem() {
	this.checked = false;
	this.disabled = false;
    }


    /**
     * Gets the name of the item.
     * The name is used to identify the item and thus should be unique in the surrounding selection box group.
     *
     * @return The name of the item.
     */
    public String getName() {
	return name;
    }

    /**
     * Sets the name of the item.
     * The name is used to identify the item and thus should be unique in the surrounding selection box group.
     *
     * @param name The name of the item.
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * Gets the display text of the item.
     * The text is displayed on the GUI to indicate the meaning of the option to the user.
     *
     * @return The text of the item.
     */
    public String getText() {
	return text;
    }

    /**
     * Sets the display text of the item.
     * The text is displayed on the GUI to indicate the meaning of the option to the user.
     *
     * @param text The text of the item.
     */
    public void setText(String text) {
	this.text = text;
    }

    /**
     * Gets the selection value of the box item.
     *
     * @return {@code true} if the item is checked/selected, {@code false} otherwise.
     */
    public boolean isChecked() {
	return checked;
    }

    /**
     * Sets the selection value of the box item.
     * This function is used to preselect items and to set the value when the step displaying this item is finished.
     *
     * @param checked {@code true} if the item is checked/selected, {@code false} otherwise.
     */
    public void setChecked(boolean checked) {
	this.checked = checked;
    }

    /**
     * Gets whether the item is enabled, or disabled.
     * Disabled items can be used to show a preselected value to the user, but do not allow modification of the value.
     *
     * @return {@code true} if the item is disabled, {@code false} otherwise.
     */
    public boolean isDisabled() {
	return disabled;
    }

    /**
     * Sets whether the item is enabled, or disabled.
     * Disabled items can be used to show a preselected value to the user, but do not allow modification of the value.
     *
     * @param disabled {@code true} if the item is disabled, {@code false} otherwise.
     */
    public void setDisabled(boolean disabled) {
	this.disabled = disabled;
    }

}
