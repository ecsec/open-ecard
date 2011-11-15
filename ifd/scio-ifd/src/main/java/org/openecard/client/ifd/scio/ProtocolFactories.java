package org.openecard.client.ifd.scio;

import org.openecard.client.common.ifd.ProtocolFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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
