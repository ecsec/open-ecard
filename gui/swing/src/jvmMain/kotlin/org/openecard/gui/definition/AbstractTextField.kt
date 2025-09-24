/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
import java.util.Arrays
import kotlin.random.Random

private val logger = KotlinLogging.logger { }

/**
 * Base definition for text fields.
 * A field can be identified by an ID.
 *
 * @see TextField
 *
 * @see PasswordField
 *
 * @author Tobias Wich
 */
abstract class AbstractTextField(
	id: String,
	random: Random = Random.Default,
) : IDTrait(random, id),
	InputInfoUnit,
	OutputInfoUnit {
	/**
	 * Returns the description of the text field.
	 * The description can be used as a label in front of the field.
	 *
	 * @return String describing the text field.

	 * Sets the description of the text field.
	 * The description can be used as a label in front of the field.
	 *
	 * @param description String describing the text field.
	 */
	var description: String? = null
	var value: CharArray = CharArray(0)
		/**
		 * Gets the value of the text field.
		 *
		 * @return The value of the text field.
		 */
		get(): CharArray = field.clone()

		/**
		 * Sets the value of the text field.
		 *
		 * @param value The value of the text field.
		 */
		set(value) {
			Arrays.fill(field, ' ')
			field = value.clone()
		}

	/**
	 * Gets the minimum length of the text field value.
	 * The length of the value should be checked in the GUI implementation. If the length is wrong, the implementation
	 * can notify the user and let him correct the value.
	 *
	 * @see .setMaxLength
	 * @return The minimum length of the text value.

	 * Sets the minimum length of the text field value.
	 * The length of the value should be checked in the GUI implementation. If the length is wrong, the implementation
	 * can notify the user and let him correct the value.
	 *
	 * @see .setMaxLength
	 * @param minLength The minimum length of the text value.
	 */
	var minLength: Int

	/**
	 * Gets the maximum length of the text field value.
	 * The length of the value should be checked in the GUI implementation. If the length is wrong, the implementation
	 * can notify the user and let him correct the value.
	 *
	 * @see .getMinLength
	 * @return The maximum length of the text value.

	 * Sets the maximum length of the text field value.
	 * The length of the value should be checked in the GUI implementation. If the length is wrong, the implementation
	 * can notify the user and let him correct the value.
	 *
	 * @see .setMinLength
	 * @param maxLength The maximum length of the text value.
	 */
	var maxLength: Int

	/**
	 * Creates an instance initialized with a given ID.
	 *
	 * @param id The id to initialize the instance with.
	 */
	init {
		this.minLength = 0
		this.maxLength = Int.MAX_VALUE
	}

	/**
	 * {@inheritDoc}
	 *
	 * **NOTE:** It is important to override this method in subclasses if additional members are introduced.
	 */
	override fun copyContentFrom(origin: InfoUnit) {
		if (this.javaClass != origin.javaClass) {
			logger.warn {
				"${"Trying to copy content from type {} to type {}."} ${origin.javaClass} ${
					this.javaClass
				}"
			}
			return
		}
		val other = origin as AbstractTextField
		// do copy
		this.description = other.description
		Arrays.fill(this.value, ' ')
		this.value = other.value.clone()
		this.minLength = other.minLength
		this.maxLength = other.maxLength
	}
}
