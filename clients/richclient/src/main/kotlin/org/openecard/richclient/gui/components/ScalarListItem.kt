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

import org.openecard.addon.manifest.ScalarListEntryType
import org.openecard.gui.swing.common.GUIDefaults
import org.openecard.richclient.gui.manage.*
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat

import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * This class models a graphical representation of a ScalarListEntry from the add-on manifest specification.
 *
 * @author Hans-Martin Haase
 */
class ScalarListItem(property: String, properties: Settings, type: ScalarListEntryType) :
    JPanel() {
    /**
     * An ArrayList which contains all JTextFields used on the GUI.
     */
    private var textFieldList: ArrayList<JTextField>? = null

    /**
     * An ArrayList containing all JSpinners used on the GUI.
     */
    private var spinnerList: ArrayList<JSpinner>? = null

    /**
     * The type of the ScalarListEntry.
     */
    private var type: ScalarListEntryType? = null

    /**
     * Setting object which contains the value of the property [ScalarListItem.property].
     */
    private val properties: Settings

    /**
     * The property which is managed by this ScalarListItem.
     */
    private val property: String


    /**
     * The current number of rows in this ScalarListItem.
     */
    private var currentRow: Int = 0

    /**
     * A reference to the last plus button.
     */
    private var lastPlus: JButton? = null


    /**
     * Creates a new ScalarListItem from the given `property` and `type`.
     *
     * @param property The property which is represented by this ScalarListEntry.
     * @param properties Settings object which manages the setting of changed property values in the configuration file.
     * @param type The type of the ScalarListEntry which is managed by this ScalarListItem.
     */
    init {
        if (type == ScalarListEntryType.BIGDECIMAL) {
            this.type = ScalarListEntryType.BIGDECIMAL
        } else if (type == ScalarListEntryType.BIGINTEGER) {
            this.type = ScalarListEntryType.BIGINTEGER
        } else {
            this.type = ScalarListEntryType.STRING
        }

        // basic variable and set layout
        this.properties = properties
        this.property = property
        setLayout(GridBagLayout())

        // initialize the managed list dependent on the type.
        if (type == ScalarListEntryType.BIGDECIMAL || type == ScalarListEntryType.BIGINTEGER) {
            spinnerList = ArrayList()
            textFieldList = null
        } else {
            textFieldList = ArrayList()
            spinnerList = null
        }

        // fill the GUI with stored entries
        val entriesList: String? = properties.getProperty(property)
        if (entriesList != null) {
            val singleEntries: Array<String> =
                entriesList.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (entry: String? in singleEntries) {
                addRow(entry)
            }
        } else {
            addEmptyRow()
        }
    }

    /**
     * Add an empty row to the layout.
     */
    private fun addEmptyRow() {
        addRow(null)
    }

    /**
     * Add an entry with the value `entry` to the layout.
     * If `entry` is NULL an empty entry is created.
     *
     * @param entry Value to set in the spinner or text field.
     */
    private fun addRow(entry: String?) {
        var item: JComponent? = null
        when (type) {
            ScalarListEntryType.BIGDECIMAL, ScalarListEntryType.BIGINTEGER -> {
                val spinner: JSpinner = createNumberEntry(entry)
                spinnerList!!.add(spinner)
                item = spinner
            }

            ScalarListEntryType.STRING -> {
                val field: JTextField = createStringEntry(entry)
                textFieldList!!.add(field)
                item = field
            }

			else -> {}
        }

        addComponent(item!!)
    }

    /**
     * Adds a JSpinner or JTextField to the layout.
     *
     * @param item The element to place on the panel.
     */
    private fun addComponent(item: JComponent) {
        // create a new plus button and hide the previous one
        val plusButton: JButton = createButton(true, item)
        if (lastPlus != null) {
            lastPlus!!.setVisible(false)
        }
        lastPlus = plusButton

        // create minus button
        val minusButton: JButton = createButton(false, item)

        // GridBagConstraints for the item to add
        val componentConstraints: GridBagConstraints = GridBagConstraints()
        componentConstraints.anchor = GridBagConstraints.WEST
        componentConstraints.fill = GridBagConstraints.HORIZONTAL
        componentConstraints.gridx = 0
        componentConstraints.gridy = currentRow
        componentConstraints.weightx = 1.0
        add(item, componentConstraints)

        // Constraint for the plus button
        val plusConstraints: GridBagConstraints = GridBagConstraints()
        plusConstraints.anchor = GridBagConstraints.WEST
        plusConstraints.fill = GridBagConstraints.NONE
        plusConstraints.gridx = 2
        plusConstraints.gridy = currentRow
        add(plusButton, plusConstraints)

        // constraint for the minus button
        val minusConstraints: GridBagConstraints = GridBagConstraints()
        minusConstraints.anchor = GridBagConstraints.WEST
        minusConstraints.fill = GridBagConstraints.NONE
        minusConstraints.gridx = 1
        minusConstraints.gridy = currentRow
        minusConstraints.insets = Insets(0, 10, 0, 10)
        add(minusButton, minusConstraints)

        // increase the currentRow to place the next entry in the next row
        currentRow++
    }

    /**
     * Create a plus or minus button dependent on the given `isAdd` variable.
     *
     * @param isAdd boolean variable which indicates whether to create a plus or minus button. A plus button is created
     * if the variable is `true` else a minus button is created.
     * @param item The item which is associated with this button.
     * @return A [JButton] dependent on the input parameters.
     */
    private fun createButton(isAdd: Boolean, item: JComponent): JButton {
        val button: JButton = JButton()
        if (isAdd) {
            button.setIcon(closedIndicator)
            button.addActionListener(AddRowListener(button, item))
        } else {
            button.setIcon(openedIndicator)
            button.addActionListener(RemoveRowListener(item))
        }

        button.setOpaque(true)
        button.setFocusPainted(false)
        button.setBorder(EmptyBorder(Insets(0, 0, 0, 0)))
        button.setHorizontalAlignment(SwingConstants.LEFT)
        button.setMargin(Insets(0, 0, 0, 0))
        button.setBounds(0, 0, 0, 0)
        button.setContentAreaFilled(false)
        button.setHorizontalTextPosition(SwingConstants.TRAILING)
        return button
    }

    /**
     * Creates a JSpinner which is able to handle BigInteger or BigDecimal entries.
     *
     * @param value The initial value of the spinner.
     * @return A JSpinner which displays the value `value`.
     */
    private fun createNumberEntry(value: String?): JSpinner {
        val spinner: JSpinner = object : JSpinner() {
            override fun getPreferredSize(): Dimension {
                val dim: Dimension = super.getPreferredSize()
                dim.width = 100
                return dim
            }
        }
        if (type == ScalarListEntryType.BIGDECIMAL) {
            val `val`: BigDecimal
            if (value == null || value == "") {
                `val` = BigDecimal("0")
            } else {
                `val` = BigDecimal(value)
            }
            spinner.setModel(SpinnerMathNumberModel(`val`, null, null, BigDecimal("0.1")))
        } else {
            val `val`: BigInteger
            if (value == null || value == "") {
                `val` = BigInteger("0")
            } else {
                `val` = BigInteger(value, 10)
            }
            spinner.setModel(SpinnerMathNumberModel(`val`, null, null, BigInteger("1")))
        }
        spinner.setEditor(MathNumberEditor(spinner, DecimalFormat.getInstance(spinner.getLocale()) as DecimalFormat?))
        spinner.addChangeListener(SpinnerValueChangedListener())
        return spinner
    }

    /**
     * Creates a JTextField from the given `value`.
     *
     * @param value A text to display in the [JTextField].
     * @return A [JTextField] which displayes the value.
     */
    private fun createStringEntry(value: String?): JTextField {
        val input: JTextField = object : JTextField(value) {
            override fun getPreferredSize(): Dimension {
                val dim: Dimension = super.getPreferredSize()
                dim.width = 100
                return dim
            }
        }
        // add listener for value changes
        input.getDocument().addDocumentListener(DocumentJoinListener())
        return input
    }

    /**
     * Creates a JCheckBox which represents a boolean type ScalarListEntry.
     *
     * @param value Indicator whether the JCheckBox is checked or not.
     * @return A [JCheckBox] depending on the `value`.
     */
    private fun createBooleanEntry(value: String?): JCheckBox {
        val box: JCheckBox = JCheckBox()

        if (value != null && value.equals("true", ignoreCase = true)) {
            box.setSelected(true)
        } else {
            box.setSelected(false)
        }

        box.addItemListener(object : ItemListener {
            override fun itemStateChanged(e: ItemEvent) {
                properties.setProperty(property, box.isSelected().toString())
            }
        })

        return box
    }

    /**
     * Rebuilds the complete panel for the case a row have to be removed.
     * Necessary because the GridBagLayout does not allow to remove a row directly.
     */
    private fun rebuild() {
        removeAll()
        currentRow = 0
        if (textFieldList == null) {
            spinnerList!!.clear()
        } else {
            textFieldList!!.clear()
        }
        val props: String? = properties.getProperty(property)
        if (props != null && props != "") {
            val props2: Array<String> = props.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (entry: String in props2) {
                addRow(entry)
            }
        } else {
            addEmptyRow()
        }

        revalidate()
        repaint()
    }

    /**
     * DocumentListener implementation which manages value changes in a JTextField of a ScalarListItem.
     */
    private inner class DocumentJoinListener : DocumentListener {
        override fun insertUpdate(e: DocumentEvent) {
            properties.setProperty(property, joinContent())
        }

        override fun removeUpdate(e: DocumentEvent) {
            properties.setProperty(property, joinContent())
        }

        override fun changedUpdate(e: DocumentEvent) {
            // ignore
        }

        /**
         * Joins the content of all JTextField on the GUI.
         *
         * @return A String of semicolon separated strings gathered from all TextField on the GUI.
         */
        fun joinContent(): String {
            val sb: StringBuilder = StringBuilder()
            for (field: JTextField in textFieldList!!) {
                if (field.getText() != null && field.getText() != "") {
                    sb.append(field.getText())
                    sb.append(";")
                }
            }

            return sb.toString()
        }
    }

    /**
     * ChangeListener implementation which manages the merging and joining of the JSpinner on a ScalarListItem.
     */
    private inner class SpinnerValueChangedListener : ChangeListener {
        override fun stateChanged(e: ChangeEvent) {
            val sb: StringBuilder = StringBuilder()
            for (spinner: JSpinner in spinnerList!!) {
                if (type == ScalarListEntryType.BIGDECIMAL) {
                    val dec: BigDecimal = spinner.getModel().getValue() as BigDecimal
                    sb.append(dec.toPlainString())
                } else {
                    val inte: BigInteger = spinner.getModel().getValue() as BigInteger
                    sb.append(inte.toString(10))
                }
                sb.append(";")
            }

            properties.setProperty(property, sb.toString())
        }
    }

    /**
     * ActionListener implementation which dynamically adds a new JTextField or JSpinner to the layout.
     */
    private inner class AddRowListener
    /**
     * Creates a new AddRowListener dependent on the given components.
     *
     * @param button The button which is associated with this Listener.
     * @param field
     */(private val button: JButton, private val field: JComponent) : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            if (textFieldList != null && (field as JTextField).getText() != "") {
                addEmptyRow()
                button.setVisible(false)
            } else if (spinnerList != null && properties.getProperty(property) != null) {
                addEmptyRow()
                button.setVisible(false)
            }
        }
    }

    /**
     * ActionListener implementation which removes a row from the layout.
     */
    private inner class RemoveRowListener
    /**
     * Creates a  new RemoveRowListener dependent on the input `item`.
     *
     * @param item A JComponent which shall be removed. This a have to be a JSpinner or a JTextField.
     */(private val item: JComponent) : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            if (properties.getProperty(property) != null) {
                if (textFieldList == null) {
                    if (type == ScalarListEntryType.BIGDECIMAL) {
                        val currentVal: String =
                            ((item as JSpinner).getModel().getValue() as BigDecimal).toPlainString() + ";"
                        var props: String? = properties.getProperty(property)
                        props = props!!.replaceFirst(currentVal.toRegex(), "")
                        properties.setProperty(property, props)
                    } else {
                        val currentVal: String =
                            ((item as JSpinner).getModel().getValue() as BigInteger).toString(10) + ";"
                        var props: String? = properties.getProperty(property)
                        props = props!!.replaceFirst(currentVal.toRegex(), "")
                        properties.setProperty(property, props)
                    }
                    spinnerList!!.remove(item)
                } else {
                    (item as JTextField).setText("")
                    textFieldList!!.remove(item)
                }
            }
            rebuild()
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
