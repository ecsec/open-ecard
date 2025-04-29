/****************************************************************************
 * Copyright (C) 2016 ecsec GmbH.
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
package org.openecard.common.util

import java.util.*

/**
 * Very simple implementation of a bidirectional map.
 * The map saves the reverse mapping for all key value pairs and can return a reversed view on the map. <br></br>
 * Besides the put method, this map is immutable in order to simplify the implementation. As a result this map is
 * intended to be used as a pure lookup table for bidirectional key value pairs.
 *
 * @author Tobias Wich
 * @param <K1> Type of the first key parameter.
 * @param <K2> Type of the second key parameter.
</K2></K1> */
class BiMap<K1, K2> : AbstractMap<K1, K2?> {
    private val synchPoint: Any
    private val map1: MutableMap<K1, K2?>
    private val map2: MutableMap<K2?, K1>
    private val reverse: BiMap<K2, K1>

    constructor() {
        this.synchPoint = Any()
        this.map1 = HashMap()
        this.map2 = HashMap()
        this.reverse = BiMap(synchPoint, map2, map1, this)
    }

    private constructor(synchPoint: Any, map1: MutableMap<K1, K2>, map2: MutableMap<K2, K1>, reverse: BiMap<*, *>) {
        this.synchPoint = synchPoint
        this.map1 = map1
        this.map2 = map2
        this.reverse = reverse
    }

    override fun entrySet(): Set<Map.Entry<K1, K2?>> {
        return Collections.unmodifiableMap(map1).entries
    }

    override fun put(key1: K1, key2: K2?): K2? {
        synchronized(synchPoint) {
            val result = map1.put(key1, key2)
            map2[key2] = key1
            return result
        }
    }

    /**
     * Gets a reversed view of the map.
     *
     * @return Reversed view of the map.
     */
    fun reverse(): BiMap<K2, K1> {
        return reverse
    }
}
