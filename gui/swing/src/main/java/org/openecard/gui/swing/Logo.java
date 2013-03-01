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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.openecard.common.util.FileUtils;


/**
 * Open eCard logo for the sidebar of the Swing GUI.
 * The logo is placed on a JPanel, so that it can be placed on any component conveniently.
 *
 * @author Moritz Horsch <horsch@cdc.informatik.tu-darmstadt.de>
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class Logo extends JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Load logo from classpath and instantiate panel.
     */
    public Logo() {
	ImageIcon logo = new ImageIcon();
	URL url = FileUtils.resolveResourceAsURL(Logo.class, "openecard_logo.png");

	if (url != null) {
	    Toolkit toolkit = Toolkit.getDefaultToolkit();
	    Image image = toolkit.getImage(url);
	    image = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
	    logo.setImage(image);
	}

	// set a horizontal box layout (logo on the left, separator on the right)
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	// add the logo
	JLabel lbl = new JLabel(logo);
	add(lbl);

	// add the panel containing the separator
	JPanel separatorPanel = new JPanel();
	// set a vertical box layout (dummy on the top, separator on the bottom)
	separatorPanel.setLayout(new BoxLayout(separatorPanel, BoxLayout.Y_AXIS));
	JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
	JPanel dummyPanel = new JPanel();
	dummyPanel.setPreferredSize(new Dimension(10, 60));
	separatorPanel.add(dummyPanel);
	separatorPanel.add(separator);
	add(separatorPanel);

	// add a space of 10 at the bottom
	setBorder(new EmptyBorder(0, 0, 10, 0));
    }

}
