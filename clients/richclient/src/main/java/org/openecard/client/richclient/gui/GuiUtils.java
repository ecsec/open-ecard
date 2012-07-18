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

package org.openecard.client.richclient.gui;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;


/**
 *
 * @author Johannes Schmölz <johannes.schmoelz@ecsec.de>
 */
public class GuiUtils {

    public static ImageIcon getImageIcon(String name) {
        URL imageUrl = GuiUtils.class.getResource("images/" + name);
        if (imageUrl == null) {
            imageUrl = GuiUtils.class.getResource("/images/" + name);
        }

        ImageIcon icon = new ImageIcon(imageUrl);
        return icon;
    }

    public static Image getImage(String name) {
        ImageIcon icon = getImageIcon(name);
        return icon.getImage();
    }

}
