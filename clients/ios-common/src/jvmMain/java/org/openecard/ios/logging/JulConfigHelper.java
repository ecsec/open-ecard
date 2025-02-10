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


import java.util.Date;
import java.util.logging.*;

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
    handler.setFormatter(new Formatter() {
        @Override
        public String format(LogRecord record) {
         return String.format("[%1$tF %1$tT.%1$tL %2$s.%3$s [%4$-7s] %5$s %n", new Date(record.getMillis()), record.getSourceClassName(), record.getSourceMethodName(), record.getLevel(), record.getMessage());
        }
    });
	Logger root = Logger.getLogger("");
	root.addHandler(handler);
    root.setLevel(Level.FINEST);

    }

    public static void registerLogHandler(LogMessageHandler handler) {
        Logger root = Logger.getLogger("");
        root.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                handler.log(String.format("[%1$tF %1$tT.%1$tL %2$s.%3$s [%4$-7s] %5$s %n", new Date(record.getMillis()), record.getSourceClassName(), record.getSourceMethodName(), record.getLevel(), record.getMessage()));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        });
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
