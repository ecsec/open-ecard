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
package org.openecard.ifd.scio

import org.openecard.common.ifd.ProtocolFactory
import java.util.*

/**
 * Simple class to memorise different protocol factories.
 *
 * @author Tobias Wich
 */
class ProtocolFactories {

    private val factories: MutableMap<String, ProtocolFactory> = mutableMapOf()


    fun contains(proto: String): Boolean {
        return factories.containsKey(proto)
    }

    fun protocols(): List<String> {
        return factories.keys.toList()
    }

    fun get(proto: String): ProtocolFactory? {
        return factories[proto]
    }

    fun add(proto: String, impl: ProtocolFactory): Boolean {
        var result = false
        if (!contains(proto)) {
            result = true
            factories.put(proto, impl)
        }
        return result
    }
}
