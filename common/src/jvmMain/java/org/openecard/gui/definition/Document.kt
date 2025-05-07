/****************************************************************************
 * Copyright (C) 2015-2017 ecsec GmbH.
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
 * Implementation of a simple document type.
 * The document is represented by a MimetType and a document value which represents the content of the document.
 *
 * @author Hans-Martin Haase
 */
class Document : Cloneable {
	/**
	 * Get the MimeType of this document.
	 *
	 * @return The MimeType of this document.

	 * Sets the MimeType of the document to display.
	 *
	 * @param type The MimeType to set for this document.
	 */
	var mimeType: String? = null

	/**
	 * Get the value of this document as byte array.
	 *
	 * @return The value/content of this document as byte array.

	 * Sets the value of this document.
	 *
	 * @param value The byte array representation of this documents value.
	 */
	lateinit var value: ByteArray

	/**
	 * Creates a new empty instance.
	 */
	constructor()

	/**
	 * Creates a new instance from the given MimeType and document content.
	 *
	 * @param mimeType The MimeType to set for the new instance.
	 * @param content The content to set for the new instance.
	 */
	constructor(mimeType: String?, content: ByteArray) {
		this.mimeType = mimeType
		value = content
	}

	public override fun clone(): Document {
		val result = super.clone() as Document
		// deep copy data object
		val data = result.value
		result.value = data.copyOf(data.size)
		return result
	}
}
