/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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

package org.openecard.richclient.gui.manage

import java.awt.Component
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JPanel

/**
 * Panel aggregating several action entries.
 * The entries are of type [ActionEntryPanel].
 *
 * @author Tobias Wich
 */
class ActionPanel : JPanel() {
    private val glue: Component

    /**
     * Creates a panel instance.
     */
    init {
        setLayout(BoxLayout(this, BoxLayout.Y_AXIS))
        setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0))

        val space: Component = Box.createRigidArea(Dimension(20, 10))
        add(space)

        glue = Box.createGlue()
        add(glue)
    }

    /**
     * Adds an action entry to this panel.
     *
     * @param actionEntry Entry to add to the panel.
     */
    fun addActionEntry(actionEntry: ActionEntryPanel) {
        remove(glue)

        actionEntry.setAlignmentY(TOP_ALIGNMENT)
        actionEntry.setAlignmentX(LEFT_ALIGNMENT)
        add(actionEntry)

        val rigidArea: Component = Box.createRigidArea(Dimension(20, 20))
        add(rigidArea)

        add(glue)
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
