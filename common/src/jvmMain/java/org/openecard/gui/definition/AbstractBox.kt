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

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

/**
 * Base definition for selection boxes.
 * The boxes form a Group of options that can be selected.<br></br>
 * An AbstractBox can be identified by an ID.
 *
 * @see Radiobox
 *
 * @see Checkbox
 *
 * @see BoxItem
 *
 * @author Tobias Wich
 */
abstract class AbstractBox(
	id: String,
) : IDTrait(id),
	InputInfoUnit,
	OutputInfoUnit {
	/**
	 * Gets the group text of the selection box group.
	 *
	 * @return The text for title of this group.

	 * Sets the group text of the selection box group.
	 *
	 * @param groupText The text for title of this group.
	 */
	var groupText: String? = null

	/**
	 * Get writable list of box items.
	 * The box items form the actual selectable content of this type. Modifications made to this list directly influence
	 * this instance.
	 *
	 * @return List of box items.
	 */
	val boxItems: MutableList<BoxItem> by lazy { mutableListOf() }

	/**
	 * {@inheritDoc}
	 *
	 * **NOTE:** It is important to override this method in subclasses if additional members are introduced.
	 */
	override fun copyContentFrom(origin: InfoUnit) {
		if (this.javaClass != origin.javaClass) {
			logger.warn { "${"Trying to copy content from type {} to type {}."} ${origin.javaClass} ${this.javaClass}" }
			return
		}
		val other = origin as AbstractBox
		// do copy
		this.groupText = other.groupText
		boxItems.clear()
		for (next in other.boxItems) {
			val copy = BoxItem()
			copy.isChecked = next.isChecked
			copy.isDisabled = next.isDisabled
			copy.name = next.name
			copy.text = next.text
			boxItems.add(copy)
		}
	}
}
