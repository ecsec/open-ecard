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
 ***************************************************************************/

package org.openecard.common.util;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Very simple implementation of a bidirectional map.
 * The map saves the reverse mapping for all key value pairs and can return a reversed view on the map. <br>
 * Besides the put method, this map is immutable in order to simplify the implementation. As a result this map is
 * intended to be used as a pure lookup table for bidirectional key value pairs.
 *
 * @author Tobias Wich
 * @param <K1> Type of the first key parameter.
 * @param <K2> Type of the second key parameter.
 */
public class BiMap <K1, K2> extends AbstractMap<K1, K2> {

    private final Object synchPoint;
    private final Map<K1, K2> map1;
    private final Map<K2, K1> map2;
    private final BiMap<K2, K1> reverse;

    public BiMap() {
	this.synchPoint = new Object();
	this.map1 = new HashMap<>();
	this.map2 = new HashMap<>();
	this.reverse = new BiMap<>(synchPoint, map2, map1, this);
    }

    private BiMap(Object synchPoint, Map<K1, K2> map1, Map<K2, K1> map2, BiMap reverse) {
	this.synchPoint = synchPoint;
	this.map1 = map1;
	this.map2 = map2;
	this.reverse = reverse;
    }

    @Override
    public Set<Entry<K1, K2>> entrySet() {
	return Collections.unmodifiableMap(map1).entrySet();
    }

    @Override
    public K2 put(K1 key1, K2 key2) {
	synchronized (synchPoint) {
	    K2 result = map1.put(key1, key2);
	    map2.put(key2, key1);
	    return result;
	}
    }

    /**
     * Gets a reversed view of the map.
     *
     * @return Reversed view of the map.
     */
    public BiMap<K2, K1> reverse() {
	return reverse;
    }

}
