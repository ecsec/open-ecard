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

import io.github.oshai.kotlinlogging.KotlinLogging
import org.openecard.addon.AddonPropertiesException
import org.openecard.addon.manifest.ScalarEntryType
import org.openecard.addon.manifest.ScalarListEntryType
import org.openecard.i18n.I18N
import org.openecard.richclient.gui.components.CheckboxListItem
import org.openecard.richclient.gui.components.FileListEntryItem
import org.openecard.richclient.gui.components.MathNumberEditor
import org.openecard.richclient.gui.components.OpenFileBrowserListener
import org.openecard.richclient.gui.components.ScalarListItem
import org.openecard.richclient.gui.components.SpinnerMathNumberModel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JSpinner
import javax.swing.JTextField
import javax.swing.border.Border
import javax.swing.border.TitledBorder
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

private val LOG = KotlinLogging.logger { }

/**
 * Aggregator class for settings entries.
 * The entries form a group with an optional caption.
 *
 * @author Tobias Wich
 * @author Hans-Martin Haase
 */
open class SettingsGroup(
	title: String?,
	protected val properties: Settings,
) : JPanel() {
	protected val container: JPanel
	private val fieldLabels: MutableMap<Component, JLabel> = mutableMapOf()
	protected var itemIdx: Int = 0

	/**
	 * Creates an instance bound to a set of properties.
	 *
	 * @param title Optional title to display as group caption.
	 * @param settings Settings object which wraps a Properties object or an AddonProperties object.
	 */
	init {

		var frameBorder: Border = BorderFactory.createEmptyBorder(2, 2, 2, 2)
		if (title != null) {
			val titleBorder: TitledBorder = BorderFactory.createTitledBorder(frameBorder, title)
			titleBorder.setTitleJustification(TitledBorder.LEADING)
			titleBorder.setTitlePosition(TitledBorder.TOP)
			titleBorder.setTitleFont(JLabel().getFont().deriveFont(Font.BOLD))
			frameBorder = titleBorder
		}
		setBorder(frameBorder)
		setLayout(BorderLayout())

		// configure tuple container
		container = JPanel()
		add(container, BorderLayout.NORTH)
		val layout: GridBagLayout = GridBagLayout()
		layout.columnWidths = intArrayOf(0, 10, 0, 0)
		layout.rowHeights = intArrayOf(0, 0)
		layout.columnWeights = doubleArrayOf(0.0, 0.0, 1.0, Double.MIN_VALUE)
		layout.rowWeights = doubleArrayOf(0.0, Double.MIN_VALUE)
		container.setLayout(layout)
	}

	/**
	 * Saves the bound properties.
	 * This method is abstract, so that the overriding class can decide how and where to save the properties.
	 *
	 * @throws IOException Thrown in case the properties could not be written to the output device.
	 * @throws SecurityException Thrown in case the permission to save the properties is missing.
	 * @throws org.openecard.addon.AddonPropertiesException
	 */
	@Throws(IOException::class, SecurityException::class, AddonPropertiesException::class)
	open fun saveProperties() {
		properties.store()
	}

	/**
	 * Enables or disables entries in the group.
	 * The entry is identified by its input field which is returned in the add functions. When disabled the entry
	 * disappears.
	 *
	 * @param element Input element identifying the entry.
	 * @param enabled True whe element should be enabled, false otherwise.
	 * @see .addInputItem
	 * @see .addBoolItem
	 * @see .addSelectionItem
	 * @see .addMultiSelectionItem
	 */
	protected fun setEnabledComponent(
		element: Component,
		enabled: Boolean,
	) {
		val label: JLabel? = fieldLabels.get(element)
		label!!.setVisible(enabled)
		element.setVisible(enabled)
	}

	/**
	 * Adds an input field to the group.
	 * The specified property is bound to the input and updates when the value changes.
	 *
	 * @param name Name displayed on the label besides the input element.
	 * @param description Optional tooltip description visible when hovering the label.
	 * @param property Property entry this element is bound to.
	 * @return The input element which has been created and added to the entry.
	 */
	protected fun addInputItem(
		name: String,
		description: String?,
		property: String,
	): JTextField = addInputItem(name, description, property, false)

	/**
	 * Adds an input field to the group.
	 * The specified property is bound to the input and updates when the value changes.
	 *
	 * @param name Name displayed on the label besides the input element.
	 * @param description Optional tooltip description visible when hovering the label.
	 * @param property Property entry this element is bound to.
	 * @param isPassword If `true` the input field's text is masked and thus usable for passwords.
	 * @return The input element which has been created and added to the entry.
	 */
	protected fun addInputItem(
		name: String,
		description: String?,
		property: String,
		isPassword: Boolean,
	): JTextField {
		val label: JLabel = addLabel(name, description)

		var value: String? = properties.getProperty(property)
		value = value ?: ""

		val input: JTextField
		if (isPassword) {
			input =
				object : JPasswordField(value) {
					override fun getPreferredSize(): Dimension {
						val dim: Dimension = super.getPreferredSize()
						dim.width = 100
						return dim
					}
				}
		} else {
			input =
				object : JTextField(value) {
					override fun getPreferredSize(): Dimension {
						val dim: Dimension = super.getPreferredSize()
						dim.width = 100
						return dim
					}
				}
		}

		fieldLabels.put(input, label)
		// add listener for value changes
		input.document.addDocumentListener(
			object : DocumentListener {
				override fun insertUpdate(e: DocumentEvent) {
					properties.setProperty(property, input.getText())
				}

				override fun removeUpdate(e: DocumentEvent) {
					properties.setProperty(property, input.getText())
				}

				override fun changedUpdate(e: DocumentEvent) {
					// ignore
				}
			},
		)
		addComponent(input)
		itemIdx++

		return input
	}

	protected fun addScalarListItem(
		name: String,
		desc: String?,
		property: String,
		entry: ScalarListEntryType,
	): ScalarListItem {
		val label: JLabel = addListLabel(name, desc)

		val item: ScalarListItem = ScalarListItem(property, properties, entry)
		fieldLabels.put(item, label)
		addComponent(item)
		itemIdx++

		return item
	}

	/**
	 * Adds a check box to the group.
	 * The specified property is bound to the input and updates when the value changes.
	 *
	 * @param name Name displayed on the label besides the input element.
	 * @param description Optional tooltip description visible when hovering the label.
	 * @param property Property entry this element is bound to.
	 * @return The check box element which has been created and added to the entry.
	 */
	protected fun addBoolItem(
		name: String,
		description: String?,
		property: String,
	): JCheckBox {
		val label: JLabel = addLabel(name, description)

		val value: String? = properties.getProperty(property)
		if (value == null || value == "") {
			properties.setProperty(
				property,
				java.lang.Boolean.FALSE
					.toString(),
			)
		}
		val boolValue: Boolean = value.toBoolean()
		val input: JCheckBox = JCheckBox()
		input.setSelected(boolValue)
		fieldLabels.put(input, label)
		// add listener for value changes
		input.addItemListener(
			object : ItemListener {
				override fun itemStateChanged(e: ItemEvent) {
					properties.setProperty(property, input.isSelected.toString())
				}
			},
		)
		addComponent(input)
		itemIdx++

		return input
	}

	/**
	 * Adds a selection field to the group.
	 * The specified property is bound to the input and updates when the value changes.
	 *
	 * @param name Name displayed on the label besides the input element.
	 * @param description Optional tooltip description visible when hovering the label.
	 * @param property Property entry this element is bound to.
	 * @param values
	 * @return The selection element which has been created and added to the entry.
	 */
	protected fun addSelectionItem(
		name: String,
		description: String?,
		property: String,
		vararg values: String,
	): JComboBox<String> {
		val label: JLabel = addLabel(name, description)
		val test: JPanel = JPanel()
		val comboBox: JComboBox<String> = JComboBox(values)

		if (listOf(*values).contains("")) {
			comboBox.setSelectedItem(properties.getProperty(property))
		} else {
			// in this case the empty string is not allowed so an option have to be set. We take the first element in the
			// array.
			val prop: String? = properties.getProperty(property)
			if (prop == null || prop == "") {
				comboBox.setSelectedItem(values.get(0))
			} else {
				comboBox.setSelectedItem(prop)
			}
		}

		comboBox.addItemListener(
			object : ItemListener {
				override fun itemStateChanged(e: ItemEvent) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						properties.setProperty(property, e.getItem() as String?)
					}
				}
			},
		)

		addComponent(comboBox)
		fieldLabels.put(test, label)
		itemIdx++
		return comboBox
	}

	/**
	 * Adds a selection field capable of selecting multiple values to the group.
	 * The specified property is bound to the input and updates when the value changes.
	 *
	 * @param name Name displayed on the label besides the input element.
	 * @param description Optional tooltip description visible when hovering the label.
	 * @param property Property entry this element is bound to.
	 * @param values Selectable values.
	 * @return The selection element which has been created and added to the entry.
	 */
	protected fun addMultiSelectionItem(
		name: String,
		description: String?,
		property: String,
		values: List<String>,
	): JPanel {
		val optionName: JLabel = addListLabel(name, description)
		val contentPane: JPanel = JPanel(GridBagLayout())
		val checkboxPane: JPanel = JPanel(GridBagLayout())

		var row: Int = 0
		var col: Int = 0
		val property2: String? = properties.getProperty(property)
		for (value: String in values) {
			val c: GridBagConstraints = GridBagConstraints()
			if (col != 0) {
				if (col % 3 == 0) {
					col = 0
					row = row + 1
				}
			}
			c.gridx = col
			c.gridy = row
			c.fill = GridBagConstraints.NONE
			c.anchor = GridBagConstraints.NORTHWEST

			if (property2 != null) {
				val multProps: Array<String> =
					property2
						.split(
							";".toRegex(),
						).dropLastWhile { it.isEmpty() }
						.toTypedArray()
				val selectedOpts: List<String> = listOf(*multProps)
				if (selectedOpts.contains(value)) {
					checkboxPane.add(CheckboxListItem(value, true, property, properties), c)
				} else {
					checkboxPane.add(CheckboxListItem(value, false, property, properties), c)
				}
			} else {
				checkboxPane.add(CheckboxListItem(value, false, property, properties), c)
			}

			if (col == 2) {
				val c2: GridBagConstraints = GridBagConstraints()
				c2.fill = GridBagConstraints.BOTH
				c2.weightx = 1.0
				c2.gridx = 3
				c2.gridy = row
				checkboxPane.add(JLabel(), c2)
			}

			col++
		}

		val c3: GridBagConstraints = GridBagConstraints()
		c3.anchor = GridBagConstraints.NORTHWEST
		c3.fill = GridBagConstraints.HORIZONTAL
		c3.gridwidth = GridBagConstraints.REMAINDER
		c3.weightx = 1.0
		contentPane.add(checkboxPane, c3)

		addComponent(contentPane)
		itemIdx++
		fieldLabels.put(contentPane, optionName)
		return contentPane
	}

	private fun addLabel(
		name: String,
		description: String?,
	): JLabel {
		val label: JLabel = JLabel(name)
		label.setToolTipText(description)
		label.setFont(label.getFont().deriveFont(Font.PLAIN))
		val constraints: GridBagConstraints = GridBagConstraints()
		constraints.insets = Insets(5, 10, 0, 5)
		constraints.gridx = 0
		constraints.gridy = itemIdx
		constraints.anchor = GridBagConstraints.WEST
		container.add(label, constraints)
		return label
	}

	private fun addListLabel(
		name: String,
		description: String?,
	): JLabel {
		val label: JLabel = JLabel(name)
		label.setToolTipText(description)
		label.setFont(label.getFont().deriveFont(Font.PLAIN))
		val constraints: GridBagConstraints = GridBagConstraints()
		constraints.insets = Insets(5, 10, 0, 5)
		constraints.gridx = 0
		constraints.gridy = itemIdx
		constraints.anchor = GridBagConstraints.NORTHWEST
		container.add(label, constraints)
		return label
	}

	private fun addComponent(component: Component) {
		val constraints: GridBagConstraints = GridBagConstraints()
		constraints.insets = Insets(5, 3, 0, 5)
		constraints.fill = GridBagConstraints.HORIZONTAL
		constraints.gridx = 2
		constraints.gridy = itemIdx
		container.add(component, constraints)
	}

	protected fun addScalarEntryTypNumber(
		name: String,
		description: String?,
		property: String,
		type: String,
	): JSpinner? {
		val label: JLabel = addLabel(name, description)
		val value: String? = properties.getProperty(property)
		val model: SpinnerMathNumberModel

		if (type == ScalarEntryType.BIGDECIMAL.name) {
			if (value == null || value == "") {
				model = SpinnerMathNumberModel(BigDecimal("0.0"), null, null, BigDecimal("0.1"))
			} else {
				val convertedValue: BigDecimal = BigDecimal(value)
				model = SpinnerMathNumberModel(convertedValue, null, null, BigDecimal("0.1"))
			}
		} else if (type == ScalarEntryType.BIGINTEGER.name) {
			if (value == null || value == "") {
				model = SpinnerMathNumberModel(BigInteger.ZERO, null, null, BigInteger.ONE)
			} else {
				val convertedValue: BigInteger = BigInteger(value)
				model = SpinnerMathNumberModel(convertedValue, null, null, BigInteger.ONE)
			}
		} else {
			LOG.error { "Type STRING and BOOLEAN are not allowed for the use of this function." }
			return null
		}

		val spinner: JSpinner =
			object : JSpinner(model) {
				override fun getPreferredSize(): Dimension {
					val dim: Dimension = super.getPreferredSize()
					dim.width = 100
					return dim
				}
			}
		spinner.addChangeListener(
			object : ChangeListener {
				override fun stateChanged(e: ChangeEvent) {
					properties.setProperty(property, spinner.getModel().getValue().toString())
				}
			},
		)
		spinner.setEnabled(true)
		spinner.setEditor(MathNumberEditor(spinner, DecimalFormat.getInstance(spinner.getLocale()) as DecimalFormat?))
		addComponent(spinner)
		fieldLabels.put(spinner, label)
		itemIdx++
		return spinner
	}

	protected fun addFileEntry(
		name: String,
		description: String?,
		property: String,
		fileType: String,
		requiredBeforeAction: Boolean,
	): JPanel {
		val filePanel: JPanel = JPanel(GridBagLayout())
		val label: JLabel = addLabel(name, description)

		val currentValue: String? = properties.getProperty(property)

		val filePathField: JTextField =
			object : JTextField() {
				// the following is necessary because most path are longer than the field and GridBagLayout does not care
				// about MaximumSize. The following seems good for the default size of the configuration window but is does
				// not scale good if the size of the window becomes bigger.
				override fun getPreferredSize(): Dimension {
					val dim: Dimension = super.getPreferredSize()
					dim.width = 100
					return dim
				}
			}

		if (currentValue != null) {
			filePathField.text = currentValue
			filePathField.setToolTipText(currentValue)
		}

		filePathField.document.addDocumentListener(
			object : DocumentListener {
				override fun insertUpdate(e: DocumentEvent) {
					val file: File = File(filePathField.getText())
					if (file.exists() && file.isFile()) {
						properties.setProperty(property, filePathField.getText())
					}
				}

				override fun removeUpdate(e: DocumentEvent) {
					val file: File = File(filePathField.getText())
					if ((file.exists() && file.isFile()) || filePathField.getText() == "") {
						properties.setProperty(property, filePathField.getText())
					}
				}

				override fun changedUpdate(e: DocumentEvent) {
					// ignore
				}
			},
		)

		val fieldConstraint: GridBagConstraints = GridBagConstraints()
		fieldConstraint.anchor = GridBagConstraints.WEST
		fieldConstraint.fill = GridBagConstraints.HORIZONTAL
		fieldConstraint.gridx = 0
		fieldConstraint.gridy = 0
		fieldConstraint.weightx = 2.0
		filePanel.add(filePathField, fieldConstraint)

		val browseButton: JButton = JButton(I18N.strings.addon_settings_browse.localized())
		browseButton.addActionListener(OpenFileBrowserListener(fileType, filePathField))
		val buttonConstraint: GridBagConstraints = GridBagConstraints()
		buttonConstraint.anchor = GridBagConstraints.EAST
		buttonConstraint.fill = GridBagConstraints.NONE
		buttonConstraint.gridx = 1
		buttonConstraint.gridy = 0
		buttonConstraint.weightx = 0.0
		buttonConstraint.gridwidth = GridBagConstraints.REMAINDER
		buttonConstraint.insets = Insets(0, 5, 0, 0)
		filePanel.add(browseButton, buttonConstraint)

		addComponent(filePanel)
		itemIdx++
		fieldLabels.put(filePanel, label)
		return filePanel
	}

	fun addFileListEntry(
		name: String,
		description: String,
		property: String,
		fileType: String,
		isRequired: Boolean,
	): JPanel {
		val label: JLabel = addListLabel(name, description)

		val item: FileListEntryItem = FileListEntryItem(fileType, property, properties)

		val constraints2: GridBagConstraints = GridBagConstraints()
		constraints2.anchor = GridBagConstraints.NORTHWEST
		constraints2.insets = Insets(5, 3, 0, 0)
		constraints2.fill = GridBagConstraints.HORIZONTAL
		constraints2.gridx = 2
		constraints2.gridy = itemIdx
		container.add(item, constraints2)

		itemIdx++
		fieldLabels.put(item, label)

		return item
	}

	companion object {
		private const val serialVersionUID: Long = 1L
	}
}
