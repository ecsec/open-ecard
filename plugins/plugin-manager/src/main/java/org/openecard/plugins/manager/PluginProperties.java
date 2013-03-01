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

package org.openecard.plugins.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import org.openecard.common.util.FileUtils;


/**
 * Helper class for loading and saving the activation status of plugins in a properties file.
 *
 * @author Dirk Petrautzki <petrautzki@hs-coburg.de>
 */
public final class PluginProperties {

    private static final String COMMENT = "This file stores the activation status of the available plugins.";
    private static final String FALSE = Boolean.toString(false);
    private static final String PLUGIN_PROPERTIES_FILE = "plugin.properties";
    private static final Properties props = new Properties();

    /**
     * Loads the plugin Properties from the properties file in the home configuration directory.
     * <br/> Creates the properties file if it doesn't exist.
     *
     * @throws IOException if the home configuration directory coudn't be found or the properties file coudn't be loaded
     */
    public static void loadProperties() throws IOException {
	InputStream is = null;

	File cfgDir = FileUtils.getHomeConfigDir();
	String propFileStr = cfgDir + File.separator + PLUGIN_PROPERTIES_FILE;
	File propFile = new File(propFileStr);

	if (! propFile.exists()) {
	    propFile.createNewFile();
	}

	is = new FileInputStream(propFile);
	props.load(is);
    }

    static String getProperty(String key) {
	return props.getProperty(key, FALSE);
    }

    static Object setProperty(String key, String value) {
	return props.setProperty(key, value);
    }

    /**
     * Saves the plugin Properties into the properties file in the home configuration directory.
     * 
     * @throws IOException if the home configuration directory coudn't be found or the properties coudn't be saved
     */
    public static void saveProperties() throws IOException {
	File cfgDir = FileUtils.getHomeConfigDir();
	String logFileStr = cfgDir + File.separator + PLUGIN_PROPERTIES_FILE;
	File logFile = new File(logFileStr);
	OutputStream out = new FileOutputStream(logFile);
	props.store(out, COMMENT);
    }

}
