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
 * Definition class for a text element which can fold its content.
 * The ToggleText has a title which is always displayed and a text which can be folded.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class ToggleText :
	IDTrait(),
	InputInfoUnit {
	/**
	 * Gets the title of this instance.
	 *
	 * @return The title of this instance.
	 */
	var title: String? = null

	/**
	 * Get the underlying Document of this ToggleText instance.
	 *
	 * @return The [Document] used by this instance or null if there is currently no such document.

	 * Sets the Document of this ToggleText instance.
	 * <br></br>
	 * <br></br>
	 * Note: The [Document] type allows every mime type. The ability to render [Document]s of types other
	 * than text/plain depends on the GUI implementation and is not granted.
	 *
	 * @param doc [Document] to set for this ToggleText.
	 */
	var document: Document? = null

	/**
	 * Gets whether the text is collapsed or not.
	 * In the collapsed state, the element's text is not visible.
	 *
	 * @return `true` if the text is collapsed, `false` otherwise.

	 * Sets whether the text is collapsed or not.
	 * In the collapsed state, the element's text is not visible.
	 *
	 * @param collapsed `true` if the text is collapsed, `false` otherwise.
	 */
	var isCollapsed: Boolean = false

	var text: String
		/**
		 * Gets the text of this instance.
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
		 * Sets the text of this instance.
		 *
		 * @param text The text of this instance.
		 */
		set(text) {
			if (document == null) {
				document = Document()
			}

			document?.mimeType = "text/plain"
			document?.value = text.toByteArray(Charset.forName("UTF-8"))
		}

	override fun type(): InfoUnitElementType = InfoUnitElementType.TOGGLE_TEXT

	override fun copyContentFrom(origin: InfoUnit) {
		if (this.javaClass != origin.javaClass) {
			logger.warn { "${"Trying to copy content from type {} to type {}."} ${origin.javaClass} ${this.javaClass}" }
			return
		}
		val other = origin as ToggleText
		// do copy
		this.title = other.title
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
		this.isCollapsed = other.isCollapsed
	}
}
