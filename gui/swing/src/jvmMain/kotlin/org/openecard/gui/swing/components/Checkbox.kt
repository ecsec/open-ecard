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
import org.openecard.gui.definition.Checkbox
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.swing.StepFrame
import java.awt.*
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

/**
 * Implementation of a checkbox group for use in a [StepFrame].
 *
 * @author Tobias Wich
 * @author Florian Feldmann
 * @author Moritz Horsch
 */
class Checkbox(checkbox: Checkbox) : StepComponent, Focusable {

    private val result: Checkbox = Checkbox(checkbox.id) // copy of checkbox, so result is pre assembled
	private val boxButtons: ArrayList<JCheckBox>
    private val defaultCheckbox: MutableList<BoxItem>
    private val panel: JPanel = JPanel(BorderLayout())
	var contentPanel: JPanel = JPanel()

    init {
		//	contentPanel.setBorder(new LineBorder(Color.GRAY));
        panel.setBorder(EmptyBorder(10, 10, 10, 10))

        // set group heading if it exists
        if (checkbox.groupText != null && !checkbox.groupText.isEmpty()) {
            val groupText = JLabel(checkbox.groupText)
            groupText.setBorder(EmptyBorder(5, 0, 10, 0))
            panel.add(groupText, BorderLayout.NORTH)
        }


        val layout = GridLayout(0, 2)
        //	GridBagLayout layout = new GridBagLayout();
        contentPanel.setLayout(layout)
        val c = GridBagConstraints()
        var alternate = 0

        // create buttons, item copies and add to panel
        boxButtons = ArrayList<JCheckBox>(checkbox.getBoxItems().size)
        defaultCheckbox = checkbox.getBoxItems()
        for (next in checkbox.getBoxItems()) {
            // copy box item
            val copy = BoxItem()
            result.getBoxItems().add(copy)
			copy.name = next.name
			copy.text = next.text
			copy.isDisabled = next.isDisabled
            // create checkbox
            val component = CheckBoxItem(next.text ?: "", next.isChecked)
            if (next.isDisabled) {
                component.setEnabled(false)
            }
            if (next.isChecked) {
                component.setSelected(true)
            }

            component.setBackground(Color.WHITE)

            c.fill = GridBagConstraints.HORIZONTAL
            c.gridx = alternate
            c.gridy = GridBagConstraints.RELATIVE
            alternate = alternate xor 1 // alternate checkboxes left and right -

            // disable for vertical layout only
            contentPanel.add(component)
            //	    panel.add(component, c);
            boxButtons.add(component)
        }
        panel.add(contentPanel, BorderLayout.CENTER)
    }

    fun resetSelection() {
        for (i in 0..<contentPanel.componentCount) {
            val b = contentPanel.getComponent(i) as JCheckBox
            for (defaultCheckbox1 in defaultCheckbox) {
                if (b.getName() == defaultCheckbox1.name) {
                    b.setSelected(defaultCheckbox1.isChecked)
                }
            }
        }
    }

    override val component: Component
		get() {
			return panel
		}

    override fun validate(): Boolean {
        return true
    }

    override val isValueType: Boolean = true

    override val value: OutputInfoUnit
		get() {
        // loop over checkboxes and set checked values in result
        for (i in boxButtons.indices) {
            val component = boxButtons[i]
			this.result.getBoxItems()[i].isChecked = component.isSelected
        }
        // prepare result
        return result
    }

    override fun setFocus() {
        if (!boxButtons.isEmpty()) {
            boxButtons[0].requestFocusInWindow()
        }
    }
}
