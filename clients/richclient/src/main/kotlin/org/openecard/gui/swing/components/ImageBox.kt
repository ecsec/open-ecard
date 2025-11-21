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

import org.openecard.gui.definition.ImageBox
import org.openecard.gui.definition.OutputInfoUnit
import org.openecard.gui.swing.StepFrame
import java.awt.Component
import javax.swing.ImageIcon
import javax.swing.JLabel

/**
 * Implementation of a simple image component for use in a [StepFrame].
 *
 * @author Tobias Wich
 */
class ImageBox(
	imageBox: ImageBox,
) : StepComponent {
	private val imageLabel: JLabel
	private val image: ImageIcon = ImageIcon(imageBox.imageData)

	init {
		this.imageLabel = JLabel(image)
	}

	override val component: Component
		get() {
			return imageLabel
		}

	override val isValueType: Boolean
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}

	override fun validate(): Boolean = throw UnsupportedOperationException("Not supported yet.")

	override val value: OutputInfoUnit?
		get() {
			throw UnsupportedOperationException("Not supported yet.")
		}
}
