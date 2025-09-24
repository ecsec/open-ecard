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
 * The document is represented by a MimeType and a document value which represents the content of the document.
 *
 * @author Hans-Martin Haase
 */
class Document(
	/**
	 * The MimeType of this document.
	 */
	var mimeType: String,
	/**
	 * The value of this document as byte array.
	 * This array is cloned.
	 */
	value: ByteArray,
) : Cloneable {
	/**
	 * The value of this document as byte array.
	 */
	var value: ByteArray = value.copyOf()

	public override fun clone(): Document = Document(mimeType, value.copyOf())
}
