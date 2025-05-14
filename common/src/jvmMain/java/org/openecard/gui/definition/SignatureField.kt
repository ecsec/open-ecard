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
 * Definition class for signature fields.
 * Signature fields provide a drawing canvas where the user can create a signature.
 *
 * @author Tobias Wich
 */
class SignatureField(
	id: String,
) : IDTrait(id),
	InputInfoUnit,
	OutputInfoUnit {
	/**
	 * Gets the description text of the signature field.
	 * The description text is shown besides the signature field.
	 *
	 * @return The description text of this element.

	 * Sets the description text of the signature field.
	 * The description text is shown besides the signature field.
	 *
	 * @param text The description text of this element.
	 */
	var text: String? = null
	private var value: ByteArray? = null

	/**
	 * Gets the value of the signature field.
	 * The signature is an image. The value of the signature field is encoded as a PNG image.
	 *
	 * @return The value of the signature field encoded as a PNG image.
	 */
	fun getValue(): ByteArray? = if (value == null) null else value!!.copyOf(value!!.size)

	/**
	 * Sets the value of the signature field.
	 * The signature is an image. The value of the signature field is encoded as a PNG image.
	 *
	 * @param value The value of the signature field encoded as a PNG image.
	 */
	fun setValue(value: ByteArray) {
		this.value = value.copyOf(value.size)
	}

	override fun type(): InfoUnitElementType = InfoUnitElementType.SIGNAUTRE_FIELD

	override fun copyContentFrom(origin: InfoUnit) {
		if (this.javaClass != origin.javaClass) {
			logger.warn {
				"${"Trying to copy content from type {} to type {}."} ${origin.javaClass} ${
					this.javaClass
				}"
			}
			return
		}
		val other = origin as SignatureField
		// do copy
		this.text = other.text
		if (other.value != null) {
			this.value = other.value!!.copyOf(other.value!!.size)
		}
	}
}
