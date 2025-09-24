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
 */
package org.openecard.gui.definition

/**
 * GUI component types.
 * Each element type is listed in this enum to operate with the [InfoUnit] type without performing the instanceof
 * idiom.
 *
 * @author Tobias Wich
 */
enum class InfoUnitElementType {
	TEXT,
	TOGGLE_TEXT,
	HYPERLINK,
	IMAGE_BOX,
	CHECK_BOX,
	RADIO_BOX,
	TEXT_FIELD,
	PASSWORD_FIELD,
	SIGNAUTRE_FIELD,
}
