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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
    public static final int DEFAULT = LoggingTypes.LOGCAT.ordinal();
    public static final String EXIT = "EXIT";

    /**
     * Initializes the logging according to the type currently stored in the preferences. 
     * If no type is found in the preferences no logging is used as default.
     * 
     * @param ctx Context of the App
     */
    public static void initLogging(Context ctx) {
	// load logging type from preferences with no logging as default
	SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
	LoggingTypes loggingType = LoggingTypes.values()[preferences.getInt(LOGGINGTYPE, DEFAULT)];

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

    /**
     * Send the Intent for displaying an Uri especially to the browser that invoked the App. This prevents the 'browser
     * choosing dialog' from popping up on system with more than one browser installed. We need this because if we close
     * the App immediately after sending this intent, the choosing dialog will be closed too and the user won't see any
     * site.
     * 
     * @param browserIntent The initial intent for displaying the Uri
     * @param ctx Context of the App
     * @return an Intent to call the refresh URL in a browser activity
     */
    public static Intent getRefreshIntent(Intent browserIntent, Context ctx) {
	String browserActivityName;
	if (browserIntent.getBooleanExtra("isTokenFromObject", false)) {
	    // currently only the firefox plugin starts the app this way
	    browserActivityName = "firefox";
	} else {
	    browserActivityName = "browseractivity";
	}
	Uri uri = browserIntent.getData();
	PackageManager packageManager = ctx.getPackageManager();
	List<ResolveInfo> list = packageManager.queryIntentActivities(browserIntent, 0);
	logger.debug("Number of possibly matching activities: {}", list.size());
	for (ResolveInfo resolveInfo : list) {
	    String activityName = resolveInfo.activityInfo.name;
	    String packageName = resolveInfo.activityInfo.packageName;
	    logger.debug("checking activity {}", activityName);
	    if (activityName.toLowerCase().contains(browserActivityName)) {
		logger.debug("Found the sought browser: package: {} activity name: {}", packageName, activityName);
		browserIntent = packageManager.getLaunchIntentForPackage(packageName);
		ComponentName comp = new ComponentName(packageName, activityName);
		browserIntent.setAction(Intent.ACTION_VIEW);
		browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
		browserIntent.setComponent(comp);
		browserIntent.setData(uri);
		break;
	    }
	}
	return browserIntent;
    }

}
