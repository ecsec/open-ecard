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

package org.openecard.gui.definition;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * GUI component class which represents an image.
 * In order to use the component, at least an image must be set. The MIME type is optional, but may be needed by some
 * GUI implementations.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public final class ImageBox extends IDTrait implements InputInfoUnit {

    private static final Logger logger = LoggerFactory.getLogger(ImageBox.class);

    private byte[] imageData;
    private String mimeType;

    /**
     * Get the raw data of the image.
     *
     * @see #setImageData(byte[])
     * @return The raw image data.
     */
    public byte[] getImageData() {
	return imageData;
    }
    /**
     * Sets the raw image data for this instance.
     * The image must be given in the serialized form of an image container format such as PNG, JPEG, etc.
     *
     * @param imageData The raw image data.
     */
    public void setImageData(byte[] imageData) {
	this.imageData = imageData;
    }

    /**
     * Gets the MIME type for the image represented by this instance.
     *
     * @see #setMimeType(java.lang.String)
     * @return String containing the MIME type.
     */
    public String getMimeType() {
	return mimeType;
    }
    /**
     * Sets the MIME type for the image represented by this instance.
     *
     * @See <a href="https://www.iana.org/assignments/media-types/">IANA Registered MIME Types</a>
     * @See <a href="https://www.iana.org/assignments/media-types/image/">IANA Registered Image MIME Types</a>
     * @param mimeType MIME type describing the image type.
     */
    public void setMimeType(String mimeType) {
	this.mimeType = mimeType;
    }


    @Override
    public InfoUnitElementType type() {
	return InfoUnitElementType.IMAGE_BOX;
    }

    @Override
    public void copyContentFrom(InfoUnit origin) {
	if (! (this.getClass().equals(origin.getClass()))) {
	    logger.warn("Trying to copy content from type {} to type {}.", origin.getClass(), this.getClass());
	    return;
	}
	ImageBox other = (ImageBox) origin;
	// do copy
	if (other.imageData != null) {
	    this.imageData = Arrays.copyOf(other.imageData, other.imageData.length);
	}
	this.mimeType = other.mimeType;
    }

}
