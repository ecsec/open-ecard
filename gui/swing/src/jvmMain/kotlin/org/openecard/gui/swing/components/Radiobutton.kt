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
package org.openecard.gui.swing.components

import org.openecard.gui.definition.BoxItem
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.definition.Radiobox
import org.openecard.gui.swing.StepFrame
import java.awt.Component
import java.awt.GridLayout
import javax.swing.ButtonGroup
import javax.swing.JPanel
import javax.swing.JRadioButton

/**
 * Implementation of a radio button group for use in a [StepFrame].
 *
 * @author Tobias Wich
 */
class Radiobutton(radio: Radiobox) : StepComponent, Focusable {

    private val result: Radiobox = Radiobox(radio.id) // copy of radio, so result is pre assembled
	private val buttons: ArrayList<JRadioButton>
    private val panel: JPanel = JPanel()

	init {
		val layout = GridLayout(0, 1)
        panel.setLayout(layout)

        // create buttons and add to label, also copy items to result
        val bg = ButtonGroup()
        buttons = ArrayList<JRadioButton>(radio.getBoxItems().size)
        for (next in radio.getBoxItems()) {
            // copy box item
            val copy = BoxItem()
            result.getBoxItems().add(copy)
			copy.name = next.name
			copy.text = next.text
			copy.isDisabled = next.isDisabled
            // create checkbox
            val component = JRadioButton(next.text ?: "", next.isChecked)
            bg.add(component)
            if (next.isDisabled) {
                component.setEnabled(false)
            }
            if (next.isChecked) {
                component.setSelected(true)
            }
            panel.add(component)
            buttons.add(component)
        }
    }


	override val component: Component
		get(){
        return panel
    }

    override fun validate(): Boolean {
        // only valid if exactly one button is selected
        var numSelected = 0
        for (next in buttons) {
            if (next.isSelected) {
                numSelected++
            }
        }
        return numSelected == 1
    }

    override val isValueType: Boolean = true

	override val value: OutputInfoUnit
		get(){
        // loop over checkboxes and set checked values in result
        for (i in buttons.indices) {
            val component = buttons.get(i)
			result.getBoxItems()[i].isChecked = component.isSelected
        }
        return result
    }

    override fun setFocus() {
        if (!buttons.isEmpty()) {
            buttons[0].requestFocusInWindow()
        }
    }
}
