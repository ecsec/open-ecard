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

package org.openecard.pkcs11;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import java.io.File;
import java.io.IOException;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for the Logback configuration.
 * This class loads a file named pkcs11_logback.xml from the home directory if it is present.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class LogbackConfig {

    private static final Logger logger = LoggerFactory.getLogger(LogbackConfig.class);
    private static final String cfgFileName = "pkcs11_logback.xml";

    /**
     * Load Logback configuration.
     * At first the code tries to load the file '$HOME/.openecard/pkcs11_logback.xml', if it does not exist, then
     * the default config is used. The default configuration is loaded from withing the pkcs11 jar.
     *
     * @throws IOException If no config could be loaded.
     * @throws SecurityException If the config dir is not writable.
     * @throws JoranException If the config file contains invalid content.
     */
    public static void load() throws IOException, SecurityException, JoranException {
	File cfgDir = FileUtils.getHomeConfigDir();
	String logFileStr = cfgDir + File.separator + cfgFileName;
	File logFile = new File(logFileStr);

	if (logFile.canRead() && logFile.isFile()) {
	    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
	    JoranConfigurator conf = new JoranConfigurator();
	    conf.setContext(ctx);
	    ctx.reset();
	    conf.doConfigure(logFile);
	    logger.info("Configured Logback with config file from: {}", logFile);
	}
    }

}
