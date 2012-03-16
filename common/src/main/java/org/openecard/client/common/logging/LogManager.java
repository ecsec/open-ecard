package org.openecard.client.common.logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


/**
 * Shallow LogManager which can help set up the logging configuration<br/>
 * This class is mostly irrelevant when using java.util.logging, however it makes it easy to switch implementations
 * like log4j etc. (provided someone wants to do such a foolish thing ;-).
 *
 * @author Johannes Schmoelz <johannes.schmoelz@ecsec.de>
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class LogManager {
    
    public static final String openecardPath = System.getProperty("user.home") + File.separator + ".openecard";
    public static final String openecardConfFileName = "logging.properties";


    public static void createHomeLogPath(String dirName) {
	File f = new File(System.getProperty("user.home") + File.separator + dirName);
	createLogPath(f);
    }
    public static void createLogPath(File path) {
	if (! path.exists()) {
	    path.mkdirs();
	}
    }

    public static boolean loadOpeneCardDefaultConfig() {
	createLogPath(new File(openecardPath));
	return loadConfigFromResource("openecard_config/" + openecardConfFileName);
    }

    public static boolean loadConfigFromResource(String resourceName) {
	InputStream s = LogManager.class.getResourceAsStream(resourceName);
	// try loading with leading /
	if (s == null && !resourceName.startsWith("/")) {
	    s = LogManager.class.getResourceAsStream("/" + resourceName);
	}
	// check again
	if (s == null) {
	    System.err.println("ERROR: The logging config file '" + resourceName + "' could not be found in classpath.");
	    return false;
	} else {
	    return loadConfig(s);
	}
    }

    public static boolean loadConfig(InputStream config) {
	try {
	    java.util.logging.LogManager.getLogManager().readConfiguration(config);
	    return true;
	} catch (IOException ex) {
	    System.err.println("ERROR: Unable to read new log configuration.");
	    ex.printStackTrace(System.err);
	    return false;
	}
    }
    
    public static java.util.logging.Logger getLogger(String name) {
        return java.util.logging.Logger.getLogger(name);
    }

}
