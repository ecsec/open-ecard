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
 ***************************************************************************/

package org.openecard.client.connector.common;


/**
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public enum MimeType {

    TEXT_PLAIN("txt", "text/plain"),
    TEXT_HTML("html", "text/html"),
    TEXT_CSS("css", "text/css"),
    TEXT_XML("xml", "text/xml"),

    IMAGE_ICO("ico", "image/vnd.microsoft.icon"),
    IMAGE_PNG("png", "image/png"),
    IMAGE_JPEG("jpeg", "image/jpeg"),
    IMAGE_GIF("gif", "image/gif"),

    APPLICATION_JS("js", "application/javascript");

    private String filenameExtension;
    private String mimeType;

    private MimeType(String filenameExtension, String mimeType) {
	this.filenameExtension = filenameExtension;
	this.mimeType = mimeType;
    }


    /**
     * Returns the FilenameExtension.
     *
     * @return FilenameExtension
     */
    public String getFilenameExtension() {
	return filenameExtension;
    }

    /**
     * Returns the MimeType.
     *
     * @return MimeType
     */
    public String getMimeType() {
	return mimeType;
    }

    /**
     * Creates a new MineType from a String.
     *
     * @param filenameExtension FilenameExtension
     * @return MimeType
     */
    public static MimeType fromFilenameExtension(String filenameExtension) {
	for (MimeType item : MimeType.values()) {
	    if (item.getFilenameExtension().equals(filenameExtension)) {
		return item;
	    }
	}

	return null;
    }

    /**
     * Creates a new MimeType from a String.
     *
     * @param mimeType MimeType
     * @return MimeType
     */
    public MimeType fromMineType(String mimeType) {
	for (MimeType item : MimeType.values()) {
	    if (item.getMimeType().equals(mimeType)) {
		return item;
	    }
	}

	return null;
    }

}
