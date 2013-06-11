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

package org.openecard.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Openecard properties class.
 * These properties are loaded from the following places. The first occurrence of a key value pair overrides the next.
 * <ol>
 *   <li>System Properties</li>
 *   <li>$HOME/.openecard/openecard.properties</li>
 *   <li>$classpath/openecard_config/openecard.properties</li>
 * </ol>
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 */
public class OpenecardProperties {

    private static final Logger _logger = LoggerFactory.getLogger(OpenecardProperties.class);

    private static Internal properties;

    private static class Internal extends OverridingProperties {
	public Internal(InputStream bundledProps, InputStream homeProps) throws IOException {
	    super(bundledProps, homeProps);
	}
    }

    static {
	load();
    }
    /**
     * Load properties from application bundle and disc.
     */
    public static synchronized void load() {
	InputStream homeProps = null;
	InputStream bundledProps = null;
	try {
	    File homePath = FileUtils.getHomeConfigDir();
	    File cfgFile = new File(homePath, "openecard.properties");
	    homeProps = new FileInputStream(cfgFile);
	} catch (IOException ex) {
	    _logger.info("Failed to load bundled properties.", ex);
	}
	try {
	    String fileName = "openecard_config/openecard.properties";
	    bundledProps = FileUtils.resolveResourceAsStream(OpenecardProperties.class, fileName);
	} catch (IOException ex) {
	    _logger.info("Failed to load properties from config dir.", ex);
	}
	try {
	    properties = new Internal(bundledProps, homeProps);
	} catch (IOException ex) {
	    // in that case a null pointer occurs when properties is accessed
	    _logger.error(ex.getMessage(), ex);
	}
    }


    /**
     * @see OverridingProperties#getProperty(java.lang.String)
     */
    public static String getProperty(String key) {
	return properties.getProperty(key);
    }

    /**
     * @see OverridingProperties#properties()
     */
    public static Properties properties() {
	return properties.properties();
    }

}
