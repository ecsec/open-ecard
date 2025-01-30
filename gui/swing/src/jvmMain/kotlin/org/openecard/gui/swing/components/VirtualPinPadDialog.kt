/****************************************************************************
 * Copyright (C) 2014-2016 ecsec GmbH.
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

import org.openecard.common.util.FileUtils.resolveResourceAsURL
import org.openecard.gui.definition.PasswordField
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Insets
import java.awt.Window
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.WindowEvent
import javax.swing.*
import javax.swing.text.JTextComponent

/**
 * Dialog displaying a virtual pin pad.
 * The dialog is modal and bound to the window displaying the input field. If a maximum length is given in the
 * definition of the PIN element, then the dialog closes itself automatically after entering the respective number of
 * characters.
 *
 * @author Tobias Wich
 */
class VirtualPinPadDialog(
    pinButton: VirtualPinPadButton,
    private val inputField: JTextComponent,
    private val passDef: PasswordField
) : JDialog(
    getOwningWindow(inputField), "PIN-Pad", ModalityType.DOCUMENT_MODAL
) {

    private var numCharsEntered = 0

    /**
     * Creates a new instance of the dialog.
     *
     * @param pinButton
     * @param inputField The component capturing the PIN.
     * @param passDef The definition of the password field.
     */
    init {
		this.inputField.text = ""

        setSize(200, 200)
        setResizable(false)
		layout = BorderLayout(3, 3)

        val dialogLocation = pinButton.locationOnScreen
        dialogLocation.translate(0, pinButton.getHeight())
		location = dialogLocation

        val buttons = JPanel(GridLayout(4, 3, 4, 4))
        add(buttons, BorderLayout.CENTER)
        for (i in 1..9) {
            buttons.add(createButton(i))
        }
        // last row
        buttons.add(createRemoveSingleElementButton())
        buttons.add(createButton(0))
        buttons.add(createCloseButton())
    }

    private fun createButton(num: Int): JButton {
        val button = JButton(num.toString())
        button.addActionListener(NumberProcessingListener())
        return button
    }

    private fun createRemoveSingleElementButton(): JButton {
        val button = JButton()
        //setButtonFont(button);
        button.addActionListener(RemoveSingleElementListener())
        val ico: Icon = ImageIcon(resolveResourceAsURL(VirtualPinPadDialog::class.java, "arrow.png"))
        button.setIcon(ico)
        val marginInset = button.margin
        button.setMargin(Insets(marginInset.top, 5, marginInset.bottom, 5))
        return button
    }

    private fun createCloseButton(): JButton {
        val button = JButton("OK")
        //setButtonFont(button);
        button.addActionListener(CloseInputListener())
        val marginInset = button.margin
        button.setMargin(Insets(marginInset.top, 5, marginInset.bottom, 5))
        return button
    }

    private fun setButtonFont(button: JButton) {
        var f = button.getFont()
        f = f.deriveFont(f.size2D + 10)
        button.setFont(f)
    }


    /**
     * Listener handling button presses of the PIN number buttons.
     */
    private inner class NumberProcessingListener : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            val b = e.getSource() as JButton
            var data = inputField.getText()
            data += b.text
			inputField.text = data

            numCharsEntered++
            if (passDef.maxLength > 0 && numCharsEntered >= passDef.maxLength) {
				isVisible = false
            }
        }
    }

    private inner class RemoveSingleElementListener : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            var data = inputField.getText()
            if (!data.isEmpty()) {
                data = data.substring(0, data.length - 1)
				inputField.text = data
                numCharsEntered--
            }
        }
    }

    private inner class CloseInputListener : ActionListener {
        override fun actionPerformed(e: ActionEvent) {
            dispatchEvent(WindowEvent(this@VirtualPinPadDialog, WindowEvent.WINDOW_CLOSING))
        }
    }

}

private fun getOwningWindow(inputField: JTextComponent): Window? {
	return SwingUtilities.getWindowAncestor(inputField)
}
