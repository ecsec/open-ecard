/****************************************************************************
 * Copyright (C) 2012-2018 ecsec GmbH.
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

import org.openecard.ws.common.OverridingProperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
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
 * @author Tobias Wich
 */
public class OpenecardProperties {

    private static final Logger LOG = LoggerFactory.getLogger(OpenecardProperties.class);

    private static OverridingProperties properties;

    static {
	load();
    }

    /**
     * Load properties from application bundle and disc.
     */
    public static synchronized void load() {
	InputStream homeProps = loadHomeProps();
	InputStream bundledProps = null;

	try {
	    String fileName = "openecard_config/openecard.properties";
	    bundledProps = FileUtils.resolveResourceAsStream(OpenecardProperties.class, fileName);
	} catch (IOException ex) {
	    LOG.info("Failed to load properties from config dir.", ex);
	}
	try {
	    properties = new OverridingProperties(bundledProps, homeProps);
	} catch (IOException ex) {
	    // in that case a null pointer occurs when properties is accessed
	    LOG.error(ex.getMessage(), ex);
	}
    }

    private static InputStream loadHomeProps() {
	try {
	    File homePath = FileUtils.getHomeConfigDir();
	    File cfgFile = new File(homePath, "openecard.properties");
	    InputStream homeProps = new FileInputStream(cfgFile);
	    return homeProps;
	} catch (FileNotFoundException ex) {
	    LOG.debug("No properties file to load as it does not exist.");
	    return null;
	} catch (IOException ex) {
	    LOG.warn("Failed to load bundled properties.", ex);
	    return null;
	}
    }

    private static void saveHomeProps(Properties homeProps) throws IOException {
	File homePath = FileUtils.getHomeConfigDir();
	File cfgFile = new File(homePath, "openecard.properties");
	OutputStream out = new FileOutputStream(cfgFile);
	homeProps.store(out, null);
	out.close();
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

    /**
     * Writes the changes, not the defaults in the given Properties instance to the user's config file.
     * This function preserves the property values already present in the config file.
     *
     * @param changes Properties to be written.
     * @throws IOException Thrown in case there was a problem reading or writing the config file.
     */
    public static synchronized void writeChanges(Properties changes) throws IOException {
	// load currently written properties
	Properties homeProps = new Properties();
	InputStream homeStream = loadHomeProps();
	if (homeStream != null) {
	    homeProps.load(homeStream);
	    homeStream.close();
	}

	for (Map.Entry<Object, Object> next : changes.entrySet()) {
	    homeProps.put(next.getKey(), next.getValue());
	}

	saveHomeProps(homeProps);
    }

}
