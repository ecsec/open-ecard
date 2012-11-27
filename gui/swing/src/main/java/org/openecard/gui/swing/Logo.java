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

package org.openecard.gui.swing;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openecard.common.util.FileUtils;


/**
 * Open eCard logo for the sidebar of the Swing GUI.
 * The logo is placed on a JPanel, so that it can be placed on any component conveniently.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 */
public class Logo extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Load logo from classpath and instantiate panel.
     */
    public Logo() {
	ImageIcon logo = new ImageIcon();
	URL url = FileUtils.resolveResourceAsURL(Logo.class, "openecardwhite.gif");

	if (url != null) {
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Image image = toolkit.getImage(url);
	    image = image.getScaledInstance(45, 45, Image.SCALE_SMOOTH);
	    logo.setImage(image);
	}

	add(new JLabel(logo));
    }

}
