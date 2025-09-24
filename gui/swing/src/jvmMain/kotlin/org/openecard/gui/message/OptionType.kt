/****************************************************************************
 * Copyright (C) 2013 ecsec GmbH.
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
 */
package org.openecard.gui.message

/**
 * Option type describing the messagebox.
 * Options can be either a yes, no question box, a yes, no question box with a cancel button or an OK, cancel question
 * box.
 *
 * @author Tobias Wich
 */
enum class OptionType {
	YES_NO_OPTION,
	YES_NO_CANCEL_OPTION,
	OK_CANCEL_OPTION,
}
