package org.openecard.client.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Basic Properties class which overrides default values from System.properties on its creation.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class OverridingProperties {

    private final Properties properties;

    protected OverridingProperties(String fName) throws IOException {
	InputStream in = OverridingProperties.class.getResourceAsStream("/" + fName);
	if (in == null) {
	    in = OverridingProperties.class.getResourceAsStream(fName);
	}
	properties = new Properties();
	properties.load(in);
	init();
    }
    protected OverridingProperties(InputStream stream) throws IOException {
	properties = new Properties();
	properties.load(stream);
	init();
    }
    protected OverridingProperties(Properties props) {
	properties = new Properties(props);
	init();
    }

    private void init() {
	overrideFromSystem();
    }

    private void overrideFromSystem() {
	for (Object nextKey : properties.keySet()) {
	    if (nextKey instanceof String) {
		String next = (String) nextKey;
		if (System.getProperties().containsKey(next)) {
		    properties.setProperty(next, System.getProperties().getProperty(next));
		}
	    }
	}
    }


    public final String getProperty(String key) {
	return properties.getProperty(key);
    }

    public final Object setProperty(String key, String value) {
	return properties.setProperty(key, value);
    }

    public final Properties properties() {
        return new Properties(properties);
    }

}
