package org.openecard.client.common.logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 */
public class LogManager {
    
    static {
        setup();
    }
    
    private static void setup() {
        try {
            InputStream is = LogManager.class.getResourceAsStream("/logging.properties");
            if (is != null) {
                java.util.logging.LogManager.getLogManager().readConfiguration(is);
                File f = new File(System.getProperty("user.home") + File.separator + ".ecsec");
                if (! f.exists()) {
                    f.mkdir();
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        } catch (SecurityException ex) {
            System.err.println(ex.getMessage());
        }
    }
    
    public static java.util.logging.Logger getLogger(String name) {
        return java.util.logging.Logger.getLogger(name);
    }
}
