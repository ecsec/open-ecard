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

import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.definition.Text
import org.openecard.gui.swing.StepFrame
import java.awt.Component
import java.awt.Insets
import javax.swing.JTextPane
import javax.swing.UIManager
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit

/**
 * Implementation of a simple text component for use in a [StepFrame].
 *
 * @author Tobias Wich
 */
class Text(
	text: Text,
) : StepComponent {
	private val textArea: JTextPane

	init {
		val textValue = text.document!!

		val textString =
			when (textValue.mimeType) {
				"text/plain" -> "<html><body>" + textValue.value.decodeToString() + "</body></html>"

				"text/html" -> // pray that the code is HTML 3.2 compliant
					textValue.value.decodeToString()

				else -> throw IllegalArgumentException(
					"Content with the MimeType " + textValue.mimeType + " is not supported by the Swing Text implementation.",
				)
			}

		textArea = JTextPane()
		textArea.setEditorKitForContentType("text/html", HTMLEditorKit())
		textArea.setContentType("text/html")
		textArea.setMargin(Insets(0, 0, 0, 0))
		textArea.isEditable = false

		val font = UIManager.getFont("Label.font")
		val bodyRule = "body { font-family: " + font.family + "; " + "font-size: " + font.getSize() + "pt; }"
		val doc = textArea.document as HTMLDocument
		doc.styleSheet.addRule(bodyRule)

		textArea.text = textString
	}

	override val component: Component
		get() {
			return textArea
		}

	override fun validate(): Boolean = true

	override val isValueType: Boolean = false

	override val value: OutputInfoUnit? = null
}
