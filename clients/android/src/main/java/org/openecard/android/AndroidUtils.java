/****************************************************************************
 * Copyright (C) 2012 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import java.io.IOException;
import java.io.InputStream;
import org.openecard.android.activities.LoggingTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Class provides util functions and constants especially for the Android App.
 * 
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public class AndroidUtils {

    private static final Logger logger = LoggerFactory.getLogger(AndroidUtils.class);

    public static final String LOGGINGTYPE = "LOGGINGTYPE";
    public static final int NO_LOG = LoggingTypes.NONE.ordinal();

    /**
     * Initializes the logging according to the type currently stored in the preferences. 
     * If no type is found in the preferences no logging is used as default.
     * 
     * @param ctx Context of the App
     */
    public static void initLogging(Context ctx) {
	// load logging type from preferences with no logging as default
	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
	LoggingTypes loggingType = LoggingTypes.values()[preferences.getInt(LOGGINGTYPE, NO_LOG)];

	LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
	try {
	    JoranConfigurator configurator = new JoranConfigurator();
	    configurator.setContext(lc);
	    lc.reset(); // override default configuration
	    InputStream configInputStream;
	    switch (loggingType) {
		case LOGCAT:
		    configInputStream = ctx.getAssets().open("logback-logcat.xml");
		    break;
		case LOGCAT_SDCARD:
		    configInputStream = ctx.getAssets().open("logback-logcat+sdcard.xml");
		    break;
		case SDCARD:
		    configInputStream = ctx.getAssets().open("logback-sdcard.xml");
		    break;
		default:
		    configInputStream = ctx.getAssets().open("logback-nolog.xml");
		    break;
	    }
	    configurator.doConfigure(configInputStream);
	} catch (JoranException je) {
	    // StatusPrinter will handle this
	} catch (IOException e) {
	    logger.error("Loading of logging config failed.", e);
	}
	StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
    }

}
