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
 * Definition class for password fields.
 * Password fields have a field where text can be input. The text is masked, so that other persons watching the user
 * enter his password can't see it.
 *
 * @author Tobias Wich
 */
class PasswordField(
	id: String,
) : AbstractTextField(id) {
	override fun type(): InfoUnitElementType = InfoUnitElementType.PASSWORD_FIELD
}
