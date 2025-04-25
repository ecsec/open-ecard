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
package org.openecard.control.binding.http.common


/**
 * @author Moritz Horsch
 */
enum class MimeType(
    /**
     * Returns the FilenameExtension.
     *
     * @return FilenameExtension
     */
    val filenameExtension: String,
    /**
     * Returns the MimeType.
     *
     * @return MimeType
     */
    val mimeType: String
) {
    TEXT_PLAIN("txt", "text/plain"),
    TEXT_HTML("html", "text/html"),
    TEXT_CSS("css", "text/css"),
    TEXT_XML("xml", "text/xml"),

    IMAGE_ICO("ico", "image/vnd.microsoft.icon"),
    IMAGE_PNG("png", "image/png"),
    IMAGE_JPEG("jpeg", "image/jpeg"),
    IMAGE_GIF("gif", "image/gif"),

    APPLICATION_JS("js", "application/javascript");


    /**
     * Creates a new MimeType from a String.
     *
     * @param mimeType MimeType
     * @return MimeType
     */
    fun fromMineType(mimeType: String): MimeType? {
        for (item in entries) {
            if (item.mimeType == mimeType) {
                return item
            }
        }

        return null
    }

    companion object {
        /**
         * Creates a new MineType from a String.
         *
         * @param filenameExtension FilenameExtension
         * @return MimeType
         */
        fun fromFilenameExtension(filenameExtension: String): MimeType? {
            for (item in entries) {
                if (item.filenameExtension == filenameExtension) {
                    return item
                }
            }

            return null
        }
    }
}
