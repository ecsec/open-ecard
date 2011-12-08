package org.openecard.client.recognition;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openecard.client.common.OverridingProperties;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class RecognitionProperties {

    private static class Internal extends OverridingProperties {
        public Internal() throws IOException {
            super("cardrecognition.properties");
        }
    }

    static {
        try {
            properties = new Internal();
        } catch (IOException ex) {
            // in that case a null pointer occurs when properties is accessed
            Logger.getLogger(RecognitionProperties.class.getName()).log(Level.SEVERE, null, ex);
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


    public static String getAction() {
        return getProperty("org.openecard.client.recognition.action");
    }

    public static String getServiceName() {
        return getProperty("org.openecard.client.recognition.serviceName");
    }

    public static String getServiceAddr() {
        return getProperty("org.openecard.client.recognition.serviceAddr");
    }

}
