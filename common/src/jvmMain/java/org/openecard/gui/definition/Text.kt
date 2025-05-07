/****************************************************************************
 * Copyright (C) 2012-2015 ecsec GmbH.
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
import java.nio.charset.Charset

private val logger = KotlinLogging.logger { }

/**
 * Definition class for simple text elements.
 * The Text element is a text displaying an information to the user.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
class Text :
	IDTrait,
	InputInfoUnit {
	/**
	 * Get the underlying Document of this Text instance.
	 *
	 * @return The [Document] used by this instance or null if there is currently no such document.

	 * Sets the Document of this Text instance.
	 * <br></br>
	 * <br></br>
	 * Note: The [Document] type allows every mime type. The ability to render [Document]s of types other
	 * than text/plain depends on the GUI implementation and is not granted.
	 *
	 * @param doc [Document] to set for this Text.
	 */
	var document: Document? = null

	/**
	 * Creates a new empty instance.
	 */
	constructor()

	/**
	 * Creates a new instance from the given String `text`.
	 *
	 * @param text The text which shall be displayed.
	 */
	constructor(text: String) : this(text.toByteArray(Charset.forName("UTF-8")), "text/plain")

	private constructor(value: ByteArray, mimeType: String) {
		document = Document()
		document?.mimeType = mimeType
		document?.value = value
	}

	/**
	 * Creates a new instance from the given [Document] `doc`.
	 * <br></br>
	 * <br></br>
	 * Note: The [Document] type allows every mime type. The ability to render [Document]s of types other
	 * than text/plain depends on the GUI implementation and is not granted.
	 *
	 * @param doc [Document] to set for this Text.
	 */
	constructor(doc: Document?) {
		document = doc
	}

	var text: String
		/**
		 * Gets the text set for this instance.
		 *
		 * @return The text of this instance or an empty string if the underlying [Document] is `NULL` or the
		 * value of the [Document] or the MimeType of the underlying [Document] is `NULL` or does not start
		 * with `text/`.
		 */
		get() {
			if (document == null || document?.value == null || document?.value!!.isEmpty()) {
				return ""
			}

			return if (document?.mimeType != null && document?.mimeType!!.startsWith("text/")) {
				String(document?.value!!, Charset.forName("UTF-8"))
			} else {
				""
			}
		}

		/**
		 * Sets the text for this instance.
		 *
		 * @param text The text to set for this instance.
		 */
		set(text) {
			if (document == null) {
				document = Document()
			}

			document?.mimeType = ("text/plain")
			document?.value = text.toByteArray(Charset.forName("UTF-8"))
		}

	override fun type(): InfoUnitElementType = InfoUnitElementType.TEXT

	override fun copyContentFrom(origin: InfoUnit) {
		if (this.javaClass != origin.javaClass) {
			logger.warn { "${"Trying to copy content from type {} to type {}."} ${origin.javaClass} ${this.javaClass}" }
			return
		}
		val other = origin as Text
		// do copy
		if (other.document != null) {
			val doc = Document()
			if (other.document?.mimeType != null) {
				doc.mimeType = other.document?.mimeType
			}

			if (other.document?.value != null) {
				val contentBytes = ByteArray(other.document?.value!!.size)
				System.arraycopy(other.document?.value!!, 0, contentBytes, 0, other.document?.value!!.size)
				doc.value = contentBytes
			}
			this.document = doc
		}
	}
}
