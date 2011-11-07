package org.openecard.client.ifd;

import org.openecard.client.common.OverridingProperties;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class IFDProperties {

    private static class Internal extends OverridingProperties {
        public Internal() throws IOException {
            super("ifd.properties");
        }
    }

    static {
        try {
            properties = new Internal();
        } catch (IOException ex) {
            // in that case a null pointer occurs when properties is accessed
            Logger.getLogger(IFDProperties.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static Internal properties;


    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }

}
