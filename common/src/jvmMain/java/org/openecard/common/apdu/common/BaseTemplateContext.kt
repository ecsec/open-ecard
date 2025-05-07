/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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
package org.openecard.common.apdu.common

/**
 * Template context containing functions and values which may be helpful in all contexts.
 * Specialized contexts may derive from this class and add context specific values and functions.
 *
 * @author Tobias Wich
 */
class BaseTemplateContext : HashMap<String?, Any?>() {
    /**
     * Provision the map with basic values and functions.
     */
    init {
        put("tlv", TLVFunction())
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
