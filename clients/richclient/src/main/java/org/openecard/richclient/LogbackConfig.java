/****************************************************************************
 * Copyright (C) 2012 ecsec GmbH.
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

package org.openecard.richclient;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import java.io.File;
import java.io.IOException;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for the Logback configuration.
 *
 * @author Tobias Wich
 */
public class LogbackConfig {

    private static final String CFG_FILENAME = "richclient_logback.xml";
    private static Logger LOG; // loaded on demand, for reason see in load() below

    public static File getConfFile() throws IOException {
	File cfgDir = FileUtils.getHomeConfigDir();
	String logFileStr = cfgDir + File.separator + CFG_FILENAME;
	File logFile = new File(logFileStr);
	return logFile;
    }

    /**
     * Load Logback configuration.
     * At first the code tries to load the file '$HOME/.openecard/richclient_logback.xml', if it does not exist, then
     * the default config is used. The default configuration is loaded from within the richclient jar.
     *
     * @throws IOException If no config could be loaded.
     * @throws SecurityException If the config dir is not writable.
     * @throws JoranException If the config file contains invalid content.
     */
    public static void load() throws IOException, SecurityException, JoranException {
	File logFile = getConfFile();

	if (logFile.canRead() && logFile.isFile()) {
	    // this prevents loading the bundled config
	    System.setProperty("logback.configurationFile", logFile.getAbsolutePath());

	    // make sure to reload logging config
	    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
	    ctx.reset();
	    JoranConfigurator conf = new JoranConfigurator();
	    conf.setContext(ctx);
	    ctx.reset();
	    conf.doConfigure(logFile);

	    // load logger after loading config, in order to avoid loading bundled config first
	    if (LOG == null) {
		LOG = LoggerFactory.getLogger(LogbackConfig.class);
	    }
	    LOG.info("Configured Logback with config file from: {}", logFile);
	} else {
	    loadDefault();
	}
    }

    public static void loadDefault() throws JoranException {
	System.clearProperty("logback.configurationFile");
	LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
	ctx.reset();
	ContextInitializer init = new ContextInitializer(ctx);
	init.autoConfig();
    }

}
