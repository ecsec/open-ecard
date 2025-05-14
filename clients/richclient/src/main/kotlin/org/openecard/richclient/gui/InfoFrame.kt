/****************************************************************************
 * Copyright (C) 2015 ecsec GmbH.
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

package org.openecard.richclient.gui

import java.awt.Container
import javax.swing.JFrame

/**
 * Frame class with the necessary interface for status element updates.
 *
 * @author Tobias Wich
 */
class InfoFrame(
	title: String?,
) : JFrame(title),
	StatusContainer {
	private var isShown = false

	override fun updateContent(status: Container) {
		pack()
	}

	override fun setVisible(b: Boolean) {
		if (isShown) {
			state =
				if (b) {
					NORMAL
				} else {
					ICONIFIED
				}
		} else {
			super.setVisible(b)

			// set after first setVisable(true) call
			if (b) {
				isShown = true
			}
		}
	}
}
