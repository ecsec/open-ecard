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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.charset.Charset

/**
 * Definition class for a text element which can fold its content.
 * The ToggleText has a title which is always displayed and a text which can be folded.
 *
 * @author Moritz Horsch
 * @author Hans-Martin Haase
 */
class ToggleText : IDTrait(), InputInfoUnit {
    /**
     * Gets the title of this instance.
     *
     * @return The title of this instance.
     */
    /**
     * Sets the title of this instance.
     *
     * @param title The title of this instance.
     */
    @JvmField
    var title: String? = null
    /**
     * Get the underlying Document of this ToggleText instance.
     *
     * @return The [Document] used by this instance or null if there is currently no such document.
     */
    /**
     * Sets the Document of this ToggleText instance.
     * <br></br>
     * <br></br>
     * Note: The [Document] type allows every mime type. The ability to render [Document]s of types other
     * than text/plain depends on the GUI implementation and is not granted.
     *
     * @param doc [Document] to set for this ToggleText.
     */
    @JvmField
    var document: Document? = null
    /**
     * Gets whether the text is collapsed or not.
     * In the collapsed state, the element's text is not visible.
     *
     * @return `true` if the text is collapsed, `false` otherwise.
     */
    /**
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
            if (document == null || document.getValue() == null || document.getValue().size == 0) {
                return ""
            }

            return if (document.getMimeType() != null && document.getMimeType().startsWith("text/")) {
                String(document.getValue(), Charset.forName("UTF-8"))
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

            document.setMimeType("text/plain")
            document.setValue(text.toByteArray(Charset.forName("UTF-8")))
        }


    override fun type(): InfoUnitElementType {
        return InfoUnitElementType.TOGGLE_TEXT
    }

    override fun copyContentFrom(origin: InfoUnit) {
        if (!(this.javaClass == origin.javaClass)) {
            logger.warn(
                "Trying to copy content from type {} to type {}.", origin.javaClass,
                this.javaClass
            )
            return
        }
        val other = origin as ToggleText
        // do copy
        this.title = other.title
        if (other.document != null) {
            val doc = Document()
            if (other.document.getMimeType() != null) {
                doc.mimeType = other.document.getMimeType()
            }

            if (other.document.getValue() != null) {
                val contentBytes = ByteArray(other.document.getValue().size)
                System.arraycopy(other.document.getValue(), 0, contentBytes, 0, other.document.getValue().size)
                doc.value = contentBytes
            }
            this.document = doc
        }
        this.isCollapsed = other.isCollapsed
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Text::class.java)
    }
}
