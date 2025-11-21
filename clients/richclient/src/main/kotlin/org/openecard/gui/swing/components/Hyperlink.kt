/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

import org.openecard.gui.definition.Hyperlink
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.swing.StepFrame
import org.openecard.gui.swing.common.SwingUtils
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import javax.swing.JLabel

/**
 * Implementation of a hyperlink for use in a [StepFrame]. <br></br>
 * The link also has a click event which launches a browser. When the mouse
 * is located over the link, it is emphasised by underlining it.<br></br>
 * If no text is supplied, the text of the url is displayed.
 *
 * @author Tobias Wich
 */
class Hyperlink(
	link: Hyperlink,
) : StepComponent {
	private val href: URL = link.href!!
	private val text: String = link.text ?: href.toString()
	private val underlineText: String = "<html><u>$text</u></html>"
	private val label: JLabel = JLabel(text)

	init {
		label.isDoubleBuffered = true
		label.setForeground(Color.blue)
		label.setToolTipText(href.toString())
		label.addMouseListener(BrowserLauncher())
	}

	override val component: Component
		get() {
			return label
		}

	override fun validate(): Boolean = true

	override val isValueType: Boolean = false

	override val value: OutputInfoUnit? = null

	/**
	 * Open browser on click and hover link when mouse is located over the link.
	 */
	private inner class BrowserLauncher : MouseListener {
		override fun mouseClicked(e: MouseEvent?) {
			try {
				val uri = URI(href.toString())
				SwingUtils.openUrl(uri, false)
			} catch (ex: URISyntaxException) {
				// silently fail, its just no use against developer stupidity
			}
		}

		override fun mousePressed(e: MouseEvent?) {
		}

		override fun mouseReleased(e: MouseEvent?) {
		}

		override fun mouseEntered(e: MouseEvent?) {
			label.setText(underlineText)
		}

		override fun mouseExited(e: MouseEvent?) {
			label.setText(text)
		}
	}
}
