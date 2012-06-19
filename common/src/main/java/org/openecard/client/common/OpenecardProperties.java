package org.openecard.client.common;

import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class OpenecardProperties {

    private static final Logger _logger = LoggerFactory.getLogger(OpenecardProperties.class);

    private static class Internal extends OverridingProperties {
	public Internal() throws IOException {
	    super("openecard_config/openecard.properties");
	}
    }

    static {
	try {
	    properties = new Internal();
	} catch (IOException ex) {
	    // in that case a null pointer occurs when properties is accessed
	    _logger.error(ex.getMessage(), ex);
	}
    }

    private static Internal properties;


    public static String getProperty(String key) {
	return properties.getProperty(key);
    }

    public static Object setProperty(String key, String value) {
	return properties.setProperty(key, value);
    }

    public static Properties properties() {
	return properties.properties();
    }

}
