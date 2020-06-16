/****************************************************************************
 * Copyright (C) 2020 ecsec GmbH.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file may be used in accordance with the terms and conditions
 * contained in a signed written agreement between you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.ios.logging;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 *
 * @author Tobias Wich
 */
public class JulConfigHelper {

    public static void resetLogging() {
	LogManager.getLogManager().reset();
	ConsoleHandler handler = new ConsoleHandler();
	// print every statement on the console, let the loggers decide whether to log a statement or not
	handler.setLevel(Level.ALL);
	Logger root = Logger.getLogger("");
	root.addHandler(handler);
	root.setLevel(Level.WARNING);
    }

    public static void setLogLevel(String logger, LogLevel level) {
	Logger.getLogger(logger).setLevel(toJulLevel(level));
    }

    private static Level toJulLevel(LogLevel level) {
	switch (level) {
	    case ERROR:
		return Level.SEVERE;
	    case WARN:
		return Level.WARNING;
	    case INFO:
		return Level.INFO;
	    case DEBUG:
	    default:
		return Level.FINE;
	}
    }

}
