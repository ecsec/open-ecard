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

import java.awt.BorderLayout
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.AbstractListModel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener

/**
 * Data model for the add-on selection lists on the ManagementDialog.
 * The data model manages the entries as well as the functionality to replace logo and addon page on the dialog.
 * The class implements various listeners, so that the model can be used as listener in the list and dialog.
 * The events processed are [.valueChanged] and
 * [.windowClosed]. The listener then saves the currently open settings page and
 * changes the displayed add-on panel if applicable.
 *
 * @author Tobias Wich
 */
class AddonSelectionModel(
    private val dialog: ManagementDialog,
    private val container: JPanel
) :
    AbstractListModel<String>(), ListSelectionListener, WindowListener {
    private var idxCounter: Int = 0

    private val names: HashMap<Int, String> = HashMap()
	private val addons: HashMap<Int, AddonPanel> = HashMap()
	private val addonClasses: HashMap<Int, Class<AddonPanel>> = HashMap()

	private var lastActivePanel: AddonPanel? = null

	/**
     * Adds an add-on element to the model.
     *
     * @param name Name displayed in the list.
     * @param addonPanel Panel displayed, when the item is selected.
     */
    @Synchronized
    fun addElement(name: String, addonPanel: AddonPanel) {
        if (!names.containsValue(name)) {
			names[idxCounter] = name
			addons[idxCounter] = addonPanel
			idxCounter++
        }
    }

    override fun getSize(): Int {
        return names.size
    }

    override fun getElementAt(index: Int): String? {
        return names[index]
    }

    override fun valueChanged(e: ListSelectionEvent) {
        val source: Any = e.source
        if (!e.valueIsAdjusting && source is JList<*>) {
            // save last displayed component
            saveLastDialog()
            // load other panel if an index is selected
            val idx: Int = source.selectedIndex
            if (idx >= 0) {
                val panel = getPanel(idx)
                lastActivePanel = panel
                container.removeAll()
                container.add(panel, BorderLayout.CENTER)
                // invalidate component, else it won't be redrawn
                container.invalidate()
                container.validate()
                container.repaint()
                // update icon in management panel
                dialog.setLogo(panel.getLogo())
            }
        }
    }

    @Synchronized
    private fun getPanel(idx: Int): AddonPanel {
        // TODO: load panel from class
        return addons[idx] ?: throw IllegalArgumentException("No panel found for index $idx")
    }

    private fun saveLastDialog() {
        if (lastActivePanel != null) {
            lastActivePanel!!.saveProperties()
            lastActivePanel = null
        }
    }


    override fun windowOpened(e: WindowEvent) {
        // ignore
    }

    override fun windowClosing(e: WindowEvent) {
        // ignore
    }

    override fun windowClosed(e: WindowEvent) {
        saveLastDialog()
    }

    override fun windowIconified(e: WindowEvent) {
        // ignore
    }

    override fun windowDeiconified(e: WindowEvent) {
        // ignore
    }

    override fun windowActivated(e: WindowEvent) {
        // ignore
    }

    override fun windowDeactivated(e: WindowEvent) {
        // ignore
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
