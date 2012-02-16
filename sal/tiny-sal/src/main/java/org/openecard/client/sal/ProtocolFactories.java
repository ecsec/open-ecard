/*
 * Copyright 2012 Tobias Wich ecsec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecard.client.sal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openecard.client.common.sal.ProtocolFactory;


/**
 * Simple class to memorise different protocol factories.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class ProtocolFactories {

    private Map<String,ProtocolFactory> factories = new TreeMap<String, ProtocolFactory>();


    public boolean contains(String proto) {
	return factories.containsKey(proto);
    }
    public List<String> protocols() {
	return new ArrayList<String>(factories.keySet());
    }

    public ProtocolFactory get(String proto) {
	return factories.get(proto);
    }

    public boolean add(String proto, ProtocolFactory impl) {
	boolean result = false;
	if (! contains(proto)) {
	    result = true;
	    factories.put(proto, impl);
	}
	return result;
    }

}
