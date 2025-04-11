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
package org.openecard.gui.swing

import org.openecard.common.util.FileUtils.resolveResourceAsURL
import java.awt.Dimension
import java.awt.Image
import java.awt.Toolkit
import javax.swing.*
import javax.swing.border.EmptyBorder

/**
 * Open eCard logo for the sidebar of the Swing GUI.
 * The logo is placed on a JPanel, so that it can be placed on any component conveniently.
 *
 * @author Moritz Horsch
 * @author Dirk Petrautzki
 */
class Logo : JPanel() {
    /**
     * Load logo from classpath and instantiate panel.
     */
    init {
        val logo: ImageIcon = loadLogoIcon()
        val lbl = JLabel(logo)

        // set a horizontal box layout (logo on the left, separator on the right)
        setLayout(BoxLayout(this, BoxLayout.Y_AXIS))
        add(lbl)

        // add the panel containing the separator
        val separatorPanel = JPanel()
        // set a vertical box layout (dummy on the top, separator on the bottom)
        separatorPanel.setLayout(BoxLayout(separatorPanel, BoxLayout.Y_AXIS))
        val separator = JSeparator(SwingConstants.HORIZONTAL)
		val dummyPanel = JPanel()
		dummyPanel.preferredSize = Dimension(10, 6)
        separatorPanel.add(dummyPanel)
        separatorPanel.add(separator)
        add(separatorPanel)

        // add a space of 10 at the bottom
        setBorder(EmptyBorder(0, 0, 10, 0))
    }

    companion object {
        private const val serialVersionUID = 1L

        fun loadLogoIcon(): ImageIcon {
            val logo = ImageIcon()
            val url = resolveResourceAsURL(Logo::class.java, "openecard_logo.png")

            if (url != null) {
                val toolkit = Toolkit.getDefaultToolkit()
                var image = toolkit.getImage(url)
                image = image.getScaledInstance(60, 60, Image.SCALE_SMOOTH)
                logo.setImage(image)
            }

            return logo
        }
    }
}
