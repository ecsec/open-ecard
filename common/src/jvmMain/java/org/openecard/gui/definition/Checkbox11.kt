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
 * Checkbox element for user consents.
 * Checkbox items can be checked or unchecked. If one element is checked or unchecked it does not affect other items.
 *
 * @author Tobias Wich
 */
class Checkbox
/**
 * Creates a new Checkbox instance and initializes it with the given ID.
 *
 * @param id The ID to initialize the instance with.
 */
    (id: String) : AbstractBox(id) {
    override fun type(): InfoUnitElementType {
        return InfoUnitElementType.CHECK_BOX
    }
}
