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
 */
package org.openecard.gui.definition

/**
 * Definition of a box item for use in selection box elements.
 * A box item has a name to identify it, a text which is shown to the user and it is either checked or unchecked. The
 * item can be disabled if the user has no choice to check or uncheck it.
 *
 * @see AbstractBox
 *
 * @see Checkbox
 *
 * @see Radiobox
 *
 * @author Tobias Wich
 */
class BoxItem {
	/**
	 * Gets the name of the item.
	 * The name is used to identify the item and thus should be unique in the surrounding selection box group.
	 */
	var name: String? = null

	/**
	 * Gets the display text of the item.
	 * The text is displayed on the GUI to indicate the meaning of the option to the user.
	 */
	var text: String? = null

	/**
	 * Gets the selection value of the box item.
	 * This value is used to preselect items and to set the value when the step displaying this item is finished.
	 */
	var isChecked: Boolean = false

	/**
	 * Gets whether the item is enabled, or disabled.
	 * Disabled items can be used to show a preselected value to the user, but do not allow modification of the value.
	 *
	 */
	var isDisabled: Boolean = false
}
