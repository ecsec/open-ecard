/****************************************************************************
 * Copyright (C) 2013-2015 ecsec GmbH.
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

import org.openecard.addon.AddonPropertiesException
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.IOException
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants

/**
 * Panel aggregating several setting group entries.
 * The entries are of type [SettingsGroup].
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
abstract class SettingsPanel : JPanel() {
    private val groups: ArrayList<SettingsGroup> = ArrayList()
	private val contentPane: JPanel = JPanel()

    /**
     * Creates a panel instance.
     */
    init {
		contentPane.setLayout(GridBagLayout())
        setLayout(GridBagLayout())
        var c: GridBagConstraints = GridBagConstraints()
        val scrollPane: JScrollPane = JScrollPane(contentPane)
        scrollPane.setBorder(BorderFactory.createEmptyBorder())
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED)
        scrollPane.getVerticalScrollBar().setValue(0)
        scrollPane.getVerticalScrollBar().setBlockIncrement(16)
        scrollPane.getVerticalScrollBar().setUnitIncrement(16)
        c.fill = GridBagConstraints.BOTH
        c.gridwidth = GridBagConstraints.REMAINDER
        c.weightx = 1.0
        c.weighty = 1.0
        c.anchor = GridBagConstraints.NORTHWEST
        add(scrollPane, c)

        // create a filler which is always at the end of the panel
        c = GridBagConstraints()
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridwidth = GridBagConstraints.REMAINDER
        c.weightx = 1.0
        c.weighty = 1.0
        c.anchor = GridBagConstraints.NORTHWEST
        c.insets = Insets(10, 0, 0, 0)
        contentPane.add(JPanel(), c)
    }

    /**
     * Adds a settings group to this panel.
     *
     * @param item The group to add to the panel.
     */
    protected fun addSettingsGroup(item: SettingsGroup) {
        val c: GridBagConstraints = GridBagConstraints()
        c.fill = GridBagConstraints.HORIZONTAL
        c.gridwidth = GridBagConstraints.REMAINDER
        c.weightx = 1.0
        c.weighty = 0.0
        c.anchor = GridBagConstraints.NORTHWEST
        c.insets = Insets(10, 0, 0, 0)
        contentPane.add(item, c, groups.size)
        groups.add(item)
    }

    /**
     * Saves all settings groups of this panel.
     *
     * @throws IOException Thrown in case the properties could not be written to the output device.
     * @throws SecurityException Thrown in case the permission to save the properties is missing.
     * @throws org.openecard.addon.AddonPropertiesException Thrown in case the AddonProperties object throws this
     * exception in the store method.
     */
    @Throws(IOException::class, SecurityException::class, AddonPropertiesException::class)
    fun saveProperties() {
        for (next: SettingsGroup in groups) {
            next.saveProperties()
        }
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
