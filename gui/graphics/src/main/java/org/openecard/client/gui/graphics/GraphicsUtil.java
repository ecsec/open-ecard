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

package org.openecard.client.gui.graphics;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 * Utility class for creating images.
 * 
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class GraphicsUtil {
    
    /**
     * Creates an image of the Open eCard logo with the specified width and height. </ br>
     * The background color of the image will be transparent, while the background color
     * of the logo will be white.
     * 
     * @param logoWidth logo width
     * @param logoHeight logo height
     * @return Open eCard logo as java.awt.Image
     */
    public static Image createOpenECardLogo(int logoWidth, int logoHeight) {
        return createOpenECardLogo(logoWidth, logoHeight, logoWidth, logoHeight, 0, 0, false);
    }
    
    /**
     * Creates an image of the Open eCard logo with the specified width and height. </ br>
     * The background color of the image will be transparent, while the background color
     * of the logo will be either transparent, if <b>transparent</b> is set to <b>true</b>,
     * or white, if <b>transparent</b> is set to <b>false</b>.
     * 
     * @param logoWidth logo width
     * @param logoHeight logo height
     * @param transparent background color of the logo (transparent or white)
     * @return Open eCard logo as java.awt.Image
     */
    public static Image createOpenECardLogo(int logoWidth, int logoHeight, boolean transparent) {
        return createOpenECardLogo(logoWidth, logoHeight, logoWidth, logoHeight, 0, 0, transparent);
    }
    
    /**
     * Creates an image of the Open eCard logo with the specified width and height. </ br>
     * The size of the logo can be defined separately via <b>logoWidth</b> and <b>logoHeight</b>.
     * The position of the logo within the image can be defined via <b>posX</b> and <b>posY</b>,
     * with <b>posX</b> specifying the x coordinate and <b>posY</b> specifying the y coordinate
     * of the upper left corner of the logo.
     * The background color of the image will be transparent, while the background color
     * of the logo will be white.
     * 
     * @param logoWidth logo width
     * @param logoHeight logo height
     * @param imageWidth image width
     * @param imageHeight image height
     * @param posX x coordinate of the upper left corner of the logo
     * @param posY y coordinate of the upper left corner of the logo
     * @return Open eCard logo as java.awt.Image
     */
    public static Image createOpenECardLogo(int logoWidth, int logoHeight, int imageWidth, int imageHeight, int posX, int posY) {
        return createOpenECardLogo(logoWidth, logoHeight, imageWidth, imageHeight, posX, posY, false);
    }
    
    /**
     * Creates an image of the Open eCard logo with the specified width and height. </ br>
     * The size of the logo can be defined separately via <b>logoWidth</b> and <b>logoHeight</b>.
     * The position of the logo within the image can be defined via <b>posX</b> and <b>posY</b>,
     * with <b>posX</b> specifying the x coordinate and <b>posY</b> specifying the y coordinate
     * of the upper left corner of the logo.
     * The background color of the image will be transparent, while the background color
     * of the logo will be either transparent, if <b>transparent</b> is set to <b>true</b>,
     * or white, if <b>transparent</b> is set to <b>false</b>.
     * 
     * @param logoWidth logo width
     * @param logoHeight logo height
     * @param imageWidth image width
     * @param imageHeight image height
     * @param posX x coordinate of the upper left corner of the logo
     * @param posY y coordinate of the upper left corner of the logo
     * @param transparent background color of the logo (transparent or white)
     * @return Open eCard logo as java.awt.Image
     */
    public static Image createOpenECardLogo(int logoWidth, int logoHeight, int imageWidth, int imageHeight, int posX, int posY, boolean transparent) {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        
        if (transparent) {
            OecLogoBgTransparent icon = new OecLogoBgTransparent();
            icon.setDimension(new Dimension(logoWidth, logoHeight));
            icon.paintIcon(null, g, posX, posY);
        } else {
            OecLogoBgWhite icon = new OecLogoBgWhite();
            icon.setDimension(new Dimension(logoWidth, logoHeight));
            icon.paintIcon(null, g, posX, posY);
        }
        
        return image;
    }
}
