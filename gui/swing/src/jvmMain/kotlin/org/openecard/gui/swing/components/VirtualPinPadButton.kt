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
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.text.JTextComponent

/**
 * Button launching a pin pad dialog when pressed.
 * The button needs access to the password input field, and the definition to create a suitable dialog.
 *
 * @author Tobias Wich
 */
class VirtualPinPadButton(private val inputField: JTextComponent, private val passDef: PasswordField) : JLabel(
	pinPadIcon
) {
	/**
	 * Creates an instance of the button.
	 *
	 * @param inputField The component capturing the PIN.
	 * @param passDef The definition of the password field.
	 */
	init {
		setBorder(BorderFactory.createEmptyBorder())
		addMouseListener(ButtonStyleHandler())
		addMouseListener(VirtualPinPadDialogHandler())
	}

	private inner class ButtonStyleHandler : MouseListener {
		override fun mouseClicked(e: MouseEvent) {
		}

		override fun mousePressed(e: MouseEvent) {
			setBorder(BorderFactory.createLoweredBevelBorder())
		}

		override fun mouseReleased(e: MouseEvent) {
			setBorder(BorderFactory.createEmptyBorder())
		}

		override fun mouseEntered(e: MouseEvent) {
		}

		override fun mouseExited(e: MouseEvent) {
		}
	}

	private inner class VirtualPinPadDialogHandler : MouseListener {
		override fun mouseClicked(e: MouseEvent) {
			val dialog = VirtualPinPadDialog(this@VirtualPinPadButton, inputField, passDef)
			dialog.isVisible = true
		}

		override fun mousePressed(e: MouseEvent) {
		}

		override fun mouseReleased(e: MouseEvent) {
		}

		override fun mouseEntered(e: MouseEvent) {
		}

		override fun mouseExited(e: MouseEvent) {
		}
	}

}

private val pinPadIcon: ImageIcon
	get() {
		val imgUrl = resolveResourceAsURL(
			VirtualPinPadButton::class.java,
			"virtual-pinpad-button.png"
		)
		val img = ImageIcon(imgUrl)
		return img
	}
