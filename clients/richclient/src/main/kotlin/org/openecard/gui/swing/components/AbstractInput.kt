/****************************************************************************
 * Copyright (C) 2012-2016 ecsec GmbH.
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
package org.openecard.gui.swing.components

import org.openecard.gui.definition.AbstractTextField
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.definition.PasswordField
import org.openecard.gui.definition.TextField
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField
import javax.swing.text.BadLocationException
import javax.swing.text.Segment

/**
 * Common base for [TextField] and [PasswordField]. <br></br>
 * The casting is ugly, but in the short time no better solution occured to me.
 * Remind, the problem is that TextInput and PasswordInput are identical but
 * have no base class. C++ templates would help, but Java generics don't.
 * Feel free to get rid of this mess.
 *
 * @author Tobias Wich
 */
class AbstractInput private constructor(
	input: AbstractTextField,
	output: AbstractTextField,
	textFieldImpl: JTextField,
) : StepComponent,
	Focusable {
	// extract values from input and write to output (depending on actual type)
	private val name: String = input.id
	private val minLength: Int = input.minLength
	private val maxLength: Int = input.maxLength

	private val panel: JPanel = JPanel()
	override val component = this.panel
	private val label: JLabel
	private val textField: JTextField

	private val result: AbstractTextField

	constructor(input: TextField) : this(input, TextField(input.id), JTextField(20))
	constructor(input: PasswordField) : this(input, PasswordField(input.id), JPasswordField(12)) {
		this.panel.add(VirtualPinPadButton(textField, input), 1)
	}

	init {
		val value: CharArray = input.value
		val labelText = input.description
		// create result element
		result = output
		result.minLength = minLength
		result.maxLength = maxLength
		result.description = labelText

		// correct values
		this.textField = textFieldImpl
		if (this.textField is JPasswordField) {
		} else {
			this.textField.text = String(value)
			this.textField.selectAll()
		}

		this.label = JLabel()
		this.label.minimumSize = Dimension(100, 0)
		this.label.maximumSize = Dimension(100, 50)
		this.label.setSize(100, this.label.size.height)
		if (labelText != null) {
			this.label.setText(labelText)
		}

		// create panel for display
		val panelLayout = FlowLayout(FlowLayout.LEFT)
		this.panel.setLayout(panelLayout)
		this.panel.add(this.textField)
		this.panel.add(this.label)
	}

	private val fieldValue: CharArray
		get() {
			val doc = textField.document
			val txt = Segment()
			try {
				doc.getText(0, doc.length, txt) // use the non-String API
			} catch (e: BadLocationException) {
				return CharArray(0)
			}
			val retValue = CharArray(txt.count)
			System.arraycopy(txt.array, txt.offset, retValue, 0, txt.count)
			return retValue
		}

	override fun validate(): Boolean {
		var textValue = this.textField.getText()
		if (textValue == null) {
			textValue = ""
		}
		val textSize = textValue.length
		// min <= text && text <= max
		return minLength <= textSize && textSize <= maxLength
	}

	override val isValueType: Boolean = true

	override val value: OutputInfoUnit
		get() {
			val textValue = this.fieldValue
			result.value = textValue
			textValue.fill(' ')
			return result
		}

	override fun setFocus() {
		textField.requestFocusInWindow()
	}
}
