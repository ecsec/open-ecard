/****************************************************************************
 * Copyright (C) 2012-2017 ecsec GmbH.
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

private val LOG = KotlinLogging.logger { }

/**
 * GUI component class which represents an image.
 * In order to use the component, at least an image must be set. The MIME type is optional, but may be needed by some
 * GUI implementations.
 *
 * @author Tobias Wich
 */
class ImageBox :
	IDTrait(),
	InputInfoUnit {
	var document: Document? = null

	var imageData: ByteArray
		/**
		 * Get the raw data of the image.
		 *
		 * @see .setImageData
		 * @return The raw image data.
		 */
		get() {
			val imageData = document?.value
			return imageData!!.copyOf(imageData.size)
		}

		/**
		 * Sets the raw image data for this instance.
		 * The image must be given in the serialized form of an image container format such as PNG, JPEG, etc.
		 *
		 * @param imageData The raw image data.
		 */
		set(imageData) {
			document?.value = imageData.copyOf(imageData.size)
		}

	var mimeType: String?
		/**
		 * Gets the MIME type for the image represented by this instance.
		 *
		 * @see .setMimeType
		 * @return String containing the MIME type.
		 */
		get() = document?.mimeType

		/**
		 * Sets the MIME type for the image represented by this instance.
		 *
		 * @see [IANA Registered MIME Types](https://www.iana.org/assignments/media-types/)
		 *
		 * @see [IANA Registered Image MIME Types](https://www.iana.org/assignments/media-types/image/)
		 *
		 * @param mimeType MIME type describing the image type.
		 */
		set(mimeType) {
			document?.mimeType = mimeType
		}

	override fun type(): InfoUnitElementType = InfoUnitElementType.IMAGE_BOX

	override fun copyContentFrom(origin: InfoUnit) {
		if (this.javaClass != origin.javaClass) {
			LOG.warn { "${"Trying to copy content from type {} to type {}."} ${origin.javaClass} ${this.javaClass}" }
			return
		}
		val other = origin as ImageBox
		// copy document
		if (other.document != null) {
			try {
				this.document = other.document!!.clone()
			} catch (ex: CloneNotSupportedException) {
				throw AssertionError("Clone not implemented correctly in Document class.")
			}
		}
	}
}
