/*
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
package org.openecard.richclient.processui.definition

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

/**
 * GUI component class which represents an image.
 * In order to use the component, at least an image must be set. The MIME type is optional, but may be needed by some
 * GUI implementations.
 *
 * @author Tobias Wich
 */
class ImageBox(
	var document: Document,
) : IDTrait(),
	InputInfoUnit {
	/**
	 * The raw data of the image.
	 */
	var imageData: ByteArray
		get() {
			val imageData = document.value
			return imageData.copyOf(imageData.size)
		}
		set(imageData) {
			document.value = imageData.copyOf(imageData.size)
		}

	/**
	 * The MIME type for the image represented by this instance.
	 */
	var mimeType: String
		get() = document.mimeType
		set(mimeType) {
			document.mimeType = mimeType
		}

	override fun type(): InfoUnitElementType = InfoUnitElementType.IMAGE_BOX

	override fun copyContentFrom(origin: InfoUnit) {
		if (this.javaClass != origin.javaClass) {
			logger.warn { "${"Trying to copy content from type {} to type {}."} ${origin.javaClass} ${this.javaClass}" }
			return
		}
		val other = origin as ImageBox
		try {
			// copy document
			this.document = other.document.clone()
		} catch (ex: CloneNotSupportedException) {
			throw AssertionError("Clone not implemented correctly in Document class.")
		}
	}
}
