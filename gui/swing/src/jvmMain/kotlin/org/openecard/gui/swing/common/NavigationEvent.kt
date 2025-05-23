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
package org.openecard.gui.swing.common

import java.awt.event.ActionEvent

/**
 *
 * @author Tobias Wich
 */
enum class NavigationEvent {
	NEXT,
	BACK,
	CANCEL,
	;

	companion object {
		fun fromEvent(e: ActionEvent): NavigationEvent? {
			val command = e.getActionCommand()
			if (command == GUIConstants.BUTTON_BACK) {
				return BACK
			} else if (command == GUIConstants.BUTTON_NEXT) {
				return NEXT
			} else if (command == GUIConstants.BUTTON_FINISH) {
				return NEXT
			} else if (command == GUIConstants.BUTTON_CANCEL) {
				return CANCEL
			}
			return null
		}
	}
}
