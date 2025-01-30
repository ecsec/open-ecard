/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.richclient.gui.components

import org.openecard.common.I18n
import org.openecard.gui.swing.common.GUIDefaults
import org.openecard.richclient.gui.manage.*
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Implements a dynamic gui element which is able to remove and add a file entry.
 *
 * @author Hans-Martin Haase
 */
class FileListEntryItem(fileType: String, property: String?, properties: Settings) :
    JPanel() {
    /**
     * I18n object which is necessary to retrieve translations.
     */
    private val lang: I18n = I18n.getTranslation("addon")

    /**
     * A list which contains all text entries .
     */
    private val entries: ArrayList<String> = ArrayList()

    /**
     * A list of JButtons.
     * The order of the buttons corresponds to the order in the `entries` list.
     */
    private val entryPlusBox: ArrayList<JButton> = ArrayList()

    /**
     * A list of JTextFields.
     * The order of text fields corresponds to the order in the `entries` list.
     */
    private val textEntries: ArrayList<JTextField> = ArrayList()

    /**
     * A semicolon separated list of accepted file types.
     */
    private val fileType: String

    /**
     * The name of the property which relates to this configuration entry.
     */
    private val property: String?

    /**
     * A Settings object which is used to retrieve and set the current value of the configuration entry.
     */
    private val properties: Settings


    /**
     * The current Y coordinate in the used GridBagLayout.
     */
    private var currentYCoordinate: Int = 0

    /**
     * Creates a new FileListEntryItem object.
     *
     * @param fileType A semicolon separated list of accepted file types.
     * @param property The name of the property which relates to this configuration entry.
     * @param properties A Settings object which is used to retrieve and set the current value of the configuration entry.
     */
    init {
        setLayout(GridBagLayout())
        this.fileType = fileType
        this.properties = properties
        this.property = property
        val props: String? = properties.getProperty(property)
        if (props != null && props != "") {
            for (entry: String in props.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                entries.add(entry)
                addRow(entry)
            }
        } else {
            addEmptyEntry()
        }
    }

    /**
     * Adds an new empty entry to the list of entries.
     */
    private fun addEmptyEntry() {
        addRow(null)
    }

    /**
     * Rebuilds the complete list of entries.
     *
     * This method have to be called if the remove button was pressed. The GridBagLayout is not able to remove rows in
     * a dynamic way only dynamic adding of rows is possible. So we have to clear the whole panel and rebuild the list
     * of entries.
     */
    private fun rebuild() {
        removeAll()
        textEntries.clear()
        entryPlusBox.clear()
        if (!entries.isEmpty()) {
            for (entry: String in entries) {
                addRow(entry)
            }
        } else {
            addEmptyEntry()
        }
        revalidate()
        repaint()
    }

    /**
     * Adds an additional row to the layout.
     *
     * The added row contains an TextField with the given `entry`. If `entry` is NULL an empty TextField is
     * add which is used to add a completely new list entry.
     *
     * @param entry The entry to add to the list.
     */
    private fun addRow(entry: String?) {
        val textField: JTextField = object : JTextField() {
            // the following is necessary because most path are longer than the field and GridBagLayout does not care
            // about MaximumSize. The following seems good for the default size of the configuration window but is does
            // not scale good if the size of the window becomes bigger.
            override fun getPreferredSize(): Dimension {
                val dim: Dimension = super.getPreferredSize()
                dim.width = 100
                return dim
            }
        }
        textField.setText(entry)
        textField.setToolTipText(textField.getText())
        textField.getDocument().addDocumentListener(DocumentChangeListener(textField))
        textEntries.add(textField)

        val browseButton: JButton = JButton(lang.translationForKey("addon.settings.browse"))
        browseButton.addActionListener(OpenFileBrowserListener(fileType, textField))

        // hide plus button of the previous line
        if (!entryPlusBox.isEmpty()) {
            val prevPlus: JButton = entryPlusBox.get(entryPlusBox.size - 1)
            prevPlus.setVisible(false)
        }

        val plus: JButton = JButton()
        plus.setIcon(closedIndicator)
        plus.addActionListener(AddActionListener(plus))
        plus.setOpaque(true)
        plus.setFocusPainted(false)
        plus.setBorder(EmptyBorder(Insets(0, 0, 0, 0)))
        plus.setHorizontalAlignment(SwingConstants.LEFT)
        plus.setMargin(Insets(0, 0, 0, 0))
        plus.setBounds(0, 0, 0, 0)
        plus.setContentAreaFilled(false)
        plus.setHorizontalTextPosition(SwingConstants.TRAILING)
        entryPlusBox.add(plus)

        val minus: JButton = JButton()
        minus.setIcon(openedIndicator)
        minus.addActionListener(RemoveActionListener(minus))
        // necessary to determine which entry to remove from the list.
        minus.setName(entry)
        minus.setOpaque(true)
        minus.setFocusPainted(false)
        minus.setBorder(EmptyBorder(Insets(0, 0, 0, 0)))
        minus.setHorizontalAlignment(SwingConstants.LEFT)
        minus.setMargin(Insets(0, 0, 0, 0))
        minus.setBounds(0, 0, 0, 0)
        minus.setContentAreaFilled(false)
        minus.setHorizontalTextPosition(SwingConstants.TRAILING)

        val textConstraint: GridBagConstraints = GridBagConstraints()
        textConstraint.anchor = GridBagConstraints.WEST
        textConstraint.fill = GridBagConstraints.HORIZONTAL
        textConstraint.gridx = 0
        textConstraint.gridy = currentYCoordinate
        textConstraint.weightx = 4.0

        val plusConstraint: GridBagConstraints = GridBagConstraints()
        plusConstraint.anchor = GridBagConstraints.WEST
        plusConstraint.fill = GridBagConstraints.NONE
        plusConstraint.gridx = 3
        plusConstraint.gridy = currentYCoordinate
        plusConstraint.gridwidth = GridBagConstraints.REMAINDER
        plusConstraint.insets = Insets(0, 5, 0, 5)

        val minusConstraint: GridBagConstraints = GridBagConstraints()
        minusConstraint.anchor = GridBagConstraints.WEST
        minusConstraint.fill = GridBagConstraints.NONE
        minusConstraint.gridx = 2
        minusConstraint.gridy = currentYCoordinate
        minusConstraint.insets = Insets(0, 5, 0, 5)

        val browseConstrain: GridBagConstraints = GridBagConstraints()
        browseConstrain.anchor = GridBagConstraints.WEST
        browseConstrain.fill = GridBagConstraints.NONE
        browseConstrain.gridx = 1
        browseConstrain.gridy = currentYCoordinate
        browseConstrain.insets = Insets(5, 5, 0, 5)

        add(textField, textConstraint)
        add(browseButton, browseConstrain)
        add(plus, plusConstraint)
        add(minus, minusConstraint)
        currentYCoordinate++
    }

    /**
     * An ActionListener implementation which triggers the addition of a new empty list entry.
     */
    private inner class AddActionListener
    /**
     * Creates a new AddActionListener object.
     *
     * @param addButton The button which is associated with this listener.
     */(
        /**
         * The button which is associated with this listener.
         */
        private val button: JButton
    ) : ActionListener {
        /**
         * Make the clicked plus button invisible an invoke the creation of a new empty entry.
         *
         * @param e
         */
        override fun actionPerformed(e: ActionEvent) {
            if (textEntries.get(textEntries.size - 1).getText() != "") {
                button.setVisible(false)
                addEmptyEntry()
            }
        }
    }

    /**
     * An ActionListener implementation which triggers the removal of a entry in the list.
     */
    private inner class RemoveActionListener
    /**
     * Creates a new RemoveActionListener object.
     *
     * @param button The button which is associated with this Listener.
     */(
        /**
         * The button which is associated with this Listener.
         */
        private val button: JButton
    ) : ActionListener {
        /**
         * Triggers the removal of the selected entry.
         *
         * @param e
         */
        override fun actionPerformed(e: ActionEvent) {
            val name: String = button.getName()
            val indexOfEntry: Int = entries.indexOf(name)
            if (indexOfEntry >= 0) {
                entryPlusBox.removeAt(indexOfEntry)
                val field: JTextField = textEntries[indexOfEntry]
				field.text = ""
                textEntries.removeAt(indexOfEntry)
                entries.remove(name)
            }
            rebuild()
        }
    }

    /**
     * Implements a DocumentListener which checks the correctness of the entered content.
     */
    private inner class DocumentChangeListener(
        /**
         * The TextField which is associated with this Listener.
         */
        private val textField: JTextField
    ) : DocumentListener {
        private var currentText: String

        init {
            currentText = textField.getText()
        }

        override fun insertUpdate(e: DocumentEvent) {
            concatContentsAndSetProperty()
            val entryIndex: Int = entries.indexOf(currentText)
            // update existing entry
            if (entryIndex >= 0) {
                entryPlusBox.get(entryIndex).setName(textField.getText())
                entries.set(entryIndex, textField.getText())
            } else {
                entries.add(textField.getText())
            }
            currentText = textField.getText()
        }

        override fun removeUpdate(e: DocumentEvent) {
            concatContentsAndSetProperty()
        }

        /**
         * Not implemented in this implementation so an changed update event is ignored.
         *
         * @param e
         */
        override fun changedUpdate(e: DocumentEvent) {
            // ignore
        }

        /**
         * Concatenates the content of all filled text fields.
         *
         * The different contents a separated by a semicolon and the resulting string is set as the new value of the
         * `property` property.
         */
        fun concatContentsAndSetProperty() {
            val sb: StringBuilder = StringBuilder()
            for (text: JTextField in textEntries) {
                val file: File = File(text.getText())
                if (file.exists() && file.isFile()) {
                    sb.append(text.getText())
                    sb.append(";")
                }
            }

            properties.setProperty(property, sb.toString())
        }
    }

    companion object {
        /**
         * Icon for the minus button which indicates the removal of an entry.
         */
        private val openedIndicator: Icon = GUIDefaults.getImage("ToggleText.selectedIcon")!!

        /**
         * Icon for the plus button which indicates the addition of a new empty row.
         */
        private val closedIndicator: Icon = GUIDefaults.getImage("ToggleText.icon")!!
    }
}
