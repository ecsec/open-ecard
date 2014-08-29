/****************************************************************************
 * Copyright (C) 2014 ecsec GmbH.
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

package org.openecard.richclient.gui.manage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import org.openecard.addon.AddonProperties;
import org.openecard.addon.AddonPropertiesException;
import org.openecard.common.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Factory implementation which provides a Settings object which wraps a Properties or AddonProperties object.
 *
 * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
 */
public class SettingsFactory {

    private static final Logger logger = LoggerFactory.getLogger(AddonPropertiesWrapper.class);

    /**
     * The instance of the factory.
     */
    private static SettingsFactory instance;

    /**
     * Get a Settings object from the given Properties object.
     *
     * @param props Properties object to wrap.
     * @return A Settings object which wraps the {@code pops} object.
     */
    public static Settings getInstance(Properties props) {
	return new OpenecardPropertiesWrapper(props);
    }

    /**
     * Get a Settings object from the given AddonProperties object.
     *
     * @param props The AddonProperties to wrap in the Settings object.
     * @return A Settings object which wraps the {@code props} object.
     */
    public static Settings getInstance(AddonProperties props) {
	return new AddonPropertiesWrapper(props);
    }


    /**
     * The class extends the Settings class and wraps an AddonProperties object.
     *
     * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
     */
    public static class AddonPropertiesWrapper extends Settings {

	private final AddonProperties props;

	private AddonPropertiesWrapper(AddonProperties props) {
	    this.props = props;

	    try {
		props.loadProperties();
	    } catch (AddonPropertiesException ex) {
		logger.error("Failed to load AddonProperties.", ex);
	    }
	}

	@Override
	public void setProperty(String key, String value) {
	    props.setProperty(key, value);
	}

	@Override
	public String getProperty(String key) {
	    return props.getProperty(key);
	}

	@Override
	public void store() throws AddonPropertiesException{
	    props.saveProperties();
	}

    }

    /**
     * The class extends the Settings class and wraps a Properties object.
     *
     * @author Hans-Martin Haase <hans-martin.haase@ecsec.de>
     */
    public static class OpenecardPropertiesWrapper extends Settings {

	private final Properties props;

	private OpenecardPropertiesWrapper(Properties properties) {
	    props = properties;
	}

	@Override
	public void setProperty(String key, String value) {
	    props.setProperty(key, value);
	}

	@Override
	public String getProperty(String key) {
	    return props.getProperty(key);
	}

	@Override
	public void store() throws IOException {
	    File home = FileUtils.getHomeConfigDir();
	    File config = new File(home, "openecard.properties");
	    FileWriter writer = new FileWriter(config);
	    props.store(writer, null);
	}

    }
}
