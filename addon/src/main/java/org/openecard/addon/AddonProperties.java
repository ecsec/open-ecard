/****************************************************************************
 * Copyright (C) 2013-2014 ecsec GmbH.
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

package org.openecard.addon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import org.openecard.addon.manifest.AddonSpecification;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The class implements the AddonProperties which basically wrap a Properties object.
 *
 * @author Tobias Wich <tobias.wich@ecsec.de>
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class AddonProperties {
    /**
     * Logger for logging errors and other necessary things.
     */
    private static final Logger logger = LoggerFactory.getLogger(AddonProperties.class);
    /**
     * File extension used for files if the saveProperties and loadProperties method is used.
     */
    private static final String FILE_EXTENSION = ".conf";

    /**
     * The ID of the addon.
     */
    private final String id;
    /**
     * A File object identifying the configuration file of the addon.
     */
    private File configFile;
    /**
     * The specification of the addon.
     */
    private final AddonSpecification addonSpec;
    /**
     * A Properties object which is used for managing the addon specific properties.
     */
    private final Properties props = new Properties();

    /**
     * Creates an AddonProperties object for the given AddonSpecification.
     *
     * @param addonSpecification AddonSpecification object containing the required attributes to set.
     */
    public AddonProperties(AddonSpecification addonSpecification) {
	addonSpec = addonSpecification;
	id = addonSpec.getId();

	try {
	    configFile = new File(FileUtils.getAddonsConfDir(), id + FILE_EXTENSION);
	} catch (IOException ex) {
	    logger.error("Couldn't create file object to the config directory.", ex);
	} catch (SecurityException ex) {
	    String msg = "SecurityException seems like you do not have enough permissions to create a file object for "
		    + "the config directory.";
	    logger.error(msg, ex);
	}
    }

    /**
     * Loads a existing configuration file.
     * <p>
     * If there exists no file containing properties than nothing is loaded.
     * </p>
     *
     * @throws AddonPropertiesException Thrown if a conf file exists but can't be loaded.
     */
    public void loadProperties() throws AddonPropertiesException {
	if (configFile.exists()) {
	    try {
		props.load(new FileInputStream(configFile));
	    } catch (IOException ex) {
		logger.error("Failed to load properties for the addon with the ID " + id + ".", ex);
		throw new AddonPropertiesException("Failed to load properties for the addon with the ID " + id + ".", ex);
	    }
	} else {
	    logger.warn("Can't load the properties of the addon with the ID " + id + ". The file containing the "
		    + "properties does not exist.");
	}
    }

    /**
     * Saves the currently set properties to a file.
     * <p>
     * The file will be located in {@code $HOME/.openecard/addons}.
     * The file name will be {@code <id>.conf}.
     * </p>
     *
     * @throws AddonPropertiesException Thrown if the conf file can't be found or an error occurred while saving
     * the properties.
     */
    public void saveProperties() throws AddonPropertiesException {
	try {
	    OutputStream out = new FileOutputStream(configFile, false);
	    props.store(out, "Configuration file of the " + id + "addon.");
	} catch (FileNotFoundException ex) {
	    String msg = "Failed to save the properties for the addon with the ID " + id + ". The file was not found."
		    + "The settings aren't stored.";
	    logger.error(msg, ex);
	    throw new AddonPropertiesException(msg, ex);
	} catch (IOException ex) {
	    String msg = "Failed to save the properties for the addon with the ID " + id + ". The settings aren't "
		    + "stored.";
	    logger.error(msg, ex);
	    throw new AddonPropertiesException(msg, ex);
	}
    }

    /**
     * Set a new property with the key <b>key</b> and the value <b>value</b>
     *
     * @param key A string representing the name of the property.
     * @param value A string representing the value of the property.
     */
    public void setProperty(String key, String value) {
	props.setProperty(key, value);
    }

    /**
     * Get a property by a key.
     *
     * @param key A string representing the name of the property.
     * @return The value of property <b>key</b>. If the property does not exist <b>null</b> is returned.
     */
    public String getProperty(String key) {
	return props.getProperty(key);
    }

}
